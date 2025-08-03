package com.u1mobis.dashboard_backend.domain.iot.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.time.LocalDateTime;
import java.util.Objects;

public class SensorThreshold extends ValueObject {
    private final double minValue;
    private final double maxValue;
    private final double warningMin;
    private final double warningMax;
    private final double criticalMin;
    private final double criticalMax;
    private final String unit;
    private final LocalDateTime setAt;
    private final String setBy;
    
    public SensorThreshold(double minValue, double maxValue, double warningMin, double warningMax,
                          double criticalMin, double criticalMax, String unit, String setBy) {
        if (minValue >= maxValue) {
            throw new IllegalArgumentException("Min value must be less than max value");
        }
        if (warningMin < minValue || warningMax > maxValue) {
            throw new IllegalArgumentException("Warning thresholds must be within normal range");
        }
        if (criticalMin < minValue || criticalMax > maxValue) {
            throw new IllegalArgumentException("Critical thresholds must be within normal range");
        }
        
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.warningMin = warningMin;
        this.warningMax = warningMax;
        this.criticalMin = criticalMin;
        this.criticalMax = criticalMax;
        this.unit = unit != null ? unit.trim() : "";
        this.setAt = LocalDateTime.now();
        this.setBy = setBy != null ? setBy.trim() : "SYSTEM";
    }
    
    public static SensorThreshold createDefault(SensorType sensorType) {
        return switch (sensorType) {
            case TEMPERATURE -> new SensorThreshold(15.0, 35.0, 18.0, 32.0, 12.0, 38.0, "°C", "SYSTEM");
            case HUMIDITY -> new SensorThreshold(30.0, 70.0, 35.0, 65.0, 25.0, 80.0, "%", "SYSTEM");
            case AIR_QUALITY -> new SensorThreshold(0.0, 100.0, 20.0, 80.0, 10.0, 90.0, "AQI", "SYSTEM");
            case PRESSURE -> new SensorThreshold(950.0, 1050.0, 970.0, 1030.0, 930.0, 1070.0, "hPa", "SYSTEM");
            case VIBRATION -> new SensorThreshold(0.0, 10.0, 2.0, 8.0, 0.5, 12.0, "m/s²", "SYSTEM");
            case SOUND -> new SensorThreshold(30.0, 85.0, 40.0, 80.0, 25.0, 90.0, "dB", "SYSTEM");
            case MOTION -> new SensorThreshold(0.0, 1.0, 0.2, 0.8, 0.1, 1.0, "boolean", "SYSTEM");
            case PROXIMITY -> new SensorThreshold(0.0, 100.0, 10.0, 90.0, 5.0, 95.0, "cm", "SYSTEM");
            case CURRENT -> new SensorThreshold(0.0, 10.0, 1.0, 8.0, 0.5, 12.0, "A", "SYSTEM");
            case VOLTAGE -> new SensorThreshold(200.0, 240.0, 210.0, 230.0, 190.0, 250.0, "V", "SYSTEM");
            case POWER -> new SensorThreshold(0.0, 1000.0, 100.0, 800.0, 50.0, 1200.0, "W", "SYSTEM");
            case LIGHT -> new SensorThreshold(100.0, 1000.0, 200.0, 800.0, 50.0, 1200.0, "lux", "SYSTEM");
        };
    }
    
    public SensorStatus determineStatus(double value) {
        if (Double.isNaN(value)) {
            return SensorStatus.ERROR;
        }
        
        if (value <= criticalMin || value >= criticalMax) {
            return SensorStatus.CRITICAL;
        }
        
        if (value <= warningMin || value >= warningMax) {
            return SensorStatus.WARNING;
        }
        
        if (value < minValue || value > maxValue) {
            return SensorStatus.OUT_OF_RANGE;
        }
        
        return SensorStatus.NORMAL;
    }
    
    public boolean isWithinNormalRange(double value) {
        return value >= minValue && value <= maxValue;
    }
    
    public boolean isWithinWarningRange(double value) {
        return value >= warningMin && value <= warningMax;
    }
    
    public boolean isWithinCriticalRange(double value) {
        return value >= criticalMin && value <= criticalMax;
    }
    
    public boolean exceedsThreshold(double value) {
        return !isWithinNormalRange(value);
    }
    
    public double getDistanceFromNormal(double value) {
        if (value < minValue) {
            return minValue - value;
        } else if (value > maxValue) {
            return value - maxValue;
        }
        return 0.0;
    }
    
    public double getMinValue() {
        return minValue;
    }
    
    public double getMaxValue() {
        return maxValue;
    }
    
    public double getWarningMin() {
        return warningMin;
    }
    
    public double getWarningMax() {
        return warningMax;
    }
    
    public double getCriticalMin() {
        return criticalMin;
    }
    
    public double getCriticalMax() {
        return criticalMax;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public LocalDateTime getSetAt() {
        return setAt;
    }
    
    public String getSetBy() {
        return setBy;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SensorThreshold that = (SensorThreshold) obj;
        return Double.compare(that.minValue, minValue) == 0 &&
               Double.compare(that.maxValue, maxValue) == 0 &&
               Double.compare(that.warningMin, warningMin) == 0 &&
               Double.compare(that.warningMax, warningMax) == 0 &&
               Double.compare(that.criticalMin, criticalMin) == 0 &&
               Double.compare(that.criticalMax, criticalMax) == 0 &&
               Objects.equals(unit, that.unit);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(minValue, maxValue, warningMin, warningMax, 
                          criticalMin, criticalMax, unit);
    }
    
    @Override
    public String toString() {
        return String.format("Threshold{normal=[%.2f-%.2f], warning=[%.2f-%.2f], critical=[%.2f-%.2f] %s}",
                minValue, maxValue, warningMin, warningMax, criticalMin, criticalMax, unit);
    }
}