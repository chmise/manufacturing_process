package com.u1mobis.dashboard_backend.domain.iot.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.time.LocalDateTime;
import java.util.Objects;

public class SensorReading extends ValueObject {
    private final double value;
    private final LocalDateTime timestamp;
    private final SensorStatus status;
    private final String source;
    
    public SensorReading(double value, LocalDateTime timestamp, SensorStatus status, String source) {
        this.value = value;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.status = status != null ? status : SensorStatus.NORMAL;
        this.source = source != null ? source.trim() : "Unknown";
    }
    
    public double getValue() {
        return value;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public SensorStatus getStatus() {
        return status;
    }
    
    public String getSource() {
        return source;
    }
    
    public boolean isValid() {
        return !Double.isNaN(value) && status != SensorStatus.ERROR;
    }
    
    public boolean isRecent(int minutes) {
        return timestamp.isAfter(LocalDateTime.now().minusMinutes(minutes));
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SensorReading that = (SensorReading) obj;
        return Double.compare(that.value, value) == 0 &&
               Objects.equals(timestamp, that.timestamp) &&
               status == that.status &&
               Objects.equals(source, that.source);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value, timestamp, status, source);
    }
    
    @Override
    public String toString() {
        return String.format("Reading{value=%.2f, status=%s, timestamp=%s, source='%s'}",
                value, status, timestamp, source);
    }
}