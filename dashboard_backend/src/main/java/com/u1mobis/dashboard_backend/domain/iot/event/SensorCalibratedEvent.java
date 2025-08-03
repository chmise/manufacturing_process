package com.u1mobis.dashboard_backend.domain.iot.event;

import com.u1mobis.dashboard_backend.domain.iot.model.SensorType;
import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class SensorCalibratedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String sensorId;
    private final String companyId;
    private final SensorType sensorType;
    private final String location;
    private final String calibrationType;
    private final double previousOffset;
    private final double newOffset;
    private final double previousMultiplier;
    private final double newMultiplier;
    private final String calibratedBy;
    private final String notes;
    
    public SensorCalibratedEvent(LocalDateTime occurredOn, String sensorId, String companyId,
                               SensorType sensorType, String location, String calibrationType,
                               double previousOffset, double newOffset, double previousMultiplier,
                               double newMultiplier, String calibratedBy, String notes) {
        this.occurredOn = occurredOn;
        this.sensorId = sensorId;
        this.companyId = companyId;
        this.sensorType = sensorType;
        this.location = location;
        this.calibrationType = calibrationType;
        this.previousOffset = previousOffset;
        this.newOffset = newOffset;
        this.previousMultiplier = previousMultiplier;
        this.newMultiplier = newMultiplier;
        this.calibratedBy = calibratedBy;
        this.notes = notes;
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
    
    public String getLocation() {
        return location;
    }
    
    public String getCalibrationType() {
        return calibrationType;
    }
    
    public double getPreviousOffset() {
        return previousOffset;
    }
    
    public double getNewOffset() {
        return newOffset;
    }
    
    public double getPreviousMultiplier() {
        return previousMultiplier;
    }
    
    public double getNewMultiplier() {
        return newMultiplier;
    }
    
    public String getCalibratedBy() {
        return calibratedBy;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public boolean isSignificantCalibrationChange() {
        double offsetChange = Math.abs(newOffset - previousOffset);
        double multiplierChange = Math.abs(newMultiplier - previousMultiplier);
        return offsetChange > 0.1 || multiplierChange > 0.05;
    }
    
    public boolean isAutomaticCalibration() {
        return "AUTOMATIC".equals(calibrationType);
    }
}