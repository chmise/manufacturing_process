package com.u1mobis.dashboard_backend.domain.manufacturing.event;

import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class ProductionCompletedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String productionId;
    private final String companyId;
    private final double cycleTime;
    private final boolean onTime;
    
    public ProductionCompletedEvent(LocalDateTime occurredOn, String productionId, String companyId, 
                                   double cycleTime, boolean onTime) {
        this.occurredOn = occurredOn;
        this.productionId = productionId;
        this.companyId = companyId;
        this.cycleTime = cycleTime;
        this.onTime = onTime;
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String aggregateId() {
        return companyId;
    }
    
    public String getProductionId() {
        return productionId;
    }
    
    public String getCompanyId() {
        return companyId;
    }
    
    public double getCycleTime() {
        return cycleTime;
    }
    
    public boolean isOnTime() {
        return onTime;
    }
}