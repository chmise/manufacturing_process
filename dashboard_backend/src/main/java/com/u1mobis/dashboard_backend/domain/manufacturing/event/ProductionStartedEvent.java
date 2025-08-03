package com.u1mobis.dashboard_backend.domain.manufacturing.event;

import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class ProductionStartedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String productionId;
    private final String companyId;
    private final String productName;
    
    public ProductionStartedEvent(LocalDateTime occurredOn, String productionId, String companyId, String productName) {
        this.occurredOn = occurredOn;
        this.productionId = productionId;
        this.companyId = companyId;
        this.productName = productName;
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String aggregateId() {
        return companyId; // Company가 aggregate root가 아니라면 null 반환 가능
    }
    
    public String getProductionId() {
        return productionId;
    }
    
    public String getCompanyId() {
        return companyId;
    }
    
    public String getProductName() {
        return productName;
    }
}