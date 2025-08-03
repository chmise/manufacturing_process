package com.u1mobis.dashboard_backend.domain.inventory.event;

import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class KeyMaterialAllocatedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String materialId;
    private final String companyId;
    private final int allocatedQuantity;
    private final String workOrderId;
    private final String purpose;
    
    public KeyMaterialAllocatedEvent(LocalDateTime occurredOn, String materialId, String companyId,
                                   int allocatedQuantity, String workOrderId, String purpose) {
        this.occurredOn = occurredOn;
        this.materialId = materialId;
        this.companyId = companyId;
        this.allocatedQuantity = allocatedQuantity;
        this.workOrderId = workOrderId;
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
    
    public int getAllocatedQuantity() {
        return allocatedQuantity;
    }
    
    public String getWorkOrderId() {
        return workOrderId;
    }
    
    public String getPurpose() {
        return purpose;
    }
}