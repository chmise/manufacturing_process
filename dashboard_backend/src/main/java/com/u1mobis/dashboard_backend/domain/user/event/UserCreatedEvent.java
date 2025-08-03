package com.u1mobis.dashboard_backend.domain.user.event;

import com.u1mobis.dashboard_backend.domain.user.model.UserRole;
import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class UserCreatedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String userId;
    private final String companyId;
    private final String username;
    private final String email;
    private final UserRole role;
    private final String fullName;
    
    public UserCreatedEvent(LocalDateTime occurredOn, String userId, String companyId,
                          String username, String email, UserRole role, String fullName) {
        this.occurredOn = occurredOn;
        this.userId = userId;
        this.companyId = companyId;
        this.username = username;
        this.email = email;
        this.role = role;
        this.fullName = fullName;
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String aggregateId() {
        return userId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getCompanyId() {
        return companyId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public String getFullName() {
        return fullName;
    }
}