package com.u1mobis.dashboard_backend.domain.manufacturing.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.util.Objects;

public class ProductionProgress extends ValueObject {
    private final int percentage;
    
    public ProductionProgress(int percentage) {
        if (percentage < 0 || percentage > 100) {
            throw new IllegalArgumentException("Progress percentage must be between 0 and 100");
        }
        this.percentage = percentage;
    }
    
    public int getPercentage() {
        return percentage;
    }
    
    public boolean isStarted() {
        return percentage > 0;
    }
    
    public boolean isCompleted() {
        return percentage == 100;
    }
    
    public boolean isInProgress() {
        return percentage > 0 && percentage < 100;
    }
    
    public ProductionProgress advance(int additionalPercentage) {
        int newPercentage = Math.min(100, this.percentage + additionalPercentage);
        return new ProductionProgress(newPercentage);
    }
    
    public ProductionProgress setTo(int newPercentage) {
        return new ProductionProgress(newPercentage);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProductionProgress that = (ProductionProgress) obj;
        return percentage == that.percentage;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(percentage);
    }
    
    @Override
    public String toString() {
        return percentage + "%";
    }
}