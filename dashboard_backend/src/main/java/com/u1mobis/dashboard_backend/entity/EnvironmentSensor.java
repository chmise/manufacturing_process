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
@Table(name = "environment_sensor")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnvironmentSensor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;           // 데이터 전송 시각
    
    @Column(name = "temperature")
    private Double temperature;                // 온도 (섭씨)
    
    @Column(name = "humidity")
    private Double humidity;                   // 습도 (%)
    
    @Column(name = "air_quality")
    private Integer airQuality;                // 공기질 지수
}

