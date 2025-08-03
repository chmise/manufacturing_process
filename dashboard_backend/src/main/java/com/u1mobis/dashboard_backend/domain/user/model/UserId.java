package com.u1mobis.dashboard_backend.domain.user.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.util.Objects;
import java.util.UUID;

public class UserId extends ValueObject {
    private final String value;
    
    public UserId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID cannot be empty");
        }
        this.value = value.trim();
    }
    
    public static UserId generate() {
        return new UserId(UUID.randomUUID().toString());
    }
    
    public static UserId of(String value) {
        return new UserId(value);
    }
    
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UserId userId = (UserId) obj;
        return Objects.equals(value, userId.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return "UserId{" + value + "}";
    }
}