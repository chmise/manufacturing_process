package com.u1mobis.dashboard_backend.domain.user.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.util.Objects;
import java.util.regex.Pattern;

public class Email extends ValueObject {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    private final String value;
    
    public Email(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        
        String trimmedValue = value.trim().toLowerCase();
        
        if (!EMAIL_PATTERN.matcher(trimmedValue).matches()) {
            throw new IllegalArgumentException("Invalid email format: " + value);
        }
        
        this.value = trimmedValue;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getDomain() {
        int atIndex = value.indexOf('@');
        return atIndex > 0 ? value.substring(atIndex + 1) : "";
    }
    
    public String getLocalPart() {
        int atIndex = value.indexOf('@');
        return atIndex > 0 ? value.substring(0, atIndex) : value;
    }
    
    public boolean isFromDomain(String domain) {
        return getDomain().equalsIgnoreCase(domain);
    }
    
    public boolean isCompanyEmail() {
        String domain = getDomain();
        return !domain.equals("gmail.com") && 
               !domain.equals("yahoo.com") && 
               !domain.equals("hotmail.com") && 
               !domain.equals("outlook.com") &&
               !domain.endsWith(".co.kr") && 
               !domain.endsWith(".com");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Email email = (Email) obj;
        return Objects.equals(value, email.value);
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