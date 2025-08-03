package com.u1mobis.dashboard_backend.domain.manufacturing.event;

import com.u1mobis.dashboard_backend.domain.manufacturing.model.ProductionStatus;
import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class ProductionScheduleStatusChangedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String scheduleId;
    private final ProductionStatus oldStatus;
    private final ProductionStatus newStatus;
    
    public ProductionScheduleStatusChangedEvent(LocalDateTime occurredOn, String scheduleId, 
                                              ProductionStatus oldStatus, ProductionStatus newStatus) {
        this.occurredOn = occurredOn;
        this.scheduleId = scheduleId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String aggregateId() {
        return scheduleId;
    }
    
    public ProductionStatus getOldStatus() {
        return oldStatus;
    }
    
    public ProductionStatus getNewStatus() {
        return newStatus;
    }
}