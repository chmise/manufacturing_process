package com.u1mobis.dashboard_backend.domain.inventory.event;

import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class KeyMaterialReleasedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String materialId;
    private final String companyId;
    private final int releasedQuantity;
    private final String orderId;
    private final String reason;
    
    public KeyMaterialReleasedEvent(LocalDateTime occurredOn, String materialId, String companyId,
                                  int releasedQuantity, String orderId, String reason) {
        this.occurredOn = occurredOn;
        this.materialId = materialId;
        this.companyId = companyId;
        this.releasedQuantity = releasedQuantity;
        this.orderId = orderId;
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
    
    public int getReleasedQuantity() {
        return releasedQuantity;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public String getReason() {
        return reason;
    }
}