package com.u1mobis.dashboard_backend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "production_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductionPlan {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "plan_id")
    private Long planId;
    
    @Column(name = "line_id", nullable = false)
    private Long lineId;  // 어느 라인의 계획인지
    
    @Column(name = "plan_date", nullable = false)
    private LocalDate planDate;  // 계획 날짜
    
    @Column(name = "daily_target")
    private Integer dailyTarget = 50;  // 일일 생산 목표
    
    @Column(name = "current_progress")
    private Integer currentProgress = 0;  // 현재 진행량
    
    @Column(name = "production_rate")
    private Integer productionRate = 12;  // 시간당 생산량
    
    @Column(name = "shift")
    private String shift = "주간";  // 근무조 (주간/야간)
    
    @Column(name = "start_time")
    private LocalDateTime startTime;  // 생산 시작 시간
    
    @Column(name = "estimated_end_time")
    private LocalDateTime estimatedEndTime;  // 예상 완료 시간
    
    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime;  // 실제 완료 시간
    
    @Column(name = "status")
    private String status = "PLANNING";  // PLANNING, IN_PROGRESS, COMPLETED, CANCELLED
    
    @Column(name = "efficiency")
    private Double efficiency = 85.0;  // 계획 대비 실제 효율
    
    @Column(name = "notes")
    private String notes;  // 비고사항
    
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
    
    // 편의 메서드: 진행률 계산 (0-100%)
    public double getProgressPercentage() {
        if (dailyTarget == null || dailyTarget == 0) {
            return 0.0;
        }
        return Math.min(100.0, (currentProgress * 100.0) / dailyTarget);
    }
    
    // 편의 메서드: 진행량 업데이트
    public void updateProgress(int newProgress) {
        this.currentProgress = Math.max(0, newProgress);
        
        // 목표 달성 시 상태 변경
        if (this.currentProgress >= this.dailyTarget && "IN_PROGRESS".equals(this.status)) {
            this.status = "COMPLETED";
            this.actualEndTime = LocalDateTime.now();
        }
        
        // 효율 계산 업데이트
        this.efficiency = calculateEfficiency();
        this.updatedAt = LocalDateTime.now();
    }
    
    // 편의 메서드: 생산 시작
    public void startProduction() {
        this.status = "IN_PROGRESS";
        this.startTime = LocalDateTime.now();
        
        // 예상 완료 시간 계산 (시간당 생산량 기준)
        if (productionRate != null && productionRate > 0) {
            int hoursNeeded = (int) Math.ceil((double) dailyTarget / productionRate);
            this.estimatedEndTime = this.startTime.plusHours(hoursNeeded);
        }
        
        this.updatedAt = LocalDateTime.now();
    }
    
    // 편의 메서드: 효율 계산
    private double calculateEfficiency() {
        if (dailyTarget == null || dailyTarget == 0) {
            return 0.0;
        }
        
        // 시간 기준 효율 계산
        if (startTime != null) {
            long hoursElapsed = java.time.Duration.between(startTime, LocalDateTime.now()).toHours();
            if (hoursElapsed > 0) {
                double expectedProgress = hoursElapsed * productionRate;
                return Math.min(100.0, (currentProgress / expectedProgress) * 100.0);
            }
        }
        
        return getProgressPercentage();
    }
}