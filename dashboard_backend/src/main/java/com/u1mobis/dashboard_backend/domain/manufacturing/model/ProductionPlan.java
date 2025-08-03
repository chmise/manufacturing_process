package com.u1mobis.dashboard_backend.domain.manufacturing.model;

import com.u1mobis.dashboard_backend.shared.kernel.AggregateRoot;
import com.u1mobis.dashboard_backend.domain.manufacturing.event.ProductionPlanCreatedEvent;
import com.u1mobis.dashboard_backend.domain.manufacturing.event.ProductionPlanCompletedEvent;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class ProductionPlan extends AggregateRoot<ProductionPlanId> {
    private ProductionPlanId id;
    private ProductionLineId lineId;
    private LocalDate planDate;
    private ProductionTarget target;
    private ProductionProgress currentProgress;
    private WorkShift shift;
    private LocalDateTime startTime;
    private LocalDateTime estimatedEndTime;
    private LocalDateTime actualEndTime;
    private PlanStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    protected ProductionPlan() {
    }
    
    public ProductionPlan(ProductionPlanId id, ProductionLineId lineId, LocalDate planDate,
                         ProductionTarget target, WorkShift shift) {
        this.id = id;
        this.lineId = lineId;
        this.planDate = planDate;
        this.target = target;
        this.currentProgress = new ProductionProgress(0);
        this.shift = shift;
        this.status = PlanStatus.PLANNING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        calculateEstimatedEndTime();
        
        raiseEvent(new ProductionPlanCreatedEvent(
            LocalDateTime.now(),
            id.getValue(),
            lineId.getValue(),
            planDate,
            target.getDailyTarget()
        ));
    }
    
    public void startExecution() {
        if (status != PlanStatus.PLANNING) {
            throw new IllegalStateException("Cannot start plan from current status: " + status);
        }
        
        this.status = PlanStatus.IN_PROGRESS;
        this.startTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void updateProgress(int completedQuantity) {
        if (status != PlanStatus.IN_PROGRESS) {
            throw new IllegalStateException("Cannot update progress when not in progress");
        }
        
        this.currentProgress = new ProductionProgress(
            Math.min(100, (completedQuantity * 100) / target.getDailyTarget())
        );
        this.updatedAt = LocalDateTime.now();
        
        // 목표 달성 시 자동 완료
        if (completedQuantity >= target.getDailyTarget()) {
            completePlan();
        }
    }
    
    public void completePlan() {
        if (status == PlanStatus.COMPLETED) {
            return; // 이미 완료됨
        }
        
        this.status = PlanStatus.COMPLETED;
        this.actualEndTime = LocalDateTime.now();
        this.currentProgress = new ProductionProgress(100);
        this.updatedAt = LocalDateTime.now();
        
        raiseEvent(new ProductionPlanCompletedEvent(
            LocalDateTime.now(),
            id.getValue(),
            lineId.getValue(),
            planDate,
            isCompletedOnTime()
        ));
    }
    
    public void cancelPlan(String reason) {
        if (status == PlanStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel completed plan");
        }
        
        this.status = PlanStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }
    
    private void calculateEstimatedEndTime() {
        if (target != null && startTime != null) {
            double hoursNeeded = (double) target.getDailyTarget() / target.getProductionRate();
            this.estimatedEndTime = startTime.plusMinutes((long) (hoursNeeded * 60));
        }
    }
    
    public boolean isCompletedOnTime() {
        if (actualEndTime == null || estimatedEndTime == null) {
            return false;
        }
        return !actualEndTime.isAfter(estimatedEndTime);
    }
    
    public boolean isDelayed() {
        if (status == PlanStatus.COMPLETED) {
            return !isCompletedOnTime();
        }
        return estimatedEndTime != null && LocalDateTime.now().isAfter(estimatedEndTime);
    }
    
    public double getProgressPercentage() {
        return currentProgress.getPercentage();
    }
    
    @Override
    public ProductionPlanId getId() {
        return id;
    }
    
    public ProductionLineId getLineId() {
        return lineId;
    }
    
    public LocalDate getPlanDate() {
        return planDate;
    }
    
    public ProductionTarget getTarget() {
        return target;
    }
    
    public ProductionProgress getCurrentProgress() {
        return currentProgress;
    }
    
    public WorkShift getShift() {
        return shift;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public LocalDateTime getEstimatedEndTime() {
        return estimatedEndTime;
    }
    
    public LocalDateTime getActualEndTime() {
        return actualEndTime;
    }
    
    public PlanStatus getStatus() {
        return status;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}