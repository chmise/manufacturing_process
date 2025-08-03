package com.u1mobis.dashboard_backend.domain.inventory.event;

import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class StockReplenishedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String materialId;
    private final String companyId;
    private final int replenishedQuantity;
    private final int previousQuantity;
    private final int currentQuantity;
    private final String reason;
    
    public StockReplenishedEvent(LocalDateTime occurredOn, String materialId, String companyId,
                               int replenishedQuantity, int previousQuantity, int currentQuantity, String reason) {
        this.occurredOn = occurredOn;
        this.materialId = materialId;
        this.companyId = companyId;
        this.replenishedQuantity = replenishedQuantity;
        this.previousQuantity = previousQuantity;
        this.currentQuantity = currentQuantity;
        this.reason = reason;
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String aggregateId() {
        return materialId;
    }
    
    public String getMaterialId() {
        return materialId;
    }
    
    public String getCompanyId() {
        return companyId;
    }
    
    public int getReplenishedQuantity() {
        return replenishedQuantity;
    }
    
    public int getPreviousQuantity() {
        return previousQuantity;
    }
    
    public int getCurrentQuantity() {
        return currentQuantity;
    }
    
    public String getReason() {
        return reason;
    }
}