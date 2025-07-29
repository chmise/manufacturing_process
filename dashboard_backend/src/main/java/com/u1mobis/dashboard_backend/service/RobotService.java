package com.u1mobis.dashboard_backend.service;

import com.u1mobis.dashboard_backend.dto.RobotDto;
import com.u1mobis.dashboard_backend.dto.MqttRobotDataDto;
import com.u1mobis.dashboard_backend.entity.Robot;
import com.u1mobis.dashboard_backend.entity.Company;
import com.u1mobis.dashboard_backend.repository.RobotRepository;
import com.u1mobis.dashboard_backend.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RobotService {
    
    private final RobotRepository robotRepository;
    private final CompanyRepository companyRepository; // CompanyRepository 추가
    
    // 생산 사이클 관리용
    private final Map<String, ScheduledFuture<?>> productionSchedulers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService schedulerService = Executors.newScheduledThreadPool(10);

    @Transactional(readOnly = true)
    public RobotDto getRobotDataByCompany(String companyName, String robotId) {
        log.info("로봇 데이터 조회 - 회사: {}, 로봇ID: {}", companyName, robotId);
        Long companyId = companyRepository.findByCompanyName(companyName)
                .map(Company::getCompanyId)
                .orElseThrow(() -> new RuntimeException("회사를 찾을 수 없습니다: " + companyName));
        Robot robot = robotRepository.findByRobotIdAndCompanyId(robotId, companyId)
                .orElseThrow(() -> new RuntimeException("로봇을 찾을 수 없습니다: " + robotId + ", 회사: " + companyName));
        return convertToDto(robot);
    }

    @Transactional(readOnly = true)
    public List<RobotDto> getRobotsByCompanyName(String companyName) {
        log.info("회사별 로봇 목록 조회 - 회사: {}", companyName);
        Long companyId = companyRepository.findByCompanyName(companyName)
                .map(Company::getCompanyId)
                .orElseThrow(() -> new RuntimeException("회사를 찾을 수 없습니다: " + companyName));
        return robotRepository.findByCompanyId(companyId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // ===== MQTT 데이터 처리 =====
    
    @Transactional
    public void updateRobotFromMqtt(String companyName, MqttRobotDataDto mqttData) {
        String robotId = mqttData.getRobotId();
        
        Optional<Robot> robotOpt = robotRepository.findById(robotId);
        
        if (robotOpt.isPresent()) {
            Robot robot = robotOpt.get();
            
            // MQTT 데이터로 업데이트
            robot.setStatusText(mqttData.getStatusText());
            robot.setTemperature(mqttData.getTemperature());
            robot.setCycleTime(mqttData.getCycleTime());
            robot.setPowerConsumption(mqttData.getPowerConsumption());
            robot.setMotorStatus("작동중".equals(mqttData.getStatusText()) ? 1 : 0);
            
            robotRepository.save(robot);
            
            // 생산 사이클 관리
            manageProductionCycle(robot);
            
            log.info("로봇 {} MQTT 데이터 업데이트 완료", robotId);
        } else {
            log.warn("존재하지 않는 로봇 ID: {}", robotId);
        }
    }

    // ===== 생산 사이클 관리 =====
    
    private void manageProductionCycle(Robot robot) {
        String robotId = robot.getRobotId();
        
        // 기존 스케줄러 취소
        ScheduledFuture<?> existingScheduler = productionSchedulers.get(robotId);
        if (existingScheduler != null && !existingScheduler.isCancelled()) {
            existingScheduler.cancel(false);
        }

        // 작동중이고 사이클타임이 있으면 새 스케줄러 시작
        if ("작동중".equals(robot.getStatusText()) && robot.getCycleTime() != null && robot.getCycleTime() > 0) {
            int cycleTimeSeconds = robot.getCycleTime();
            
            ScheduledFuture<?> scheduler = schedulerService.scheduleAtFixedRate(
                    () -> increaseProduction(robotId),
                    cycleTimeSeconds,
                    cycleTimeSeconds,
                    TimeUnit.SECONDS
            );
            
            productionSchedulers.put(robotId, scheduler);
            log.info("로봇 {} 생산 스케줄러 시작: {}초 주기", robotId, cycleTimeSeconds);
        } else {
            productionSchedulers.remove(robotId);
            log.info("로봇 {} 생산 스케줄러 정지", robotId);
        }
    }

    @Async
    @Transactional
    public void increaseProduction(String robotId) {
        try {
            Optional<Robot> robotOpt = robotRepository.findById(robotId);
            
            if (robotOpt.isPresent()) {
                Robot robot = robotOpt.get();
                
                if ("작동중".equals(robot.getStatusText())) {
                    int currentCount = robot.getProductionCount() != null ? robot.getProductionCount() : 0;
                    robot.setProductionCount(currentCount + 1);
                    robotRepository.save(robot);
                    
                    log.info("로봇 {} 생산량 증가: {} → {}", robotId, currentCount, currentCount + 1);
                }
            }
        } catch (Exception e) {
            log.error("로봇 {} 생산량 증가 중 오류", robotId, e);
        }
    }

    // ===== REST API용 메서드들 =====
    
    @Transactional(readOnly = true)
    public RobotDto getRobotData(String robotId) {
        Robot robot = robotRepository.findById(robotId)
                .orElseThrow(() -> new RuntimeException("로봇을 찾을 수 없습니다: " + robotId));
        
        return convertToDto(robot);
    }
    
    @Transactional(readOnly = true)
    public List<RobotDto> getAllRobots() {
        return robotRepository.findAll().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<RobotDto> getRobotsByCompany(Long companyId) {
        return robotRepository.findByCompanyId(companyId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    private RobotDto convertToDto(Robot robot) {
        return RobotDto.builder()
                .robotId(robot.getRobotId())
                .robotName(robot.getRobotName())
                .robotType(robot.getRobotType())
                .statusText(robot.getStatusText())
                .motorStatus(robot.getMotorStatus())
                .ledStatus(robot.getLedStatus())
                .cycleTime(robot.getCycleTime())
                .productionCount(robot.getProductionCount())
                .quality(robot.getQuality())
                .temperature(robot.getTemperature())
                .powerConsumption(robot.getPowerConsumption())
                .lastUpdate(robot.getLastUpdate())
                .companyId(robot.getCompanyId())
                .lineId(robot.getProductionLine() != null ? robot.getProductionLine().getLineId() : null)
                .build();
    }
}