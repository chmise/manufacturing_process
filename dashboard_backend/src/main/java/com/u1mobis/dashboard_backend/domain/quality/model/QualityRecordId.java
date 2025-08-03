package com.u1mobis.dashboard_backend.domain.quality.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.util.Objects;
import java.util.UUID;

public class QualityRecordId extends ValueObject {
    private final String value;
    
    public QualityRecordId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Quality record ID cannot be empty");
        }
        this.value = value.trim();
    }
    
    public static QualityRecordId generate() {
        return new QualityRecordId(UUID.randomUUID().toString());
    }
    
    public static QualityRecordId fromBatch(String batchNumber) {
        return new QualityRecordId("QR_" + batchNumber);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        QualityRecordId that = (QualityRecordId) obj;
        return Objects.equals(value, that.value);
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