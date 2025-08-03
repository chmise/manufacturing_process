package com.u1mobis.dashboard_backend.domain.inventory.event;

import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class StockLowEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String materialId;
    private final String companyId;
    private final String materialName;
    private final int currentQuantity;
    private final int minimumThreshold;
    
    public StockLowEvent(LocalDateTime occurredOn, String materialId, String companyId,
                        String materialName, int currentQuantity, int minimumThreshold) {
        this.occurredOn = occurredOn;
        this.materialId = materialId;
        this.companyId = companyId;
        this.materialName = materialName;
        this.currentQuantity = currentQuantity;
        this.minimumThreshold = minimumThreshold;
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
    
    public String getMaterialName() {
        return materialName;
    }
    
    public int getCurrentQuantity() {
        return currentQuantity;
    }
    
    public int getMinimumThreshold() {
        return minimumThreshold;
    }
}