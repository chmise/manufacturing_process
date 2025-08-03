package com.u1mobis.dashboard_backend.domain.quality.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.util.Objects;

public class QualityMetric extends ValueObject {
    private final String name;
    private final double measuredValue;
    private final double expectedValue;
    private final double tolerance;
    private final String unit;
    private final boolean withinSpec;
    
    public QualityMetric(String name, double measuredValue, double expectedValue, 
                        double tolerance, String unit) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Metric name cannot be empty");
        }
        if (tolerance < 0) {
            throw new IllegalArgumentException("Tolerance cannot be negative");
        }
        
        this.name = name.trim();
        this.measuredValue = measuredValue;
        this.expectedValue = expectedValue;
        this.tolerance = tolerance;
        this.unit = unit != null ? unit.trim() : "";
        this.withinSpec = Math.abs(measuredValue - expectedValue) <= tolerance;
    }
    
    public String getName() {
        return name;
    }
    
    public double getMeasuredValue() {
        return measuredValue;
    }
    
    public double getExpectedValue() {
        return expectedValue;
    }
    
    public double getTolerance() {
        return tolerance;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public boolean isWithinSpec() {
        return withinSpec;
    }
    
    public double getDeviation() {
        return measuredValue - expectedValue;
    }
    
    public double getAbsoluteDeviation() {
        return Math.abs(getDeviation());
    }
    
    public double getDeviationPercentage() {
        return expectedValue != 0 ? (getDeviation() / expectedValue) * 100 : 0;
    }
    
    public String getFormattedValue() {
        return String.format("%.2f %s", measuredValue, unit);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        QualityMetric that = (QualityMetric) obj;
        return Double.compare(that.measuredValue, measuredValue) == 0 &&
               Double.compare(that.expectedValue, expectedValue) == 0 &&
               Double.compare(that.tolerance, tolerance) == 0 &&
               Objects.equals(name, that.name) &&
               Objects.equals(unit, that.unit);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(name, measuredValue, expectedValue, tolerance, unit);
    }
    
    @Override
    public String toString() {
        return String.format("%s: %s (Expected: %.2f ±%.2f %s) - %s", 
                name, getFormattedValue(), expectedValue, tolerance, unit,
                withinSpec ? "PASS" : "FAIL");
    }
}