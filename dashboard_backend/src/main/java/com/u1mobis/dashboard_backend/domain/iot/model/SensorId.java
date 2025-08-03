package com.u1mobis.dashboard_backend.domain.iot.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.util.Objects;
import java.util.UUID;

public class SensorId extends ValueObject {
    private final String value;
    
    public SensorId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Sensor ID cannot be empty");
        }
        this.value = value.trim();
    }
    
    public static SensorId generate() {
        return new SensorId(UUID.randomUUID().toString());
    }
    
    public static SensorId fromDeviceId(String deviceId) {
        return new SensorId("SENSOR_" + deviceId);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        SensorId sensorId = (SensorId) obj;
        return Objects.equals(value, sensorId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}