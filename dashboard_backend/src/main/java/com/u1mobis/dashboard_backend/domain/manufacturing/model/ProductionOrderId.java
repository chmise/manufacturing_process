package com.u1mobis.dashboard_backend.domain.manufacturing.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.util.Objects;

public class ProductionOrderId extends ValueObject {
    private final String value;
    
    public ProductionOrderId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Production order ID cannot be empty");
        }
        this.value = value.trim();
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProductionOrderId that = (ProductionOrderId) obj;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}