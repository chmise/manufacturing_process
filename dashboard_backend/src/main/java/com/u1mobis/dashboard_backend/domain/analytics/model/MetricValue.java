package com.u1mobis.dashboard_backend.domain.analytics.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.util.Objects;

public class MetricValue extends ValueObject {
    private final double value;
    private final String unit;
    
    public MetricValue(double value, String unit) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException("Metric value cannot be NaN or infinite");
        }
        this.value = value;
        this.unit = unit != null ? unit.trim() : "";
    }
    
    public static MetricValue percentage(double value) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("Percentage value must be between 0 and 100");
        }
        return new MetricValue(value, "%");
    }
    
    public static MetricValue count(long value) {
        if (value < 0) {
            throw new IllegalArgumentException("Count value cannot be negative");
        }
        return new MetricValue(value, "count");
    }
    
    public static MetricValue ratio(double value) {
        if (value < 0) {
            throw new IllegalArgumentException("Ratio value cannot be negative");
        }
        return new MetricValue(value, "ratio");
    }
    
    public static MetricValue time(double minutes) {
        if (minutes < 0) {
            throw new IllegalArgumentException("Time value cannot be negative");
        }
        return new MetricValue(minutes, "minutes");
    }
    
    public double getValue() {
        return value;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public boolean isPercentage() {
        return "%".equals(unit);
    }
    
    public boolean isZero() {
        return Double.compare(value, 0.0) == 0;
    }
    
    public boolean isPositive() {
        return value > 0;
    }
    
    public MetricValue add(MetricValue other) {
        if (!unit.equals(other.unit)) {
            throw new IllegalArgumentException("Cannot add metrics with different units");
        }
        return new MetricValue(this.value + other.value, this.unit);
    }
    
    public MetricValue subtract(MetricValue other) {
        if (!unit.equals(other.unit)) {
            throw new IllegalArgumentException("Cannot subtract metrics with different units");
        }
        return new MetricValue(this.value - other.value, this.unit);
    }
    
    public MetricValue multiply(double factor) {
        return new MetricValue(this.value * factor, this.unit);
    }
    
    public MetricValue divide(double divisor) {
        if (divisor == 0) {
            throw new IllegalArgumentException("Cannot divide by zero");
        }
        return new MetricValue(this.value / divisor, this.unit);
    }
    
    public String getFormattedValue() {
        if (isPercentage()) {
            return String.format("%.1f%%", value);
        } else if ("count".equals(unit)) {
            return String.format("%.0f", value);
        } else if ("minutes".equals(unit)) {
            return String.format("%.2f min", value);
        } else {
            return String.format("%.2f %s", value, unit);
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        MetricValue that = (MetricValue) obj;
        return Double.compare(that.value, value) == 0 &&
               Objects.equals(unit, that.unit);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value, unit);
    }
    
    @Override
    public String toString() {
        return getFormattedValue();
    }
}