package com.u1mobis.dashboard_backend.domain.inventory.model;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.shared.kernel.AggregateRoot;
import com.u1mobis.dashboard_backend.domain.inventory.event.KeyMaterialAllocatedEvent;
import com.u1mobis.dashboard_backend.domain.inventory.event.KeyMaterialReleasedEvent;
import com.u1mobis.dashboard_backend.domain.inventory.event.KeyMaterialReservedEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class KeyMaterial extends AggregateRoot<MaterialId> {
    private MaterialId id;
    private MaterialName name;
    private CompanyId companyId;
    private MaterialType type;
    private Quantity totalQuantity;
    private Quantity availableQuantity;
    private Quantity reservedQuantity;
    private Quantity criticalThreshold;
    private String unit;
    private String storageLocation;
    private double unitPrice;
    private String supplier;
    private Priority priority;
    private QualityStatus qualityStatus;
    private List<MaterialAllocation> allocations;
    private LocalDateTime expirationDate;
    private LocalDateTime lastInspectionDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    protected KeyMaterial() {
        this.allocations = new ArrayList<>();
    }
    
    public KeyMaterial(MaterialId id, MaterialName name, CompanyId companyId, MaterialType type,
                      Quantity totalQuantity, Quantity criticalThreshold, String unit,
                      String storageLocation, double unitPrice, Priority priority) {
        this.id = id;
        this.name = name;
        this.companyId = companyId;
        this.type = type;
        this.totalQuantity = totalQuantity;
        this.availableQuantity = totalQuantity;
        this.reservedQuantity = Quantity.zero();
        this.criticalThreshold = criticalThreshold;
        this.unit = unit;
        this.storageLocation = storageLocation;
        this.unitPrice = unitPrice;
        this.priority = priority;
        this.qualityStatus = QualityStatus.PENDING_INSPECTION;
        this.allocations = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void reserve(Quantity quantity, String orderId, String purpose) {
        if (quantity.isGreaterThan(availableQuantity)) {
            throw new IllegalArgumentException("Cannot reserve more than available quantity");
        }
        
        this.availableQuantity = availableQuantity.subtract(quantity);
        this.reservedQuantity = reservedQuantity.add(quantity);
        this.updatedAt = LocalDateTime.now();
        
        raiseEvent(new KeyMaterialReservedEvent(
            LocalDateTime.now(),
            id.getValue(),
            companyId.getValue(),
            quantity.getValue(),
            orderId,
            purpose
        ));
    }
    
    public void allocate(Quantity quantity, String workOrderId, String purpose) {
        if (quantity.isGreaterThan(reservedQuantity)) {
            throw new IllegalArgumentException("Cannot allocate more than reserved quantity");
        }
        
        this.reservedQuantity = reservedQuantity.subtract(quantity);
        this.totalQuantity = totalQuantity.subtract(quantity);
        this.updatedAt = LocalDateTime.now();
        
        MaterialAllocation allocation = new MaterialAllocation(
            workOrderId, quantity, purpose, LocalDateTime.now()
        );
        this.allocations.add(allocation);
        
        raiseEvent(new KeyMaterialAllocatedEvent(
            LocalDateTime.now(),
            id.getValue(),
            companyId.getValue(),
            quantity.getValue(),
            workOrderId,
            purpose
        ));
        
        checkCriticalLevel();
    }
    
    public void releaseReservation(Quantity quantity, String orderId, String reason) {
        if (quantity.isGreaterThan(reservedQuantity)) {
            throw new IllegalArgumentException("Cannot release more than reserved quantity");
        }
        
        this.reservedQuantity = reservedQuantity.subtract(quantity);
        this.availableQuantity = availableQuantity.add(quantity);
        this.updatedAt = LocalDateTime.now();
        
        raiseEvent(new KeyMaterialReleasedEvent(
            LocalDateTime.now(),
            id.getValue(),
            companyId.getValue(),
            quantity.getValue(),
            orderId,
            reason
        ));
    }
    
    public void receiveStock(Quantity quantity, String batchNumber) {
        this.totalQuantity = totalQuantity.add(quantity);
        this.availableQuantity = availableQuantity.add(quantity);
        this.qualityStatus = QualityStatus.PENDING_INSPECTION;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void approveQuality() {
        this.qualityStatus = QualityStatus.APPROVED;
        this.lastInspectionDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void rejectQuality(String reason) {
        this.qualityStatus = QualityStatus.REJECTED;
        this.lastInspectionDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        // Move available quantity to quarantine or return to supplier
        this.availableQuantity = Quantity.zero();
    }
    
    public void updateCriticalThreshold(Quantity newThreshold) {
        this.criticalThreshold = newThreshold;
        this.updatedAt = LocalDateTime.now();
        checkCriticalLevel();
    }
    
    public void updateStorageLocation(String newLocation) {
        this.storageLocation = newLocation;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void updateSupplier(String newSupplier) {
        this.supplier = newSupplier;
        this.updatedAt = LocalDateTime.now();
    }
    
    public void setExpirationDate(LocalDateTime expirationDate) {
        this.expirationDate = expirationDate;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isCriticalLevel() {
        return totalQuantity.isLessThan(criticalThreshold);
    }
    
    public boolean isExpired() {
        return expirationDate != null && LocalDateTime.now().isAfter(expirationDate);
    }
    
    public boolean isAvailableForUse() {
        return qualityStatus == QualityStatus.APPROVED && !isExpired();
    }
    
    public boolean canReserve(Quantity quantity) {
        return isAvailableForUse() && availableQuantity.isGreaterThanOrEqual(quantity);
    }
    
    public double getTotalValue() {
        return totalQuantity.getValue() * unitPrice;
    }
    
    public double getAvailableValue() {
        return availableQuantity.getValue() * unitPrice;
    }
    
    private void checkCriticalLevel() {
        if (isCriticalLevel()) {
            // Critical material event could be raised here
            // This would be handled by the alerts domain
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
    
    public Quantity getTotalQuantity() {
        return totalQuantity;
    }
    
    public Quantity getAvailableQuantity() {
        return availableQuantity;
    }
    
    public Quantity getReservedQuantity() {
        return reservedQuantity;
    }
    
    public Quantity getCriticalThreshold() {
        return criticalThreshold;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public String getStorageLocation() {
        return storageLocation;
    }
    
    public double getUnitPrice() {
        return unitPrice;
    }
    
    public String getSupplier() {
        return supplier;
    }
    
    public Priority getPriority() {
        return priority;
    }
    
    public QualityStatus getQualityStatus() {
        return qualityStatus;
    }
    
    public List<MaterialAllocation> getAllocations() {
        return new ArrayList<>(allocations);
    }
    
    public LocalDateTime getExpirationDate() {
        return expirationDate;
    }
    
    public LocalDateTime getLastInspectionDate() {
        return lastInspectionDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}