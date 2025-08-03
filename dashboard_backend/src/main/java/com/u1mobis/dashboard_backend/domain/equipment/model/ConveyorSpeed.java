package com.u1mobis.dashboard_backend.domain.equipment.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.util.Objects;

public class ConveyorSpeed extends ValueObject {
    private final double value; // m/s
    
    public ConveyorSpeed(double value) {
        if (value < 0.0 || value > 5.0) {
            throw new IllegalArgumentException("Conveyor speed must be between 0.0 and 5.0 m/s");
        }
        this.value = value;
    }
    
    public static ConveyorSpeed stopped() {
        return new ConveyorSpeed(0.0);
    }
    
    public static ConveyorSpeed slow() {
        return new ConveyorSpeed(0.5);
    }
    
    public static ConveyorSpeed normal() {
        return new ConveyorSpeed(1.0);
    }
    
    public static ConveyorSpeed fast() {
        return new ConveyorSpeed(2.0);
    }
    
    public double getValue() {
        return value;
    }
    
    public boolean isStopped() {
        return value == 0.0;
    }
    
    public boolean isSlow() {
        return value <= 0.5;
    }
    
    public boolean isFast() {
        return value >= 2.0;
    }
    
    public ConveyorSpeed increase(double increment) {
        return new ConveyorSpeed(Math.min(5.0, value + increment));
    }
    
    public ConveyorSpeed decrease(double decrement) {
        return new ConveyorSpeed(Math.max(0.0, value - decrement));
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ConveyorSpeed that = (ConveyorSpeed) obj;
        return Double.compare(that.value, value) == 0;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value + " m/s";
    }
}