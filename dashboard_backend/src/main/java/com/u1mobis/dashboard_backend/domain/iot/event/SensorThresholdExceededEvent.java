package com.u1mobis.dashboard_backend.domain.iot.event;

import com.u1mobis.dashboard_backend.domain.iot.model.SensorStatus;
import com.u1mobis.dashboard_backend.domain.iot.model.SensorType;
import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class SensorThresholdExceededEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String sensorId;
    private final String companyId;
    private final SensorType sensorType;
    private final double currentValue;
    private final double thresholdValue;
    private final String thresholdType; // "MIN", "MAX", "WARNING_MIN", "WARNING_MAX", "CRITICAL_MIN", "CRITICAL_MAX"
    private final SensorStatus newStatus;
    private final String location;
    
    public SensorThresholdExceededEvent(LocalDateTime occurredOn, String sensorId, String companyId,
                                      SensorType sensorType, double currentValue, double thresholdValue,
                                      String thresholdType, SensorStatus newStatus, String location) {
        this.occurredOn = occurredOn;
        this.sensorId = sensorId;
        this.companyId = companyId;
        this.sensorType = sensorType;
        this.currentValue = currentValue;
        this.thresholdValue = thresholdValue;
        this.thresholdType = thresholdType;
        this.newStatus = newStatus;
        this.location = location;
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String aggregateId() {
        return sensorId;
    }
    
    public String getSensorId() {
        return sensorId;
    }
    
    public String getCompanyId() {
        return companyId;
    }
    
    public SensorType getSensorType() {
        return sensorType;
    }
    
    public double getCurrentValue() {
        return currentValue;
    }
    
    public double getThresholdValue() {
        return thresholdValue;
    }
    
    public String getThresholdType() {
        return thresholdType;
    }
    
    public SensorStatus getNewStatus() {
        return newStatus;
    }
    
    public String getLocation() {
        return location;
    }
    
    public double getExcessAmount() {
        return Math.abs(currentValue - thresholdValue);
    }
    
    public boolean isCriticalThreshold() {
        return thresholdType.contains("CRITICAL");
    }
    
    public boolean isWarningThreshold() {
        return thresholdType.contains("WARNING");
    }
}