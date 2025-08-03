package com.u1mobis.dashboard_backend.domain.company.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.util.Objects;

public class IndustryTypeId extends ValueObject {
    private final String value;
    
    public IndustryTypeId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Industry type ID cannot be empty");
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
        IndustryTypeId that = (IndustryTypeId) obj;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "IndustryTypeId{" + value + "}";
    }
}