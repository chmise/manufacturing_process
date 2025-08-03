package com.u1mobis.dashboard_backend.domain.manufacturing.event;

import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class ProductionLineActivatedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String lineId;
    private final String companyId;
    private final String lineName;
    
    public ProductionLineActivatedEvent(LocalDateTime occurredOn, String lineId, String companyId, String lineName) {
        this.occurredOn = occurredOn;
        this.lineId = lineId;
        this.companyId = companyId;
        this.lineName = lineName;
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String aggregateId() {
        return lineId;
    }
    
    public String getLineId() {
        return lineId;
    }
    
    public String getCompanyId() {
        return companyId;
    }
    
    public String getLineName() {
        return lineName;
    }
}