package com.u1mobis.dashboard_backend.domain.manufacturing.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.util.Objects;

public class ProductionScheduleId extends ValueObject {
    private final String value;
    
    public ProductionScheduleId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Production schedule ID cannot be empty");
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
        ProductionScheduleId that = (ProductionScheduleId) obj;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "ProductionScheduleId{" + value + "}";
    }
}