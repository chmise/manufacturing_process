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
@Table(name = "robot")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Robot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "robot_id")
    private Long robotId;
    
    @Column(name = "robot_name", nullable = false)
    private String robotName;
    
    @Column(name = "company_id", nullable = false)
    private Long companyId;
    
    @Column(name = "station_code")
    private String stationCode;           // 공정 코드 (A01, B02 등)
    
    @Column(name = "robot_type")
    private String robotType;             // 로봇 타입 (KUKA, ABB, Fanuc 등)
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}