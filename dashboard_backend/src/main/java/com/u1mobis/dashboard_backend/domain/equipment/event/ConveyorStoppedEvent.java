package com.u1mobis.dashboard_backend.domain.equipment.event;

import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class ConveyorStoppedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String conveyorId;
    private final String companyId;
    private final String reason;
    
    public ConveyorStoppedEvent(LocalDateTime occurredOn, String conveyorId, String companyId, String reason) {
        this.occurredOn = occurredOn;
        this.conveyorId = conveyorId;
        this.companyId = companyId;
        this.reason = reason;
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String aggregateId() {
        return conveyorId;
    }
    
    public String getConveyorId() {
        return conveyorId;
    }
    
    public String getCompanyId() {
        return companyId;
    }
    
    public String getReason() {
        return reason;
    }
}