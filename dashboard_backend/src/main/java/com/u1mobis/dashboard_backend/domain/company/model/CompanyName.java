package com.u1mobis.dashboard_backend.domain.company.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.util.Objects;

public class CompanyName extends ValueObject {
    private final String value;
    
    public CompanyName(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Company name cannot be empty");
        }
        
        String trimmedValue = value.trim();
        if (trimmedValue.length() > 100) {
            throw new IllegalArgumentException("Company name cannot exceed 100 characters");
        }
        
        this.value = trimmedValue;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CompanyName that = (CompanyName) obj;
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