package com.u1mobis.dashboard_backend.domain.manufacturing.event;

import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class ProductionProgressUpdatedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String productionId;
    private final int progressPercentage;
    private final String currentStation;
    
    public ProductionProgressUpdatedEvent(LocalDateTime occurredOn, String productionId, 
                                        int progressPercentage, String currentStation) {
        this.occurredOn = occurredOn;
        this.productionId = productionId;
        this.progressPercentage = progressPercentage;
        this.currentStation = currentStation;
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String aggregateId() {
        return null; // ProductionId는 String이므로 null 반환
    }
    
    public String getProductionId() {
        return productionId;
    }
    
    public int getProgressPercentage() {
        return progressPercentage;
    }
    
    public String getCurrentStation() {
        return currentStation;
    }
}