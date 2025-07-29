package com.u1mobis.dashboard_backend.service;

import com.u1mobis.dashboard_backend.dto.RobotDto;
import com.u1mobis.dashboard_backend.dto.MqttRobotDataDto;
import com.u1mobis.dashboard_backend.entity.Robot;
import com.u1mobis.dashboard_backend.entity.Company;
import com.u1mobis.dashboard_backend.repository.RobotRepository;
import com.u1mobis.dashboard_backend.repository.CompanyRepository;
import com.u1mobis.dashboard_backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
    private final CompanyRepository companyRepository;
    private final JwtUtil jwtUtil;
    
    // 생산 사이클 관리용
    private final Map<String, ScheduledFuture<?>> productionSchedulers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService schedulerService = Executors.newScheduledThreadPool(10);

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
    
    // ===== JWT 기반 권한 검증 메서드들 =====
    
    /**
     * 현재 사용자의 JWT 토큰에서 회사ID 추출
     */
    private Long getCurrentUserCompanyId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() instanceof String) {
            String token = (String) authentication.getDetails();
            return jwtUtil.getCompanyIdFromToken(token);
        }
        return null;
    }
    
    /**
     * 요청된 회사가 현재 사용자의 회사와 일치하는지 검증
     */
    private void validateCompanyAccess(String companyName) {
        Company company = companyRepository.findByCompanyName(companyName)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회사입니다: " + companyName));
        
        Long userCompanyId = getCurrentUserCompanyId();
        if (userCompanyId == null) {
            throw new RuntimeException("인증 정보를 찾을 수 없습니다.");
        }
        
        if (!company.getCompanyId().equals(userCompanyId)) {
            throw new RuntimeException("해당 회사의 데이터에 접근할 권한이 없습니다.");
        }
    }
    
    // ===== 멀티테넌트 지원 메서드들 =====
    
    /**
     * 회사명으로 해당 회사의 모든 로봇 조회
     */
    @Transactional(readOnly = true)
    public List<RobotDto> getRobotsByCompanyName(String companyName) {
        log.info("회사별 로봇 목록 조회 - 회사명: {}", companyName);
        
        // JWT 기반 권한 검증
        validateCompanyAccess(companyName);
        
        // 회사명으로 회사 조회
        Company company = companyRepository.findByCompanyName(companyName)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회사입니다: " + companyName));
        
        List<Robot> robots = robotRepository.findByCompanyId(company.getCompanyId());
        
        return robots.stream()
                .map(robot -> convertToDtoWithCalculations(robot))
                .collect(Collectors.toList());
    }
    
    /**
     * 회사명과 로봇ID로 특정 로봇 조회 (권한 검증 포함)
     */
    @Transactional(readOnly = true)
    public RobotDto getRobotDataByCompany(String companyName, String robotId) {
        log.info("회사별 로봇 데이터 조회 - 회사명: {}, 로봇ID: {}", companyName, robotId);
        
        // JWT 기반 권한 검증
        validateCompanyAccess(companyName);
        
        // 회사명으로 회사 조회
        Company company = companyRepository.findByCompanyName(companyName)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 회사입니다: " + companyName));
        
        // 로봇 조회 및 회사 소속 확인
        Robot robot = robotRepository.findByRobotIdAndCompanyId(robotId, company.getCompanyId())
                .orElseThrow(() -> new RuntimeException("해당 회사에 속하지 않는 로봇이거나 존재하지 않는 로봇입니다: " + robotId));
        
        return convertToDtoWithCalculations(robot);
    }
    
    /**
     * 계산된 값들을 포함한 DTO 변환 (건강도, 가동률 등 계산 로직 포함)
     */
    private RobotDto convertToDtoWithCalculations(Robot robot) {
        RobotDto dto = convertToDto(robot);
        
        // 건강도 계산 (효율성 기반, 0-100점)
        Double health = calculateRobotHealth(robot);
        
        // 가동률 계산 (상태와 효율성 기반)
        Double utilization = calculateUtilization(robot);
        
        // 알람 상태 계산
        String alarmStatus = calculateAlarmStatus(robot);
        
        // 통신 상태 계산
        String connectionStatus = calculateConnectionStatus(robot);
        
        // 계산된 값들을 DTO에 추가 설정
        return dto.toBuilder()
                .health(health)
                .utilization(utilization)
                .alarmStatus(alarmStatus)
                .connectionStatus(connectionStatus)
                .build();
    }
    
    /**
     * 로봇 건강도 계산 (0-100점)
     */
    private Double calculateRobotHealth(Robot robot) {
        double baseHealth = 100.0;
        
        // 온도 기반 건강도 감소
        if (robot.getTemperature() != null) {
            double temp = robot.getTemperature();
            if (temp > 80) baseHealth -= 30; // 과열
            else if (temp > 60) baseHealth -= 15; // 높은 온도
            else if (temp < 10) baseHealth -= 10; // 낮은 온도
        }
        
        // 상태 기반 건강도
        if (robot.getStatusText() != null) {
            switch (robot.getStatusText()) {
                case "ERROR":
                    baseHealth -= 50;
                    break;
                case "MAINTENANCE":
                    baseHealth -= 20;
                    break;
                case "IDLE":
                    baseHealth -= 5;
                    break;
            }
        }
        
        // 전력 소비량 기반 (비정상적으로 높거나 낮으면 감점)
        if (robot.getPowerConsumption() != null) {
            double power = robot.getPowerConsumption();
            if (power > 500 || power < 50) {
                baseHealth -= 10;
            }
        }
        
        return Math.max(0, Math.min(100, baseHealth));
    }
    
    /**
     * 가동률 계산 (백분율)
     */
    private Double calculateUtilization(Robot robot) {
        if (robot.getStatusText() == null) return 0.0;
        
        switch (robot.getStatusText()) {
            case "작동중":
            case "RUNNING":
                // 사이클 타임과 생산량 기반 계산
                if (robot.getCycleTime() != null && robot.getCycleTime() > 0) {
                    // 이상적인 사이클 타임 대비 실제 성능
                    double idealCycle = 20.0; // 기준 사이클 타임 (초)
                    double efficiency = idealCycle / robot.getCycleTime() * 100;
                    return Math.min(100.0, Math.max(0.0, efficiency));
                }
                return 85.0; // 기본 가동률
            case "IDLE":
            case "대기중":
                return 0.0;
            case "MAINTENANCE":
            case "정지":
                return 0.0;
            case "ERROR":
                return 0.0;
            default:
                return 0.0;
        }
    }
    
    /**
     * 알람 상태 계산
     */
    private String calculateAlarmStatus(Robot robot) {
        // 온도 기반 알람
        if (robot.getTemperature() != null && robot.getTemperature() > 80) {
            return "심각";
        }
        
        // 상태 기반 알람
        if (robot.getStatusText() != null) {
            switch (robot.getStatusText()) {
                case "ERROR":
                    return "심각";
                case "MAINTENANCE":
                    return "경고";
                default:
                    return "정상";
            }
        }
        
        return "정상";
    }
    
    /**
     * 통신 상태 계산
     */
    private String calculateConnectionStatus(Robot robot) {
        if (robot.getLastUpdate() != null) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime lastUpdate = robot.getLastUpdate();
            
            // 5분 이내 업데이트면 온라인
            if (lastUpdate.isAfter(now.minusMinutes(5))) {
                return "온라인";
            }
        }
        
        // ERROR 상태이거나 업데이트가 오래된 경우
        if ("ERROR".equals(robot.getStatusText())) {
            return "오프라인";
        }
        
        return "오프라인";
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