package com.u1mobis.dashboard_backend.domain.analytics.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.util.Objects;
import java.util.UUID;

public class KPIId extends ValueObject {
    private final String value;
    
    public KPIId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("KPI ID cannot be empty");
        }
        this.value = value.trim();
    }
    
    public static KPIId generate() {
        return new KPIId(UUID.randomUUID().toString());
    }
    
    public static KPIId fromMetric(String metricName) {
        return new KPIId("KPI_" + metricName.toUpperCase().replace(" ", "_"));
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        KPIId kpiId = (KPIId) obj;
        return Objects.equals(value, kpiId.value);
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