package com.u1mobis.dashboard_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "station_status")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StationStatus {
    
    @Id
    @Column(name = "station_id")
    private String stationId;  // "DoorStation", "WaterLeakTestStation" 등
    
    @Column(name = "station_name", nullable = false)
    private String stationName;  // 스테이션 이름
    
    @Column(name = "line_id", nullable = false)
    private Long lineId;  // 어느 라인에 속하는지
    
    @Column(name = "status", nullable = false)
    private String status = "IDLE";  // OPERATING, IDLE, MAINTENANCE, ERROR
    
    @Column(name = "temperature")
    private Double temperature = 25.0;  // 스테이션 온도
    
    @Column(name = "pressure")
    private Double pressure = 1.0;  // 압력
    
    @Column(name = "efficiency")
    private Double efficiency = 85.0;  // 효율 (80-95% 범위)
    
    @Column(name = "equipment_status")
    private String equipmentStatus = "NORMAL";  // NORMAL, WARNING, ERROR
    
    @Column(name = "current_product")
    private String currentProduct;  // 현재 작업 중인 제품 ID
    
    @Column(name = "cycle_time")
    private Integer cycleTime = 120;  // 표준 사이클 타임 (초)
    
    @Column(name = "last_maintenance")
    private LocalDateTime lastMaintenance;
    
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
    
    // 편의 메서드: 효율 업데이트 (80-95% 범위 유지)
    public void updateEfficiency(double newEfficiency) {
        this.efficiency = Math.max(80.0, Math.min(95.0, newEfficiency));
        this.updatedAt = LocalDateTime.now();
    }
    
    // 편의 메서드: 온도 업데이트 (20-40°C 범위)
    public void updateTemperature(double newTemp) {
        this.temperature = Math.max(20.0, Math.min(40.0, newTemp));
        this.updatedAt = LocalDateTime.now();
    }
    
    // 편의 메서드: 스테이션 상태 변경
    public void changeStatus(String newStatus, String currentProductId) {
        this.status = newStatus;
        this.currentProduct = currentProductId;
        this.updatedAt = LocalDateTime.now();
    }
}