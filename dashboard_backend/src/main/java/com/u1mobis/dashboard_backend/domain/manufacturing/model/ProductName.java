package com.u1mobis.dashboard_backend.domain.manufacturing.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.util.Objects;

public class ProductName extends ValueObject {
    private final String value;
    
    public ProductName(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        
        String trimmedValue = value.trim();
        if (trimmedValue.length() > 200) {
            throw new IllegalArgumentException("Product name cannot exceed 200 characters");
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
        ProductName that = (ProductName) obj;
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