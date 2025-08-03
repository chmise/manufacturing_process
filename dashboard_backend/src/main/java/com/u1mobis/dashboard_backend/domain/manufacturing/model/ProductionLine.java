package com.u1mobis.dashboard_backend.domain.manufacturing.model;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.shared.kernel.AggregateRoot;
import com.u1mobis.dashboard_backend.domain.manufacturing.event.ProductionLineActivatedEvent;
import com.u1mobis.dashboard_backend.domain.manufacturing.event.ProductionLineDeactivatedEvent;

import java.time.LocalDateTime;

public class ProductionLine extends AggregateRoot<ProductionLineId> {
    private ProductionLineId id;
    private String name;
    private String code;
    private String description;
    private CompanyId companyId;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    protected ProductionLine() {
    }
    
    public ProductionLine(ProductionLineId id, String name, String code, String description, CompanyId companyId) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Production line name cannot be empty");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Production line code cannot be empty");
        }
        
        this.id = id;
        this.name = name.trim();
        this.code = code.trim().toUpperCase();
        this.description = description;
        this.companyId = companyId;
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void activate() {
        if (active) {
            return; // 이미 활성화됨
        }
        
        this.active = true;
        this.updatedAt = LocalDateTime.now();
        
        raiseEvent(new ProductionLineActivatedEvent(
            LocalDateTime.now(),
            id.getValue(),
            companyId.getValue(),
            name
        ));
    }
    
    public void deactivate(String reason) {
        if (!active) {
            return; // 이미 비활성화됨
        }
        
        this.active = false;
        this.updatedAt = LocalDateTime.now();
        
        raiseEvent(new ProductionLineDeactivatedEvent(
            LocalDateTime.now(),
            id.getValue(),
            companyId.getValue(),
            name,
            reason
        ));
    }
    
    public void updateDetails(String name, String description) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
        if (description != null) {
            this.description = description.trim();
        }
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean canProduceProduct() {
        return active;
    }
    
    @Override
    public ProductionLineId getId() {
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
    
    public CompanyId getCompanyId() {
        return companyId;
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