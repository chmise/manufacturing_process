package com.u1mobis.dashboard_backend.domain.manufacturing.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.util.Objects;

public class ProductColor extends ValueObject {
    private final String value;
    
    public ProductColor(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Product color cannot be empty");
        }
        this.value = value.trim().toUpperCase();
    }
    
    public String getValue() {
        return value;
    }
    
    public static ProductColor red() {
        return new ProductColor("RED");
    }
    
    public static ProductColor blue() {
        return new ProductColor("BLUE");
    }
    
    public static ProductColor white() {
        return new ProductColor("WHITE");
    }
    
    public static ProductColor black() {
        return new ProductColor("BLACK");
    }
    
    public static ProductColor silver() {
        return new ProductColor("SILVER");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProductColor that = (ProductColor) obj;
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