package com.u1mobis.dashboard_backend.domain.company.model;

import java.time.LocalDateTime;

public class IndustryType {
    private IndustryTypeId id;
    private String name;
    private String code;
    private String description;
    private String iconName;
    private String primaryColor;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    protected IndustryType() {
    }
    
    public IndustryType(String name, String code, String description) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Industry name cannot be empty");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Industry code cannot be empty");
        }
        
        this.name = name.trim();
        this.code = code.trim().toUpperCase();
        this.description = description;
        this.active = true;
        this.createdAt = LocalDateTime.now();
    }
    
    public void setId(IndustryTypeId id) {
        this.id = id;
    }
    
    public void updateDetails(String name, String description, String iconName, String primaryColor) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
        this.description = description;
        this.iconName = iconName;
        this.primaryColor = primaryColor;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void deactivate() {
        this.active = false;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void activate() {
        this.active = true;
        this.updatedAt = LocalDateTime.now();
    }
    
    public IndustryTypeId getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getIconName() {
        return iconName;
    }
    
    public String getPrimaryColor() {
        return primaryColor;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}