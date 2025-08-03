package com.u1mobis.dashboard_backend.domain.manufacturing.event;

import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class ProductionScheduleCreatedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String scheduleId;
    private final String companyId;
    private final String productName;
    
    public ProductionScheduleCreatedEvent(LocalDateTime occurredOn, String scheduleId, String companyId, String productName) {
        this.occurredOn = occurredOn;
        this.scheduleId = scheduleId;
        this.companyId = companyId;
        this.productName = productName;
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String aggregateId() {
        return scheduleId;
    }
    
    public String getCompanyId() {
        return companyId;
    }
    
    public String getProductName() {
        return productName;
    }
}