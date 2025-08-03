package com.u1mobis.dashboard_backend.domain.equipment.model;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.manufacturing.model.ProductionLineId;
import com.u1mobis.dashboard_backend.shared.kernel.AggregateRoot;
import com.u1mobis.dashboard_backend.domain.equipment.event.WorkStationStatusChangedEvent;

import java.time.LocalDateTime;

public class WorkStation extends AggregateRoot<EquipmentId> {
    private EquipmentId id;
    private String name;
    private CompanyId companyId;
    private ProductionLineId lineId;
    private WorkStationType type;
    private EquipmentStatus status;
    private StationWorkMode workMode;
    private int queueCapacity;
    private int currentQueueSize;
    private double cycleTime;
    private long totalProcessedCount;
    private LocalDateTime lastProcessTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    protected WorkStation() {
    }
    
    public WorkStation(EquipmentId id, String name, CompanyId companyId, ProductionLineId lineId,
                      WorkStationType type, int queueCapacity, double cycleTime) {
        this.id = id;
        this.name = name;
        this.companyId = companyId;
        this.lineId = lineId;
        this.type = type;
        this.status = EquipmentStatus.STOPPED;
        this.workMode = StationWorkMode.MANUAL;
        this.queueCapacity = queueCapacity;
        this.currentQueueSize = 0;
        this.cycleTime = cycleTime;
        this.totalProcessedCount = 0L;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void start() {
        if (!status.canStart()) {
            throw new IllegalStateException("Cannot start work station from current status: " + status);
        }
        
        EquipmentStatus oldStatus = this.status;
        this.status = EquipmentStatus.OPERATIONAL;
        this.updatedAt = LocalDateTime.now();
        
        raiseEvent(new WorkStationStatusChangedEvent(
            LocalDateTime.now(),
            id.getValue(),
            companyId.getValue(),
            oldStatus,
            EquipmentStatus.OPERATIONAL
        ));
    }
    
    public void stop(String reason) {
        if (!status.canStop()) {
            throw new IllegalStateException("Cannot stop work station from current status: " + status);
        }
        
        EquipmentStatus oldStatus = this.status;
        this.status = EquipmentStatus.STOPPED;
        this.updatedAt = LocalDateTime.now();
        
        raiseEvent(new WorkStationStatusChangedEvent(
            LocalDateTime.now(),
            id.getValue(),
            companyId.getValue(),
            oldStatus,
            EquipmentStatus.STOPPED
        ));
    }
    
    public void processItem() {
        if (status != EquipmentStatus.OPERATIONAL) {
            throw new IllegalStateException("Cannot process item when not operational");
        }
        
        if (currentQueueSize == 0) {
            throw new IllegalStateException("No items in queue to process");
        }
        
        this.currentQueueSize--;
        this.totalProcessedCount++;
        this.lastProcessTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void addItemToQueue() {
        if (currentQueueSize >= queueCapacity) {
            throw new IllegalStateException("Queue is at maximum capacity");
        }
        
        this.currentQueueSize++;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setWorkMode(StationWorkMode newWorkMode) {
        this.workMode = newWorkMode;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void updateCycleTime(double newCycleTime) {
        if (newCycleTime <= 0) {
            throw new IllegalArgumentException("Cycle time must be positive");
        }
        
        this.cycleTime = newCycleTime;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean canAcceptMoreItems() {
        return currentQueueSize < queueCapacity;
    }
    
    public boolean hasItemsToProcess() {
        return currentQueueSize > 0;
    }
    
    public double getUtilizationRate() {
        return queueCapacity > 0 ? (double) currentQueueSize / queueCapacity : 0.0;
    }
    
    @Override
    public EquipmentId getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public CompanyId getCompanyId() {
        return companyId;
    }
    
    public ProductionLineId getLineId() {
        return lineId;
    }
    
    public WorkStationType getType() {
        return type;
    }
    
    public EquipmentStatus getStatus() {
        return status;
    }
    
    public StationWorkMode getWorkMode() {
        return workMode;
    }
    
    public int getQueueCapacity() {
        return queueCapacity;
    }
    
    public int getCurrentQueueSize() {
        return currentQueueSize;
    }
    
    public double getCycleTime() {
        return cycleTime;
    }
    
    public long getTotalProcessedCount() {
        return totalProcessedCount;
    }
    
    public LocalDateTime getLastProcessTime() {
        return lastProcessTime;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}