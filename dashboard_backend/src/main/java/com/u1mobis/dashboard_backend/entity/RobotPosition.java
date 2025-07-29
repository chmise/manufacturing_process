package com.u1mobis.dashboard_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "robot_positions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RobotPosition {
    
    @Id
    @Column(name = "robot_id")
    private String robotId;  // Robot 엔티티와 연결
    
    @Column(name = "line_id", nullable = false)
    private Long lineId;  // 어느 라인에 속하는지
    
    // Unity 3D 좌표
    @Column(name = "position_x")
    private Double positionX = 0.0;
    
    @Column(name = "position_y")
    private Double positionY = 0.0;
    
    @Column(name = "position_z")
    private Double positionZ = 0.0;
    
    @Column(name = "battery_level")
    private Integer batteryLevel = 100;  // 배터리 레벨 (0-100%)
    
    @Column(name = "current_action")
    private String currentAction = "IDLE";  // IDLE, MOVING, WORKING, CHARGING
    
    @Column(name = "target_x")
    private Double targetX;  // 목표 좌표 (이동 중일 때)
    
    @Column(name = "target_y")
    private Double targetY;
    
    @Column(name = "target_z")
    private Double targetZ;
    
    @Column(name = "movement_speed")
    private Double movementSpeed = 1.0;  // 이동 속도
    
    @Column(name = "is_active")
    private Boolean isActive = true;
    
    @Column(name = "last_action_time")
    private LocalDateTime lastActionTime;
    
    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Robot과의 관계 (1:1)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "robot_id", insertable = false, updatable = false)
    private Robot robot;
    
    // ProductionLine과의 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "line_id", insertable = false, updatable = false)
    private ProductionLine productionLine;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // 편의 메서드: Unity 좌표 설정
    public void setPosition(double x, double y, double z) {
        this.positionX = x;
        this.positionY = y;
        this.positionZ = z;
        this.updatedAt = LocalDateTime.now();
    }
    
    // 편의 메서드: 목표 위치 설정 (이동 시작)
    public void setTarget(double x, double y, double z) {
        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
        this.currentAction = "MOVING";
        this.lastActionTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    // 편의 메서드: 배터리 소모
    public void consumeBattery(int amount) {
        this.batteryLevel = Math.max(0, this.batteryLevel - amount);
        if (this.batteryLevel <= 20) {
            this.currentAction = "CHARGING";
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    // 편의 메서드: 작업 상태 변경
    public void changeAction(String action) {
        this.currentAction = action;
        this.lastActionTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
}