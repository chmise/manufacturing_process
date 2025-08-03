package com.u1mobis.dashboard_backend.domain.alerts.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.util.Objects;
import java.util.UUID;

public class AlertId extends ValueObject {
    private final String value;
    
    public AlertId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Alert ID cannot be empty");
        }
        this.value = value.trim();
    }
    
    public static AlertId generate() {
        return new AlertId(UUID.randomUUID().toString());
    }
    
    public static AlertId fromSequence(Long sequence) {
        return new AlertId("ALERT_" + String.format("%08d", sequence));
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AlertId alertId = (AlertId) obj;
        return Objects.equals(value, alertId.value);
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