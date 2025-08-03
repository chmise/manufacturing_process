package com.u1mobis.dashboard_backend.domain.iot.event;

import com.u1mobis.dashboard_backend.domain.iot.model.SensorStatus;
import com.u1mobis.dashboard_backend.domain.iot.model.SensorType;
import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class SensorDataReceivedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String sensorId;
    private final String companyId;
    private final SensorType sensorType;
    private final double value;
    private final SensorStatus status;
    private final String location;
    
    public SensorDataReceivedEvent(LocalDateTime occurredOn, String sensorId, String companyId,
                                 SensorType sensorType, double value, SensorStatus status, String location) {
        this.occurredOn = occurredOn;
        this.sensorId = sensorId;
        this.companyId = companyId;
        this.sensorType = sensorType;
        this.value = value;
        this.status = status;
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
    
    public double getValue() {
        return value;
    }
    
    public SensorStatus getStatus() {
        return status;
    }
    
    public String getLocation() {
        return location;
    }
}