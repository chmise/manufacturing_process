package com.u1mobis.dashboard_backend.domain.equipment.event;

import com.u1mobis.dashboard_backend.domain.equipment.model.EquipmentStatus;
import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class RobotStatusChangedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String robotId;
    private final String companyId;
    private final EquipmentStatus oldStatus;
    private final EquipmentStatus newStatus;
    
    public RobotStatusChangedEvent(LocalDateTime occurredOn, String robotId, String companyId,
                                  EquipmentStatus oldStatus, EquipmentStatus newStatus) {
        this.occurredOn = occurredOn;
        this.robotId = robotId;
        this.companyId = companyId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String aggregateId() {
        return companyId;
    }
    
    public String getRobotId() {
        return robotId;
    }
    
    public String getCompanyId() {
        return companyId;
    }
    
    public EquipmentStatus getOldStatus() {
        return oldStatus;
    }
    
    public EquipmentStatus getNewStatus() {
        return newStatus;
    }
}