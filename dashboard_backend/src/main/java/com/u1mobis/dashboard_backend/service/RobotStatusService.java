package com.u1mobis.dashboard_backend.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.u1mobis.dashboard_backend.entity.Robot;
import com.u1mobis.dashboard_backend.entity.RobotStatus;
import com.u1mobis.dashboard_backend.repository.RobotRepository;
import com.u1mobis.dashboard_backend.repository.RobotStatusRepository;

@Service
@Transactional
public class RobotStatusService {
    
    @Autowired
    private RobotStatusRepository robotStatusRepository;
    
    @Autowired
    private RobotRepository robotRepository;
    
    public List<RobotStatus> getAllRobotStatus(Long companyId) {
        return robotStatusRepository.findLatestByCompanyId(companyId);
    }
    
    public Optional<RobotStatus> getRobotStatus(Long robotId, Long companyId) {
        return robotStatusRepository.findByRobotIdAndCompanyId(robotId, companyId);
    }
    
    public Optional<RobotStatus> getLatestRobotStatus(Long robotId) {
        return robotStatusRepository.findLatestByRobotId(robotId);
    }
    
    public List<RobotStatus> getRobotStatusByStation(String stationCode, Long companyId) {
        // 해당 공정의 로봇들을 먼저 찾기
        List<Robot> robots = robotRepository.findByCompanyIdAndStationCode(companyId, stationCode);
        List<Long> robotIds = robots.stream().map(Robot::getRobotId).toList();
        
        if (robotIds.isEmpty()) {
            return List.of();
        }
        
        return robotStatusRepository.findLatestByRobotIds(robotIds);
    }
    
    public RobotStatus updateRobotStatus(Long robotId, Long companyId, Integer motorStatus, Integer ledStatus) {
        RobotStatus robotStatus = RobotStatus.builder()
                .robotId(robotId)
                .companyId(companyId)
                .motorStatus(motorStatus)
                .ledStatus(ledStatus)
                .timestamp(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        
        return robotStatusRepository.save(robotStatus);
    }
    
    public List<RobotStatus> getRecentRobotStatus(Long companyId, int hours) {
        LocalDateTime fromTime = LocalDateTime.now().minusHours(hours);
        return robotStatusRepository.findByCompanyIdAndTimestampAfter(companyId, fromTime);
    }
    
    public String getRobotStatusText(Integer motorStatus, Integer ledStatus) {
        if (motorStatus == 1 && ledStatus == 1) {
            return "운영중";
        } else if (motorStatus == 0 && ledStatus == 1) {
            return "점검중";
        } else if (motorStatus == 0 && ledStatus == 0) {
            return "정지";
        } else {
            return "알수없음";
        }
    }
    
    // 디지털 트윈용 상세 정보 조회
    public RobotStatusInfo getRobotStatusInfo(Long robotId, Long companyId) {
        Optional<Robot> robotOpt = robotRepository.findByRobotIdAndCompanyId(robotId, companyId);
        Optional<RobotStatus> statusOpt = robotStatusRepository.findByRobotIdAndCompanyId(robotId, companyId);
        
        if (robotOpt.isEmpty()) {
            return null;
        }
        
        Robot robot = robotOpt.get();
        RobotStatus status = statusOpt.orElse(null);
        
        return RobotStatusInfo.builder()
                .robotId(robot.getRobotId())
                .robotName(robot.getRobotName())
                .robotType(robot.getRobotType())
                .stationCode(robot.getStationCode())
                .motorStatus(status != null ? status.getMotorStatus() : 0)
                .ledStatus(status != null ? status.getLedStatus() : 0)
                .statusText(status != null ? getRobotStatusText(status.getMotorStatus(), status.getLedStatus()) : "알수없음")
                .lastUpdate(status != null ? status.getTimestamp() : null)
                .build();
    }
    
    // 내부 클래스로 상태 정보 DTO 정의
    public static class RobotStatusInfo {
        private Long robotId;
        private String robotName;
        private String robotType;
        private String stationCode;
        private Integer motorStatus;
        private Integer ledStatus;
        private String statusText;
        private LocalDateTime lastUpdate;
        
        // Builder 패턴
        public static RobotStatusInfoBuilder builder() {
            return new RobotStatusInfoBuilder();
        }
        
        public static class RobotStatusInfoBuilder {
            private Long robotId;
            private String robotName;
            private String robotType;
            private String stationCode;
            private Integer motorStatus;
            private Integer ledStatus;
            private String statusText;
            private LocalDateTime lastUpdate;
            
            public RobotStatusInfoBuilder robotId(Long robotId) {
                this.robotId = robotId;
                return this;
            }
            
            public RobotStatusInfoBuilder robotName(String robotName) {
                this.robotName = robotName;
                return this;
            }
            
            public RobotStatusInfoBuilder robotType(String robotType) {
                this.robotType = robotType;
                return this;
            }
            
            public RobotStatusInfoBuilder stationCode(String stationCode) {
                this.stationCode = stationCode;
                return this;
            }
            
            public RobotStatusInfoBuilder motorStatus(Integer motorStatus) {
                this.motorStatus = motorStatus;
                return this;
            }
            
            public RobotStatusInfoBuilder ledStatus(Integer ledStatus) {
                this.ledStatus = ledStatus;
                return this;
            }
            
            public RobotStatusInfoBuilder statusText(String statusText) {
                this.statusText = statusText;
                return this;
            }
            
            public RobotStatusInfoBuilder lastUpdate(LocalDateTime lastUpdate) {
                this.lastUpdate = lastUpdate;
                return this;
            }
            
            public RobotStatusInfo build() {
                return new RobotStatusInfo(robotId, robotName, robotType, stationCode, 
                                         motorStatus, ledStatus, statusText, lastUpdate);
            }
        }
        
        // 생성자 및 getter/setter
        public RobotStatusInfo(Long robotId, String robotName, String robotType, String stationCode,
                              Integer motorStatus, Integer ledStatus, String statusText, LocalDateTime lastUpdate) {
            this.robotId = robotId;
            this.robotName = robotName;
            this.robotType = robotType;
            this.stationCode = stationCode;
            this.motorStatus = motorStatus;
            this.ledStatus = ledStatus;
            this.statusText = statusText;
            this.lastUpdate = lastUpdate;
        }
        
        // Getters
        public Long getRobotId() { return robotId; }
        public String getRobotName() { return robotName; }
        public String getRobotType() { return robotType; }
        public String getStationCode() { return stationCode; }
        public Integer getMotorStatus() { return motorStatus; }
        public Integer getLedStatus() { return ledStatus; }
        public String getStatusText() { return statusText; }
        public LocalDateTime getLastUpdate() { return lastUpdate; }
    }
}