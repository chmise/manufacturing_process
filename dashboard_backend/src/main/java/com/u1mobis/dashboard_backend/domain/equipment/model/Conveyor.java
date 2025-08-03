package com.u1mobis.dashboard_backend.domain.equipment.model;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.manufacturing.model.ProductionLineId;
import com.u1mobis.dashboard_backend.shared.kernel.AggregateRoot;
import com.u1mobis.dashboard_backend.domain.equipment.event.ConveyorStartedEvent;
import com.u1mobis.dashboard_backend.domain.equipment.event.ConveyorStoppedEvent;
import com.u1mobis.dashboard_backend.domain.equipment.event.ConveyorEmergencyStopEvent;

import java.time.LocalDateTime;

public class Conveyor extends AggregateRoot<EquipmentId> {
    private EquipmentId id;
    private String name;
    private CompanyId companyId;
    private ProductionLineId lineId;
    private EquipmentStatus status;
    private ConveyorSpeed speed;
    private ConveyorDirection direction;
    private boolean sensorActive;
    private boolean emergencyStop;
    private boolean maintenanceMode;
    private long totalRuntimeSeconds;
    private LocalDateTime lastCommandTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    protected Conveyor() {
    }
    
    public Conveyor(EquipmentId id, String name, CompanyId companyId, ProductionLineId lineId) {
        this.id = id;
        this.name = name;
        this.companyId = companyId;
        this.lineId = lineId;
        this.status = EquipmentStatus.STOPPED;
        this.speed = ConveyorSpeed.normal();
        this.direction = ConveyorDirection.FORWARD;
        this.sensorActive = false;
        this.emergencyStop = false;
        this.maintenanceMode = false;
        this.totalRuntimeSeconds = 0L;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void start(String reason) {
        if (emergencyStop) {
            throw new IllegalStateException("Cannot start conveyor during emergency stop");
        }
        if (maintenanceMode) {
            throw new IllegalStateException("Cannot start conveyor during maintenance");
        }
        if (!status.canStart()) {
            throw new IllegalStateException("Cannot start conveyor from current status: " + status);
        }
        
        EquipmentStatus oldStatus = this.status;
        this.status = EquipmentStatus.OPERATIONAL;
        this.lastCommandTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        raiseEvent(new ConveyorStartedEvent(
            LocalDateTime.now(),
            id.getValue(),
            companyId.getValue(),
            direction,
            speed.getValue()
        ));
    }
    
    public void stop(String reason) {
        if (!status.canStop()) {
            throw new IllegalStateException("Cannot stop conveyor from current status: " + status);
        }
        
        EquipmentStatus oldStatus = this.status;
        this.status = EquipmentStatus.STOPPED;
        this.lastCommandTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        raiseEvent(new ConveyorStoppedEvent(
            LocalDateTime.now(),
            id.getValue(),
            companyId.getValue(),
            reason
        ));
    }
    
    public void emergencyStop(String reason) {
        EquipmentStatus oldStatus = this.status;
        this.status = EquipmentStatus.EMERGENCY_STOP;
        this.emergencyStop = true;
        this.lastCommandTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        raiseEvent(new ConveyorEmergencyStopEvent(
            LocalDateTime.now(),
            id.getValue(),
            companyId.getValue(),
            reason
        ));
    }
    
    public void changeSpeed(double speedValue) {
        if (status != EquipmentStatus.OPERATIONAL) {
            throw new IllegalStateException("Cannot change speed when not operational");
        }
        
        this.speed = new ConveyorSpeed(speedValue);
        this.updatedAt = LocalDateTime.now();
    }
    
    public void changeDirection(ConveyorDirection newDirection) {
        if (status != EquipmentStatus.OPERATIONAL) {
            throw new IllegalStateException("Cannot change direction when not operational");
        }
        
        this.direction = newDirection;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void activateSensor() {
        this.sensorActive = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void deactivateSensor() {
        this.sensorActive = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void enterMaintenanceMode() {
        if (status == EquipmentStatus.OPERATIONAL) {
            stop("Entering maintenance mode");
        }
        
        this.maintenanceMode = true;
        this.status = EquipmentStatus.MAINTENANCE;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void exitMaintenanceMode() {
        this.maintenanceMode = false;
        this.status = EquipmentStatus.STOPPED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void resetEmergencyStop() {
        this.emergencyStop = false;
        this.status = EquipmentStatus.STOPPED;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void addRuntime(long seconds) {
        this.totalRuntimeSeconds += seconds;
        this.updatedAt = LocalDateTime.now();
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
    
    public EquipmentStatus getStatus() {
        return status;
    }
    
    public ConveyorSpeed getSpeed() {
        return speed;
    }
    
    public ConveyorDirection getDirection() {
        return direction;
    }
    
    public boolean isSensorActive() {
        return sensorActive;
    }
    
    public boolean isEmergencyStop() {
        return emergencyStop;
    }
    
    public boolean isMaintenanceMode() {
        return maintenanceMode;
    }
    
    public long getTotalRuntimeSeconds() {
        return totalRuntimeSeconds;
    }
    
    public LocalDateTime getLastCommandTime() {
        return lastCommandTime;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}