package com.u1mobis.dashboard_backend.domain.user.event;

import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class UserDeactivatedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String userId;
    private final String companyId;
    private final String username;
    private final String reason;
    
    public UserDeactivatedEvent(LocalDateTime occurredOn, String userId, String companyId, 
                              String username, String reason) {
        this.occurredOn = occurredOn;
        this.userId = userId;
        this.companyId = companyId;
        this.username = username;
        this.reason = reason;
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
    
    public String getReason() {
        return reason;
    }
}