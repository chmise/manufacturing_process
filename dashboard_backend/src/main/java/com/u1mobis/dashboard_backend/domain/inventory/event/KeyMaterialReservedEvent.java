package com.u1mobis.dashboard_backend.domain.inventory.event;

import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class KeyMaterialReservedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String materialId;
    private final String companyId;
    private final int reservedQuantity;
    private final String orderId;
    private final String purpose;
    
    public KeyMaterialReservedEvent(LocalDateTime occurredOn, String materialId, String companyId,
                                  int reservedQuantity, String orderId, String purpose) {
        this.occurredOn = occurredOn;
        this.materialId = materialId;
        this.companyId = companyId;
        this.reservedQuantity = reservedQuantity;
        this.orderId = orderId;
        this.purpose = purpose;
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
    
    public int getReservedQuantity() {
        return reservedQuantity;
    }
    
    public String getOrderId() {
        return orderId;
    }
    
    public String getPurpose() {
        return purpose;
    }
}