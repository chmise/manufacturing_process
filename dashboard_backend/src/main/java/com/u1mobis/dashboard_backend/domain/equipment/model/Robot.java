package com.u1mobis.dashboard_backend.domain.equipment.model;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.manufacturing.model.Position;
import com.u1mobis.dashboard_backend.domain.manufacturing.model.ProductionLineId;
import com.u1mobis.dashboard_backend.shared.kernel.AggregateRoot;
import com.u1mobis.dashboard_backend.domain.equipment.event.RobotStatusChangedEvent;
import com.u1mobis.dashboard_backend.domain.equipment.event.RobotWorkCompletedEvent;

import java.time.LocalDateTime;

public class Robot extends AggregateRoot<EquipmentId> {
    private EquipmentId id;
    private String name;
    private CompanyId companyId;
    private ProductionLineId lineId;
    private RobotType type;
    private EquipmentStatus status;
    private Position position;
    private RobotConfiguration configuration;
    private OperationalMetrics metrics;
    private LocalDateTime lastMaintenanceDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    protected Robot() {
    }
    
    public Robot(EquipmentId id, String name, CompanyId companyId, ProductionLineId lineId, 
                RobotType type, Position position) {
        this.id = id;
        this.name = name;
        this.companyId = companyId;
        this.lineId = lineId;
        this.type = type;
        this.position = position;
        this.status = EquipmentStatus.STOPPED;
        this.configuration = new RobotConfiguration();
        this.metrics = new OperationalMetrics();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void start() {
        if (!status.canStart()) {
            throw new IllegalStateException("Cannot start robot from current status: " + status);
        }
        
        EquipmentStatus oldStatus = this.status;
        this.status = EquipmentStatus.OPERATIONAL;
        this.updatedAt = LocalDateTime.now();
        
        raiseEvent(new RobotStatusChangedEvent(
            LocalDateTime.now(),
            id.getValue(),
            companyId.getValue(),
            oldStatus,
            EquipmentStatus.OPERATIONAL
        ));
    }
    
    public void stop(String reason) {
        if (!status.canStop()) {
            throw new IllegalStateException("Cannot stop robot from current status: " + status);
        }
        
        EquipmentStatus oldStatus = this.status;
        this.status = EquipmentStatus.STOPPED;
        this.updatedAt = LocalDateTime.now();
        
        raiseEvent(new RobotStatusChangedEvent(
            LocalDateTime.now(),
            id.getValue(),
            companyId.getValue(),
            oldStatus,
            EquipmentStatus.STOPPED
        ));
    }
    
    public void emergencyStop(String reason) {
        EquipmentStatus oldStatus = this.status;
        this.status = EquipmentStatus.EMERGENCY_STOP;
        this.updatedAt = LocalDateTime.now();
        
        raiseEvent(new RobotStatusChangedEvent(
            LocalDateTime.now(),
            id.getValue(),
            companyId.getValue(),
            oldStatus,
            EquipmentStatus.EMERGENCY_STOP
        ));
    }
    
    public void moveTo(Position newPosition) {
        if (status != EquipmentStatus.OPERATIONAL) {
            throw new IllegalStateException("Cannot move robot when not operational");
        }
        
        this.position = newPosition;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void completeWork(String workType) {
        if (status != EquipmentStatus.OPERATIONAL) {
            throw new IllegalStateException("Cannot complete work when not operational");
        }
        
        metrics.incrementProductionCount();
        this.updatedAt = LocalDateTime.now();
        
        raiseEvent(new RobotWorkCompletedEvent(
            LocalDateTime.now(),
            id.getValue(),
            companyId.getValue(),
            workType
        ));
    }
    
    public void updateConfiguration(double cycleTime, int motorSpeed, boolean ledEnabled) {
        this.configuration = new RobotConfiguration(cycleTime, motorSpeed, ledEnabled);
        this.updatedAt = LocalDateTime.now();
    }
    
    public void performMaintenance() {
        EquipmentStatus oldStatus = this.status;
        this.status = EquipmentStatus.MAINTENANCE;
        this.lastMaintenanceDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        raiseEvent(new RobotStatusChangedEvent(
            LocalDateTime.now(),
            id.getValue(),
            companyId.getValue(),
            oldStatus,
            EquipmentStatus.MAINTENANCE
        ));
    }
    
    public boolean needsMaintenance() {
        if (lastMaintenanceDate == null) {
            return true;
        }
        
        // 30일 이상 경과하면 정비 필요
        return lastMaintenanceDate.isBefore(LocalDateTime.now().minusDays(30));
    }
    
    public boolean isOperational() {
        return status.isOperational();
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
    
    public RobotType getType() {
        return type;
    }
    
    public EquipmentStatus getStatus() {
        return status;
    }
    
    public Position getPosition() {
        return position;
    }
    
    public RobotConfiguration getConfiguration() {
        return configuration;
    }
    
    public OperationalMetrics getMetrics() {
        return metrics;
    }
    
    public LocalDateTime getLastMaintenanceDate() {
        return lastMaintenanceDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}