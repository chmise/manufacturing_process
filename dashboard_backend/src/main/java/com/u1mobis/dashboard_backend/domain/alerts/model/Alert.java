package com.u1mobis.dashboard_backend.domain.alerts.model;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.shared.kernel.AggregateRoot;
import com.u1mobis.dashboard_backend.domain.alerts.event.AlertCreatedEvent;
import com.u1mobis.dashboard_backend.domain.alerts.event.AlertAcknowledgedEvent;
import com.u1mobis.dashboard_backend.domain.alerts.event.AlertResolvedEvent;
import com.u1mobis.dashboard_backend.domain.alerts.event.AlertEscalatedEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Alert extends AggregateRoot<AlertId> {
    private AlertId id;
    private CompanyId companyId;
    private AlertType type;
    private AlertSeverity severity;
    private AlertStatus status;
    private String title;
    private String message;
    private String source;
    private String sourceId;
    private String category;
    private List<String> tags;
    private AlertContext context;
    private String assignedTo;
    private String acknowledgedBy;
    private String resolvedBy;
    private LocalDateTime createdAt;
    private LocalDateTime acknowledgedAt;
    private LocalDateTime resolvedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime expiresAt;
    
    protected Alert() {
        this.tags = new ArrayList<>();
    }
    
    public Alert(AlertId id, CompanyId companyId, AlertType type, AlertSeverity severity,
                String title, String message, String source, String sourceId) {
        this.id = id;
        this.companyId = companyId;
        this.type = type;
        this.severity = severity;
        this.status = AlertStatus.ACTIVE;
        this.title = title;
        this.message = message;
        this.source = source;
        this.sourceId = sourceId;
        this.category = type.isProductionRelated() ? "PRODUCTION" : 
                       type.isEnvironmentalRelated() ? "ENVIRONMENT" : 
                       type.isResourceRelated() ? "RESOURCE" : "SYSTEM";
        this.tags = new ArrayList<>();
        this.context = new AlertContext();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        // 심각도에 따라 만료 시간 설정
        setExpirationBasedOnSeverity();
        
        // Alert 생성 이벤트 발생
        raiseEvent(new AlertCreatedEvent(
            LocalDateTime.now(), id.getValue(), companyId.getValue(),
            type, severity, title, message, source
        ));
        
        // 긴급 알림의 경우 즉시 에스컬레이션
        if (severity.requiresEscalation()) {
            escalate("Automatic escalation for high severity alert");
        }
    }
    
    public void acknowledge(String userId, String comment) {
        if (status == AlertStatus.RESOLVED) {
            throw new IllegalStateException("Cannot acknowledge resolved alert");
        }
        if (status == AlertStatus.ACKNOWLEDGED) {
            throw new IllegalStateException("Alert already acknowledged");
        }
        
        this.status = AlertStatus.ACKNOWLEDGED;
        this.acknowledgedBy = userId;
        this.acknowledgedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        if (comment != null && !comment.trim().isEmpty()) {
            context.addNote(comment, userId);
        }
        
        raiseEvent(new AlertAcknowledgedEvent(
            LocalDateTime.now(), id.getValue(), companyId.getValue(),
            userId, comment
        ));
    }
    
    public void resolve(String userId, String resolution) {
        if (status == AlertStatus.RESOLVED) {
            throw new IllegalStateException("Alert already resolved");
        }
        
        this.status = AlertStatus.RESOLVED;
        this.resolvedBy = userId;
        this.resolvedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        if (resolution != null && !resolution.trim().isEmpty()) {
            context.setResolution(resolution, userId);
        }
        
        raiseEvent(new AlertResolvedEvent(
            LocalDateTime.now(), id.getValue(), companyId.getValue(),
            userId, resolution, getResolutionTimeMinutes()
        ));
    }
    
    public void escalate(String reason) {
        if (status == AlertStatus.RESOLVED) {
            throw new IllegalStateException("Cannot escalate resolved alert");
        }
        
        // 심각도 상향 조정
        AlertSeverity newSeverity = escalateSeverity();
        if (newSeverity != this.severity) {
            this.severity = newSeverity;
            setExpirationBasedOnSeverity();
        }
        
        this.updatedAt = LocalDateTime.now();
        
        context.addNote("Alert escalated: " + reason, "SYSTEM");
        
        raiseEvent(new AlertEscalatedEvent(
            LocalDateTime.now(), id.getValue(), companyId.getValue(),
            severity, reason
        ));
    }
    
    public void addTag(String tag) {
        if (tag != null && !tag.trim().isEmpty() && !tags.contains(tag.trim())) {
            tags.add(tag.trim());
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    public void removeTag(String tag) {
        if (tags.remove(tag)) {
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    public void assignTo(String userId) {
        this.assignedTo = userId;
        this.updatedAt = LocalDateTime.now();
        
        context.addNote("Alert assigned to " + userId, "SYSTEM");
    }
    
    public void addNote(String note, String userId) {
        context.addNote(note, userId);
        this.updatedAt = LocalDateTime.now();
    }
    
    public void updateSeverity(AlertSeverity newSeverity) {
        if (newSeverity != this.severity) {
            this.severity = newSeverity;
            setExpirationBasedOnSeverity();
            this.updatedAt = LocalDateTime.now();
        }
    }
    
    private void setExpirationBasedOnSeverity() {
        this.expiresAt = switch (severity) {
            case EMERGENCY -> createdAt.plusHours(1);
            case CRITICAL -> createdAt.plusHours(4);
            case WARNING -> createdAt.plusHours(24);
            case INFO -> createdAt.plusDays(7);
        };
    }
    
    private AlertSeverity escalateSeverity() {
        return switch (severity) {
            case INFO -> AlertSeverity.WARNING;
            case WARNING -> AlertSeverity.CRITICAL;
            case CRITICAL -> AlertSeverity.EMERGENCY;
            case EMERGENCY -> AlertSeverity.EMERGENCY; // Already at max
        };
    }
    
    public boolean isExpired() {
        return expiresAt != null && LocalDateTime.now().isAfter(expiresAt);
    }
    
    public boolean isOverdue() {
        return status == AlertStatus.ACTIVE && isExpired();
    }
    
    public boolean requiresAttention() {
        return status == AlertStatus.ACTIVE && severity.requiresNotification();
    }
    
    public long getAgeInMinutes() {
        return java.time.Duration.between(createdAt, LocalDateTime.now()).toMinutes();
    }
    
    public long getResolutionTimeMinutes() {
        if (resolvedAt == null) {
            return 0;
        }
        return java.time.Duration.between(createdAt, resolvedAt).toMinutes();
    }
    
    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }
    
    @Override
    public AlertId getId() {
        return id;
    }
    
    public CompanyId getCompanyId() {
        return companyId;
    }
    
    public AlertType getType() {
        return type;
    }
    
    public AlertSeverity getSeverity() {
        return severity;
    }
    
    public AlertStatus getStatus() {
        return status;
    }
    
    public String getTitle() {
        return title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public String getSource() {
        return source;
    }
    
    public String getSourceId() {
        return sourceId;
    }
    
    public String getCategory() {
        return category;
    }
    
    public List<String> getTags() {
        return new ArrayList<>(tags);
    }
    
    public AlertContext getContext() {
        return context;
    }
    
    public String getAssignedTo() {
        return assignedTo;
    }
    
    public String getAcknowledgedBy() {
        return acknowledgedBy;
    }
    
    public String getResolvedBy() {
        return resolvedBy;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getAcknowledgedAt() {
        return acknowledgedAt;
    }
    
    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }
}