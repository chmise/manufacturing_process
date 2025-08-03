package com.u1mobis.dashboard_backend.domain.iot.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.time.LocalDateTime;
import java.util.Objects;

public class SensorValue extends ValueObject {
    private final double value;
    private final String unit;
    private final LocalDateTime timestamp;
    private final SensorStatus status;
    
    public SensorValue(double value, String unit, LocalDateTime timestamp, SensorStatus status) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException("Sensor value cannot be NaN or infinite");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp cannot be null");
        }
        
        this.value = value;
        this.unit = unit != null ? unit.trim() : "";
        this.timestamp = timestamp;
        this.status = status != null ? status : SensorStatus.NORMAL;
    }
    
    public static SensorValue normal(double value, String unit) {
        return new SensorValue(value, unit, LocalDateTime.now(), SensorStatus.NORMAL);
    }
    
    public static SensorValue warning(double value, String unit) {
        return new SensorValue(value, unit, LocalDateTime.now(), SensorStatus.WARNING);
    }
    
    public static SensorValue critical(double value, String unit) {
        return new SensorValue(value, unit, LocalDateTime.now(), SensorStatus.CRITICAL);
    }
    
    public static SensorValue error(String unit) {
        return new SensorValue(Double.NaN, unit, LocalDateTime.now(), SensorStatus.ERROR);
    }
    
    public double getValue() {
        return value;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public SensorStatus getStatus() {
        return status;
    }
    
    public boolean isValid() {
        return !Double.isNaN(value) && status != SensorStatus.ERROR;
    }
    
    public boolean isInRange(double min, double max) {
        return isValid() && value >= min && value <= max;
    }
    
    public boolean isAboveThreshold(double threshold) {
        return isValid() && value > threshold;
    }
    
    public boolean isBelowThreshold(double threshold) {
        return isValid() && value < threshold;
    }
    
    public boolean requiresAttention() {
        return status == SensorStatus.WARNING || status == SensorStatus.CRITICAL || status == SensorStatus.ERROR;
    }
    
    public String getFormattedValue() {
        if (!isValid()) {
            return "N/A";
        }
        return String.format("%.2f %s", value, unit);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SensorValue that = (SensorValue) obj;
        return Double.compare(that.value, value) == 0 &&
               Objects.equals(unit, that.unit) &&
               Objects.equals(timestamp, that.timestamp) &&
               status == that.status;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value, unit, timestamp, status);
    }
    
    @Override
    public String toString() {
        return String.format("SensorValue{value=%.2f %s, status=%s, timestamp=%s}", 
                value, unit, status, timestamp);
    }
}