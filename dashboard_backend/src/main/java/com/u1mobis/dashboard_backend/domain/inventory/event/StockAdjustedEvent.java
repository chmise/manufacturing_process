package com.u1mobis.dashboard_backend.domain.inventory.event;

import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class StockAdjustedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String materialId;
    private final String companyId;
    private final int previousQuantity;
    private final int adjustedQuantity;
    private final String reason;
    
    public StockAdjustedEvent(LocalDateTime occurredOn, String materialId, String companyId,
                            int previousQuantity, int adjustedQuantity, String reason) {
        this.occurredOn = occurredOn;
        this.materialId = materialId;
        this.companyId = companyId;
        this.previousQuantity = previousQuantity;
        this.adjustedQuantity = adjustedQuantity;
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
    
    public int getPreviousQuantity() {
        return previousQuantity;
    }
    
    public int getAdjustedQuantity() {
        return adjustedQuantity;
    }
    
    public String getReason() {
        return reason;
    }
}