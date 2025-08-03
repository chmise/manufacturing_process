package com.u1mobis.dashboard_backend.domain.company.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.util.Objects;

public class CompanyId extends ValueObject {
    private final String value;
    
    public CompanyId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Company ID cannot be empty");
        }
        this.value = value.trim();
    }
    
    public static CompanyId of(String value) {
        return new CompanyId(value);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CompanyId companyId = (CompanyId) obj;
        return Objects.equals(value, companyId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "CompanyId{" + value + "}";
    }
}