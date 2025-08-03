package com.u1mobis.dashboard_backend.domain.manufacturing.model;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.shared.kernel.AggregateRoot;
import com.u1mobis.dashboard_backend.domain.manufacturing.event.ProductionScheduleCreatedEvent;
import com.u1mobis.dashboard_backend.domain.manufacturing.event.ProductionScheduleStatusChangedEvent;

import java.time.LocalDateTime;

public class ProductionSchedule extends AggregateRoot<ProductionScheduleId> {
    private ProductionScheduleId id;
    private CompanyId companyId;
    private ProductName productName;
    private String productCode;
    private LocalDateTime startDateTime;
    private LocalDateTime endDateTime;
    private Quantity targetQuantity;
    private Quantity actualQuantity;
    private ProductionStatus status;
    private Integer priority;
    private String assignedWorker;
    private String equipmentLine;
    private String description;
    private String delayReason;
    private LocalDateTime actualStartTime;
    private LocalDateTime actualEndTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private boolean isSample;
    
    protected ProductionSchedule() {
    }
    
    public ProductionSchedule(CompanyId companyId, ProductName productName, 
                            LocalDateTime startDateTime, LocalDateTime endDateTime,
                            Quantity targetQuantity, Integer priority,
                            String assignedWorker, String equipmentLine) {
        this.companyId = companyId;
        this.productName = productName;
        this.startDateTime = startDateTime;
        this.endDateTime = endDateTime;
        this.targetQuantity = targetQuantity;
        this.actualQuantity = new Quantity(0, targetQuantity.getUnit());
        this.status = ProductionStatus.SCHEDULED;
        this.priority = priority != null ? priority : 2;
        this.assignedWorker = assignedWorker;
        this.equipmentLine = equipmentLine;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        this.isSample = false;
        
        raiseEvent(new ProductionScheduleCreatedEvent(
            LocalDateTime.now(),
            null, // ID will be set after persistence
            companyId.getValue(),
            productName.getValue()
        ));
    }
    
    public void setId(ProductionScheduleId id) {
        this.id = id;
    }
    
    public void startProduction() {
        if (!status.canTransitionTo(ProductionStatus.IN_PROGRESS)) {
            throw new IllegalStateException("Cannot start production from current status: " + status);
        }
        
        ProductionStatus oldStatus = this.status;
        this.status = ProductionStatus.IN_PROGRESS;
        this.actualStartTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        raiseEvent(new ProductionScheduleStatusChangedEvent(
            LocalDateTime.now(),
            id != null ? id.getValue() : null,
            oldStatus,
            ProductionStatus.IN_PROGRESS
        ));
    }
    
    public void pauseProduction(String reason) {
        if (!status.canTransitionTo(ProductionStatus.PAUSED)) {
            throw new IllegalStateException("Cannot pause production from current status: " + status);
        }
        
        ProductionStatus oldStatus = this.status;
        this.status = ProductionStatus.PAUSED;
        this.delayReason = reason;
        this.updatedAt = LocalDateTime.now();
        
        raiseEvent(new ProductionScheduleStatusChangedEvent(
            LocalDateTime.now(),
            id != null ? id.getValue() : null,
            oldStatus,
            ProductionStatus.PAUSED
        ));
    }
    
    public void completeProduction(Quantity actualQuantity) {
        if (!status.canTransitionTo(ProductionStatus.COMPLETED)) {
            throw new IllegalStateException("Cannot complete production from current status: " + status);
        }
        
        if (!actualQuantity.getUnit().equals(this.targetQuantity.getUnit())) {
            throw new IllegalArgumentException("Actual quantity unit must match target quantity unit");
        }
        
        ProductionStatus oldStatus = this.status;
        this.status = ProductionStatus.COMPLETED;
        this.actualQuantity = actualQuantity;
        this.actualEndTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        raiseEvent(new ProductionScheduleStatusChangedEvent(
            LocalDateTime.now(),
            id != null ? id.getValue() : null,
            oldStatus,
            ProductionStatus.COMPLETED
        ));
    }
    
    public void cancelProduction(String reason) {
        if (!status.canTransitionTo(ProductionStatus.CANCELLED)) {
            throw new IllegalStateException("Cannot cancel production from current status: " + status);
        }
        
        ProductionStatus oldStatus = this.status;
        this.status = ProductionStatus.CANCELLED;
        this.delayReason = reason;
        this.updatedAt = LocalDateTime.now();
        
        raiseEvent(new ProductionScheduleStatusChangedEvent(
            LocalDateTime.now(),
            id != null ? id.getValue() : null,
            oldStatus,
            ProductionStatus.CANCELLED
        ));
    }
    
    public void updateProgress(Quantity currentQuantity) {
        if (status != ProductionStatus.IN_PROGRESS) {
            throw new IllegalStateException("Can only update progress for in-progress production");
        }
        
        if (!currentQuantity.getUnit().equals(this.targetQuantity.getUnit())) {
            throw new IllegalArgumentException("Current quantity unit must match target quantity unit");
        }
        
        if (currentQuantity.isGreaterThan(this.targetQuantity)) {
            throw new IllegalArgumentException("Current quantity cannot exceed target quantity");
        }
        
        this.actualQuantity = currentQuantity;
        this.updatedAt = LocalDateTime.now();
    }
    
    public double getProgressPercentage() {
        if (targetQuantity.isZero()) {
            return 0.0;
        }
        return (double) actualQuantity.getValue() / targetQuantity.getValue() * 100.0;
    }
    
    public boolean isDelayed() {
        if (status == ProductionStatus.COMPLETED) {
            return actualEndTime != null && actualEndTime.isAfter(endDateTime);
        }
        return LocalDateTime.now().isAfter(endDateTime) && !status.isCompleted();
    }
    
    public boolean isOnSchedule() {
        return !isDelayed();
    }
    
    @Override
    public ProductionScheduleId getId() {
        return id;
    }
    
    public CompanyId getCompanyId() {
        return companyId;
    }
    
    public ProductName getProductName() {
        return productName;
    }
    
    public String getProductCode() {
        return productCode;
    }
    
    public void setProductCode(String productCode) {
        this.productCode = productCode;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getStartDateTime() {
        return startDateTime;
    }
    
    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }
    
    public Quantity getTargetQuantity() {
        return targetQuantity;
    }
    
    public Quantity getActualQuantity() {
        return actualQuantity;
    }
    
    public ProductionStatus getStatus() {
        return status;
    }
    
    public Integer getPriority() {
        return priority;
    }
    
    public String getAssignedWorker() {
        return assignedWorker;
    }
    
    public String getEquipmentLine() {
        return equipmentLine;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getDelayReason() {
        return delayReason;
    }
    
    public LocalDateTime getActualStartTime() {
        return actualStartTime;
    }
    
    public LocalDateTime getActualEndTime() {
        return actualEndTime;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
    
    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
    
    public boolean isSample() {
        return isSample;
    }
    
    public void markAsSample() {
        this.isSample = true;
    }
}