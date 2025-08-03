package com.u1mobis.dashboard_backend.domain.manufacturing.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.util.Objects;

public class Quantity extends ValueObject {
    private final Integer value;
    private final String unit;
    
    public Quantity(Integer value, String unit) {
        if (value == null || value < 0) {
            throw new IllegalArgumentException("Quantity must be non-negative");
        }
        if (unit == null || unit.trim().isEmpty()) {
            throw new IllegalArgumentException("Unit cannot be empty");
        }
        
        this.value = value;
        this.unit = unit.trim();
    }
    
    public Quantity(Integer value) {
        this(value, "개");
    }
    
    public Integer getValue() {
        return value;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public Quantity add(Quantity other) {
        if (!this.unit.equals(other.unit)) {
            throw new IllegalArgumentException("Cannot add quantities with different units");
        }
        return new Quantity(this.value + other.value, this.unit);
    }
    
    public Quantity subtract(Quantity other) {
        if (!this.unit.equals(other.unit)) {
            throw new IllegalArgumentException("Cannot subtract quantities with different units");
        }
        int result = this.value - other.value;
        if (result < 0) {
            throw new IllegalArgumentException("Result quantity cannot be negative");
        }
        return new Quantity(result, this.unit);
    }
    
    public boolean isGreaterThan(Quantity other) {
        if (!this.unit.equals(other.unit)) {
            throw new IllegalArgumentException("Cannot compare quantities with different units");
        }
        return this.value > other.value;
    }
    
    public boolean isZero() {
        return value == 0;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Quantity quantity = (Quantity) obj;
        return Objects.equals(value, quantity.value) && Objects.equals(unit, quantity.unit);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value, unit);
    }
    
    @Override
    public String toString() {
        return value + " " + unit;
    }
}