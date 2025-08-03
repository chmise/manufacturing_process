package com.u1mobis.dashboard_backend.domain.inventory.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.util.Objects;
import java.util.UUID;

public class MaterialId extends ValueObject {
    private final String value;
    
    public MaterialId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Material ID cannot be empty");
        }
        this.value = value.trim();
    }
    
    public static MaterialId generate() {
        return new MaterialId(UUID.randomUUID().toString());
    }
    
    public static MaterialId fromCode(String code) {
        return new MaterialId("MAT_" + code);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MaterialId that = (MaterialId) obj;
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