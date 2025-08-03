package com.u1mobis.dashboard_backend.domain.equipment.event;

import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;
import com.u1mobis.dashboard_backend.domain.equipment.model.ConveyorDirection;

import java.time.LocalDateTime;

public class ConveyorStartedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String conveyorId;
    private final String companyId;
    private final ConveyorDirection direction;
    private final double speed;
    
    public ConveyorStartedEvent(LocalDateTime occurredOn, String conveyorId, String companyId,
                              ConveyorDirection direction, double speed) {
        this.occurredOn = occurredOn;
        this.conveyorId = conveyorId;
        this.companyId = companyId;
        this.direction = direction;
        this.speed = speed;
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
    
    public ConveyorDirection getDirection() {
        return direction;
    }
    
    public double getSpeed() {
        return speed;
    }
}