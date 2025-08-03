package com.u1mobis.dashboard_backend.domain.inventory.model;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.shared.kernel.AggregateRoot;
import com.u1mobis.dashboard_backend.domain.inventory.event.StockAdjustedEvent;
import com.u1mobis.dashboard_backend.domain.inventory.event.StockLowEvent;
import com.u1mobis.dashboard_backend.domain.inventory.event.StockConsumedEvent;
import com.u1mobis.dashboard_backend.domain.inventory.event.StockReplenishedEvent;

import java.time.LocalDateTime;

public class Stock extends AggregateRoot<MaterialId> {
    private MaterialId id;
    private MaterialName name;
    private CompanyId companyId;
    private MaterialType type;
    private Quantity currentQuantity;
    private Quantity minimumThreshold;
    private Quantity maximumCapacity;
    private String unit;
    private String location;
    private double unitCost;
    private String supplier;
    private LocalDateTime lastRestockDate;
    private LocalDateTime lastUsedDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    protected Stock() {
    }
    
    public Stock(MaterialId id, MaterialName name, CompanyId companyId, MaterialType type,
                Quantity initialQuantity, Quantity minimumThreshold, Quantity maximumCapacity,
                String unit, String location, double unitCost) {
        this.id = id;
        this.name = name;
        this.companyId = companyId;
        this.type = type;
        this.currentQuantity = initialQuantity;
        this.minimumThreshold = minimumThreshold;
        this.maximumCapacity = maximumCapacity;
        this.unit = unit;
        this.location = location;
        this.unitCost = unitCost;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        if (initialQuantity.isPositive()) {
            this.lastRestockDate = LocalDateTime.now();
        }
    }
    
    public void consume(Quantity quantity, String reason) {
        if (quantity.isGreaterThan(currentQuantity)) {
            throw new IllegalArgumentException("Cannot consume more than available quantity");
        }
        
        Quantity oldQuantity = this.currentQuantity;
        this.currentQuantity = currentQuantity.subtract(quantity);
        this.lastUsedDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        raiseEvent(new StockConsumedEvent(
            LocalDateTime.now(),
            id.getValue(),
            companyId.getValue(),
            quantity.getValue(),
            oldQuantity.getValue(),
            currentQuantity.getValue(),
            reason
        ));
        
        checkLowStockAlert();
    }
    
    public void replenish(Quantity quantity, String reason) {
        if (quantity.isEmpty()) {
            throw new IllegalArgumentException("Replenish quantity must be positive");
        }
        
        Quantity newQuantity = currentQuantity.add(quantity);
        if (newQuantity.isGreaterThan(maximumCapacity)) {
            throw new IllegalArgumentException("Replenish would exceed maximum capacity");
        }
        
        Quantity oldQuantity = this.currentQuantity;
        this.currentQuantity = newQuantity;
        this.lastRestockDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        raiseEvent(new StockReplenishedEvent(
            LocalDateTime.now(),
            id.getValue(),
            companyId.getValue(),
            quantity.getValue(),
            oldQuantity.getValue(),
            currentQuantity.getValue(),
            reason
        ));
    }
    
    public void adjustQuantity(Quantity newQuantity, String reason) {
        if (newQuantity.isGreaterThan(maximumCapacity)) {
            throw new IllegalArgumentException("Adjustment would exceed maximum capacity");
        }
        
        Quantity oldQuantity = this.currentQuantity;
        this.currentQuantity = newQuantity;
        this.updatedAt = LocalDateTime.now();
        
        raiseEvent(new StockAdjustedEvent(
            LocalDateTime.now(),
            id.getValue(),
            companyId.getValue(),
            oldQuantity.getValue(),
            newQuantity.getValue(),
            reason
        ));
        
        checkLowStockAlert();
    }
    
    public void updateThresholds(Quantity newMinimum, Quantity newMaximum) {
        if (newMinimum.isGreaterThan(newMaximum)) {
            throw new IllegalArgumentException("Minimum threshold cannot be greater than maximum capacity");
        }
        
        this.minimumThreshold = newMinimum;
        this.maximumCapacity = newMaximum;
        this.updatedAt = LocalDateTime.now();
        
        checkLowStockAlert();
    }
    
    public void updateSupplier(String newSupplier) {
        this.supplier = newSupplier;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void updateLocation(String newLocation) {
        this.location = newLocation;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void updateUnitCost(double newUnitCost) {
        if (newUnitCost < 0) {
            throw new IllegalArgumentException("Unit cost cannot be negative");
        }
        this.unitCost = newUnitCost;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isLowStock() {
        return currentQuantity.isLessThan(minimumThreshold);
    }
    
    public boolean isOutOfStock() {
        return currentQuantity.isEmpty();
    }
    
    public boolean canConsume(Quantity quantity) {
        return currentQuantity.isGreaterThanOrEqual(quantity);
    }
    
    public double getTotalValue() {
        return currentQuantity.getValue() * unitCost;
    }
    
    public Quantity getAvailableCapacity() {
        return maximumCapacity.subtract(currentQuantity);
    }
    
    private void checkLowStockAlert() {
        if (isLowStock() && !isOutOfStock()) {
            raiseEvent(new StockLowEvent(
                LocalDateTime.now(),
                id.getValue(),
                companyId.getValue(),
                name.getValue(),
                currentQuantity.getValue(),
                minimumThreshold.getValue()
            ));
        }
    }
    
    @Override
    public MaterialId getId() {
        return id;
    }
    
    public MaterialName getName() {
        return name;
    }
    
    public CompanyId getCompanyId() {
        return companyId;
    }
    
    public MaterialType getType() {
        return type;
    }
    
    public Quantity getCurrentQuantity() {
        return currentQuantity;
    }
    
    public Quantity getMinimumThreshold() {
        return minimumThreshold;
    }
    
    public Quantity getMaximumCapacity() {
        return maximumCapacity;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public String getLocation() {
        return location;
    }
    
    public double getUnitCost() {
        return unitCost;
    }
    
    public String getSupplier() {
        return supplier;
    }
    
    public LocalDateTime getLastRestockDate() {
        return lastRestockDate;
    }
    
    public LocalDateTime getLastUsedDate() {
        return lastUsedDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}