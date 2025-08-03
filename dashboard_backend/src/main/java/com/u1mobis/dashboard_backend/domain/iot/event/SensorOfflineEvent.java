package com.u1mobis.dashboard_backend.domain.iot.event;

import com.u1mobis.dashboard_backend.domain.iot.model.SensorType;
import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class SensorOfflineEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String sensorId;
    private final String companyId;
    private final SensorType sensorType;
    private final String location;
    private final LocalDateTime lastCommunication;
    private final String reason;
    private final long offlineDurationMinutes;
    
    public SensorOfflineEvent(LocalDateTime occurredOn, String sensorId, String companyId,
                            SensorType sensorType, String location, LocalDateTime lastCommunication,
                            String reason) {
        this.occurredOn = occurredOn;
        this.sensorId = sensorId;
        this.companyId = companyId;
        this.sensorType = sensorType;
        this.location = location;
        this.lastCommunication = lastCommunication;
        this.reason = reason;
        this.offlineDurationMinutes = lastCommunication != null ? 
            java.time.Duration.between(lastCommunication, occurredOn).toMinutes() : 0;
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
    
    public LocalDateTime getLastCommunication() {
        return lastCommunication;
    }
    
    public String getReason() {
        return reason;
    }
    
    public long getOfflineDurationMinutes() {
        return offlineDurationMinutes;
    }
    
    public boolean isExtendedOutage() {
        return offlineDurationMinutes > 60; // More than 1 hour
    }
    
    public boolean isCriticalOutage() {
        return offlineDurationMinutes > 240; // More than 4 hours
    }
}