package com.u1mobis.dashboard_backend.domain.company.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.util.Objects;
import java.util.regex.Pattern;

public class CompanyCode extends ValueObject {
    private static final Pattern VALID_PATTERN = Pattern.compile("^[A-Z0-9]{4,8}$");
    private final String value;
    
    public CompanyCode(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Company code cannot be empty");
        }
        
        String trimmedValue = value.trim().toUpperCase();
        if (!VALID_PATTERN.matcher(trimmedValue).matches()) {
            throw new IllegalArgumentException("Company code must be 4-8 alphanumeric characters");
        }
        
        this.value = trimmedValue;
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CompanyCode that = (CompanyCode) obj;
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