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
@Table(name = "production_completed")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionCompleted {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "product_id", nullable = false)
    private String productId;                  // 완성된 차량 ID
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;           // 생산 완료 시각
    
    @Column(name = "cycle_time")
    private Double cycleTime;                  // 실제 차량 완성 시간 (초)
    
    @Column(name = "quality", nullable = false)
    private String quality;                    // PASS/FAIL
    
    @Column(name = "due_date")
    private LocalDateTime dueDate;             // 납기 예정일
    
    @Column(name = "is_on_time")
    private Boolean isOnTime;                  // 정시 납기 여부
    
    @Column(name = "is_first_time_pass")
    private Boolean isFirstTimePass;           // 일발 통과 여부 (rework_count = 0)
}
