package com.u1mobis.dashboard_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "conveyor_control")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConveyorControl {
    
    @Id
    @Column(name = "conveyor_id")
    private String conveyorId;  // "CONV_L1_001", "CONV_L2_001" 등
    
    @Column(name = "conveyor_name", nullable = false)
    private String conveyorName;  // 컨베이어 이름
    
    @Column(name = "line_id", nullable = false)
    private Long lineId;  // 어느 라인에 속하는지
    
    @Column(name = "command", nullable = false)
    private String command = "STOP";  // START, STOP, PAUSE, EMERGENCY_STOP
    
    @Column(name = "speed")
    private Double speed = 1.0;  // 속도 (0.5-2.0 m/s 범위)
    
    @Column(name = "direction")
    private String direction = "FORWARD";  // FORWARD, REVERSE
    
    @Column(name = "sensor_status")
    private Boolean sensorStatus = false;  // 센서 감지 상태
    
    @Column(name = "reason")
    private String reason;  // 명령 이유
    
    @Column(name = "is_emergency")
    private Boolean isEmergency = false;  // 비상 정지 상태
    
    @Column(name = "maintenance_mode")
    private Boolean maintenanceMode = false;  // 정비 모드
    
    @Column(name = "total_runtime")
    private Long totalRuntime = 0L;  // 총 가동 시간 (초)
    
    @Column(name = "last_command_time")
    private LocalDateTime lastCommandTime;
    
    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // ProductionLine과의 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id", insertable = false, updatable = false)
    private ProductionLine productionLine;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 편의 메서드: 컨베이어 시작
    public void start(double speed, String reason) {
        this.command = "START";
        this.speed = Math.max(0.5, Math.min(2.0, speed));  // 속도 범위 제한
        this.reason = reason;
        this.isEmergency = false;
        this.lastCommandTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // 편의 메서드: 컨베이어 정지
    public void stop(String reason) {
        this.command = "STOP";
        this.reason = reason;
        this.lastCommandTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // 편의 메서드: 비상 정지
    public void emergencyStop(String reason) {
        this.command = "EMERGENCY_STOP";
        this.reason = reason;
        this.isEmergency = true;
        this.speed = 0.0;
        this.lastCommandTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // 편의 메서드: 방향 변경
    public void changeDirection(String newDirection, String reason) {
        if ("FORWARD".equals(newDirection) || "REVERSE".equals(newDirection)) {
            this.direction = newDirection;
            this.reason = reason;
            this.lastCommandTime = LocalDateTime.now();
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    // 편의 메서드: 센서 상태 업데이트
    public void updateSensorStatus(boolean detected) {
        this.sensorStatus = detected;
        this.updatedAt = LocalDateTime.now();
    }
}