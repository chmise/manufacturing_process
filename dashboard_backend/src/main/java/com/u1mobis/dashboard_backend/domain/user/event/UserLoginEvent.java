package com.u1mobis.dashboard_backend.domain.user.event;

import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;

import java.time.LocalDateTime;

public class UserLoginEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String userId;
    private final String companyId;
    private final String username;
    private final String status; // "SUCCESS", "FAILED"
    private final String failureReason;
    
    public UserLoginEvent(LocalDateTime occurredOn, String userId, String companyId,
                        String username, String status, String failureReason) {
        this.occurredOn = occurredOn;
        this.userId = userId;
        this.companyId = companyId;
        this.username = username;
        this.status = status;
        this.failureReason = failureReason;
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
    
    public String getStatus() {
        return status;
    }
    
    public String getFailureReason() {
        return failureReason;
    }
    
    public boolean isSuccessful() {
        return "SUCCESS".equals(status);
    }
    
    public boolean isFailed() {
        return "FAILED".equals(status);
    }
}