package com.u1mobis.dashboard_backend.domain.analytics.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.time.LocalDateTime;
import java.util.Objects;

public class HistoricalDataPoint extends ValueObject {
    private final MetricValue previousValue;
    private final MetricValue currentValue;
    private final LocalDateTime timestamp;
    private final String source;
    
    public HistoricalDataPoint(MetricValue previousValue, MetricValue currentValue, 
                             LocalDateTime timestamp, String source) {
        this.previousValue = previousValue;
        this.currentValue = currentValue;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.source = source != null ? source.trim() : "Unknown";
    }
    
    public MetricValue getPreviousValue() {
        return previousValue;
    }
    
    public MetricValue getCurrentValue() {
        return currentValue;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public String getSource() {
        return source;
    }
    
    public MetricValue getChange() {
        if (previousValue == null) {
            return currentValue;
        }
        return currentValue.subtract(previousValue);
    }
    
    public double getChangePercentage() {
        if (previousValue == null || previousValue.isZero()) {
            return 0.0;
        }
        
        double change = getChange().getValue();
        return (change / previousValue.getValue()) * 100.0;
    }
    
    public boolean isImprovement() {
        return getChange().isPositive();
    }
    
    public boolean isDeterioration() {
        return !getChange().isPositive() && !getChange().isZero();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        HistoricalDataPoint that = (HistoricalDataPoint) obj;
        return Objects.equals(previousValue, that.previousValue) &&
               Objects.equals(currentValue, that.currentValue) &&
               Objects.equals(timestamp, that.timestamp) &&
               Objects.equals(source, that.source);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(previousValue, currentValue, timestamp, source);
    }
    
    @Override
    public String toString() {
        return String.format("DataPoint{value=%s, change=%s, timestamp=%s, source='%s'}",
                currentValue, getChange(), timestamp, source);
    }
}