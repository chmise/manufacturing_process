package com.u1mobis.dashboard_backend.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quality_records")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QualityRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_id", nullable = false)
    private String productId;                  // 차량 ID
    
    @Column(name = "robot_id", nullable = false)
    private String robotId;                    // 검사한 로봇 ID
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;           // 검사 시각
    
    @Column(name = "result", nullable = false)
    private String result;                     // PASS/FAIL
    
    @Column(name = "defect_reason")
    private String defectReason;               // 불량 사유
    
    @Column(name = "torque_value")
    private Double torqueValue;                // 검사 시 토크값
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", insertable = false, updatable = false)
    private CurrentProduction currentProduction;
}
