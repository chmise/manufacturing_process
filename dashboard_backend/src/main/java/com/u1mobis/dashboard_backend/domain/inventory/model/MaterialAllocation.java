package com.u1mobis.dashboard_backend.domain.inventory.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.time.LocalDateTime;
import java.util.Objects;

public class MaterialAllocation extends ValueObject {
    private final String workOrderId;
    private final Quantity quantity;
    private final String purpose;
    private final LocalDateTime allocatedAt;
    
    public MaterialAllocation(String workOrderId, Quantity quantity, String purpose, LocalDateTime allocatedAt) {
        if (workOrderId == null || workOrderId.trim().isEmpty()) {
            throw new IllegalArgumentException("Work order ID cannot be empty");
        }
        if (quantity == null || quantity.isEmpty()) {
            throw new IllegalArgumentException("Allocation quantity must be positive");
        }
        if (purpose == null || purpose.trim().isEmpty()) {
            throw new IllegalArgumentException("Allocation purpose cannot be empty");
        }
        
        this.workOrderId = workOrderId.trim();
        this.quantity = quantity;
        this.purpose = purpose.trim();
        this.allocatedAt = allocatedAt != null ? allocatedAt : LocalDateTime.now();
    }
    
    public String getWorkOrderId() {
        return workOrderId;
    }
    
    public Quantity getQuantity() {
        return quantity;
    }
    
    public String getPurpose() {
        return purpose;
    }
    
    public LocalDateTime getAllocatedAt() {
        return allocatedAt;
    }
    
    public boolean isForWorkOrder(String workOrderId) {
        return this.workOrderId.equals(workOrderId);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MaterialAllocation that = (MaterialAllocation) obj;
        return Objects.equals(workOrderId, that.workOrderId) &&
               Objects.equals(quantity, that.quantity) &&
               Objects.equals(purpose, that.purpose) &&
               Objects.equals(allocatedAt, that.allocatedAt);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(workOrderId, quantity, purpose, allocatedAt);
    }
    
    @Override
    public String toString() {
        return String.format("MaterialAllocation{workOrderId='%s', quantity=%s, purpose='%s', allocatedAt=%s}",
                workOrderId, quantity, purpose, allocatedAt);
    }
}