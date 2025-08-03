package com.u1mobis.dashboard_backend.domain.inventory.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.util.Objects;

public class MaterialName extends ValueObject {
    private final String value;
    
    public MaterialName(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Material name cannot be empty");
        }
        if (value.length() > 100) {
            throw new IllegalArgumentException("Material name cannot exceed 100 characters");
        }
        this.value = value.trim();
    }
    
    public String getValue() {
        return value;
    }
    
    public boolean contains(String keyword) {
        return value.toLowerCase().contains(keyword.toLowerCase());
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MaterialName that = (MaterialName) obj;
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