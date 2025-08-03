package com.u1mobis.dashboard_backend.domain.manufacturing.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.util.Objects;

public class ProductionTarget extends ValueObject {
    private final int dailyTarget;
    private final int productionRate; // 시간당 생산량
    
    public ProductionTarget(int dailyTarget, int productionRate) {
        if (dailyTarget <= 0) {
            throw new IllegalArgumentException("Daily target must be positive");
        }
        if (productionRate <= 0) {
            throw new IllegalArgumentException("Production rate must be positive");
        }
        
        this.dailyTarget = dailyTarget;
        this.productionRate = productionRate;
    }
    
    public int getDailyTarget() {
        return dailyTarget;
    }
    
    public int getProductionRate() {
        return productionRate;
    }
    
    public double getRequiredHours() {
        return (double) dailyTarget / productionRate;
    }
    
    public boolean isAchievable(int availableHours) {
        return getRequiredHours() <= availableHours;
    }
    
    public ProductionTarget adjustTarget(int newDailyTarget) {
        return new ProductionTarget(newDailyTarget, productionRate);
    }
    
    public ProductionTarget adjustRate(int newProductionRate) {
        return new ProductionTarget(dailyTarget, newProductionRate);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ProductionTarget that = (ProductionTarget) obj;
        return dailyTarget == that.dailyTarget && productionRate == that.productionRate;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(dailyTarget, productionRate);
    }
    
    @Override
    public String toString() {
        return "ProductionTarget{" +
                "dailyTarget=" + dailyTarget +
                ", productionRate=" + productionRate + "/hr" +
                '}';
    }
}