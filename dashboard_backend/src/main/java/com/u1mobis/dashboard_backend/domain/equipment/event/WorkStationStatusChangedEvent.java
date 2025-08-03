package com.u1mobis.dashboard_backend.domain.equipment.event;

import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;
import com.u1mobis.dashboard_backend.domain.equipment.model.EquipmentStatus;

import java.time.LocalDateTime;

public class WorkStationStatusChangedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String stationId;
    private final String companyId;
    private final EquipmentStatus oldStatus;
    private final EquipmentStatus newStatus;
    
    public WorkStationStatusChangedEvent(LocalDateTime occurredOn, String stationId, String companyId,
                                       EquipmentStatus oldStatus, EquipmentStatus newStatus) {
        this.occurredOn = occurredOn;
        this.stationId = stationId;
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
        return stationId;
    }
    
    public String getStationId() {
        return stationId;
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