package com.u1mobis.dashboard_backend.domain.inventory.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.util.Objects;

public class Quantity extends ValueObject {
    private final int value;
    
    public Quantity(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.value = value;
    }
    
    public static Quantity zero() {
        return new Quantity(0);
    }
    
    public static Quantity of(int value) {
        return new Quantity(value);
    }
    
    public int getValue() {
        return value;
    }
    
    public boolean isEmpty() {
        return value == 0;
    }
    
    public boolean isPositive() {
        return value > 0;
    }
    
    public boolean isGreaterThan(Quantity other) {
        return this.value > other.value;
    }
    
    public boolean isLessThan(Quantity other) {
        return this.value < other.value;
    }
    
    public boolean isGreaterThanOrEqual(Quantity other) {
        return this.value >= other.value;
    }
    
    public Quantity add(Quantity other) {
        return new Quantity(this.value + other.value);
    }
    
    public Quantity subtract(Quantity other) {
        if (this.value < other.value) {
            throw new IllegalArgumentException("Cannot subtract quantity that results in negative value");
        }
        return new Quantity(this.value - other.value);
    }
    
    public Quantity multiply(int factor) {
        if (factor < 0) {
            throw new IllegalArgumentException("Factor cannot be negative");
        }
        return new Quantity(this.value * factor);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Quantity quantity = (Quantity) obj;
        return value == quantity.value;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return String.valueOf(value);
    }
}