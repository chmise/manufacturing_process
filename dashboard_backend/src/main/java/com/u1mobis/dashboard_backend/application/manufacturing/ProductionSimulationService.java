package com.u1mobis.dashboard_backend.application.manufacturing;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.manufacturing.model.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductionSimulationService {
    private final ProductionApplicationService productionApplicationService;
    
    // 시뮬레이션 상태 관리
    private final Map<String, SimulationState> activeSimulations = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(5);
    private final Random random = new Random();
    
    // 사용 가능한 색상 및 제품
    private final String[] availableColors = {"RED", "BLUE", "WHITE", "BLACK", "SILVER"};
    private final String[] productNames = {"Car Model A", "Car Model B", "Car Model C"};
    
    public void startSimulation(Long companyId, Long lineId) {
        String simulationKey = companyId + "_" + lineId;
        
        if (activeSimulations.containsKey(simulationKey) && 
            activeSimulations.get(simulationKey).isRunning()) {
            log.info("Simulation already running for company: {}, line: {}", companyId, lineId);
            return;
        }
        
        SimulationState state = new SimulationState(companyId, lineId);
        activeSimulations.put(simulationKey, state);
        
        // 주기적 생산 시작 스케줄링 (30초마다)
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (state.isRunning()) {
                    startRandomProduction(companyId, lineId);
                }
            } catch (Exception e) {
                log.error("Error in production simulation for {}: {}", simulationKey, e.getMessage());
            }
        }, 5, 30, TimeUnit.SECONDS);
        
        // 생산 진행 시뮬레이션 (10초마다)
        scheduler.scheduleAtFixedRate(() -> {
            try {
                if (state.isRunning()) {
                    simulateProductionProgress(companyId);
                }
            } catch (Exception e) {
                log.error("Error in progress simulation for {}: {}", simulationKey, e.getMessage());
            }
        }, 10, 10, TimeUnit.SECONDS);
        
        log.info("Started production simulation for company: {}, line: {}", companyId, lineId);
    }
    
    public void stopSimulation(Long companyId, Long lineId) {
        String simulationKey = companyId + "_" + lineId;
        SimulationState state = activeSimulations.get(simulationKey);
        
        if (state != null) {
            state.stop();
            activeSimulations.remove(simulationKey);
            log.info("Stopped production simulation for company: {}, line: {}", companyId, lineId);
        }
    }
    
    public void stopAllSimulations(Long companyId) {
        activeSimulations.entrySet().removeIf(entry -> {
            String key = entry.getKey();
            SimulationState state = entry.getValue();
            
            if (key.startsWith(companyId + "_")) {
                state.stop();
                log.info("Stopped simulation: {}", key);
                return true;
            }
            return false;
        });
    }
    
    private void startRandomProduction(Long companyId, Long lineId) {
        try {
            String productName = productNames[random.nextInt(productNames.length)];
            String color = availableColors[random.nextInt(availableColors.length)];
            
            String productionId = productionApplicationService.startNewProduction(companyId, lineId, productName, color);
            
            log.debug("Started new production: {} for company: {}, line: {}", productionId, companyId, lineId);
            
        } catch (Exception e) {
            log.warn("Could not start new production for company {}, line {}: {}", companyId, lineId, e.getMessage());
        }
    }
    
    private void simulateProductionProgress(Long companyId) {
        try {
            var activeProductions = productionApplicationService.getActiveProductions(companyId);
            
            for (Production production : activeProductions) {
                simulateProductionSteps(production);
            }
            
        } catch (Exception e) {
            log.error("Error simulating production progress for company {}: {}", companyId, e.getMessage());
        }
    }
    
    private void simulateProductionSteps(Production production) {
        try {
            ProductionState state = production.getCurrentState();
            String productionId = production.getId().getValue();
            
            switch (state) {
                case IN_PROGRESS:
                    // 로봇 작업 구역으로 이동
                    productionApplicationService.moveProductionToStation(productionId, "ROBOT_WORK", 10.0, 5.0, 25);
                    break;
                    
                case MOVING_TO_ROBOT:
                    // 로봇 작업 시작
                    productionApplicationService.moveProductionToStation(productionId, "ROBOT_WORK", 10.0, 5.0, 40);
                    break;
                    
                case ROBOT_WORKING:
                    // 로봇 작업 완료 및 검사 구역으로 이동
                    productionApplicationService.completeWorkAtStation(productionId, "ROBOT_WORK");
                    productionApplicationService.moveProductionToStation(productionId, "INSPECTION", 20.0, 10.0, 70);
                    break;
                    
                case MOVING_TO_INSPECTION:
                    // 검사 시작
                    productionApplicationService.moveProductionToStation(productionId, "INSPECTION", 20.0, 10.0, 85);
                    break;
                    
                case INSPECTING:
                    // 품질 검사 결과에 따른 처리
                    if (random.nextDouble() < 0.95) { // 95% 합격률
                        productionApplicationService.completeWorkAtStation(productionId, "INSPECTION");
                        productionApplicationService.moveProductionToStation(productionId, "FINAL_ASSEMBLY", 30.0, 15.0, 100);
                    } else {
                        // 5% 확률로 재작업 필요
                        productionApplicationService.requireRework(productionId, "Quality inspection failed");
                    }
                    break;
                    
                default:
                    // 다른 상태는 처리하지 않음
                    break;
            }
            
        } catch (Exception e) {
            log.error("Error simulating steps for production {}: {}", production.getId().getValue(), e.getMessage());
        }
    }
    
    private static class SimulationState {
        private final Long companyId;
        private final Long lineId;
        private final LocalDateTime startTime;
        private volatile boolean running;
        
        public SimulationState(Long companyId, Long lineId) {
            this.companyId = companyId;
            this.lineId = lineId;
            this.startTime = LocalDateTime.now();
            this.running = true;
        }
        
        public boolean isRunning() {
            return running;
        }
        
        public void stop() {
            this.running = false;
        }
        
        public Long getCompanyId() {
            return companyId;
        }
        
        public Long getLineId() {
            return lineId;
        }
        
        public LocalDateTime getStartTime() {
            return startTime;
        }
    }
}