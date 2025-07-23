package com.u1mobis.dashboard_backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "robot_status")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RobotStatus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "robot_status_id")
    private Long robotStatusId;
    
    @Column(name = "company_id", nullable = false)
    private Long companyId;
    
    @Column(name = "robot_id", nullable = false)
    private Long robotId;
    
    @Column(name = "motor_status", nullable = false)
    private Integer motorStatus;           // 모터 상태 (0: 정지, 1: 운전)
    
    @Column(name = "led_status", nullable = false)
    private Integer ledStatus;             // LED 상태 (0: 꺼짐, 1: 켜짐)
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;       // 상태 업데이트 시각
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}