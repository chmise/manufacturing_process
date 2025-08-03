package com.u1mobis.dashboard_backend.domain.company.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.util.Objects;

public class CompanyCapacity extends ValueObject {
    private final Integer dailyProductionCapacity;
    private final Integer automationLevel;
    
    public CompanyCapacity(Integer dailyProductionCapacity, Integer automationLevel) {
        if (dailyProductionCapacity != null && dailyProductionCapacity < 1) {
            throw new IllegalArgumentException("Daily production capacity must be positive");
        }
        if (automationLevel != null && (automationLevel < 0 || automationLevel > 100)) {
            throw new IllegalArgumentException("Automation level must be between 0 and 100");
        }
        
        this.dailyProductionCapacity = dailyProductionCapacity;
        this.automationLevel = automationLevel;
    }
    
    public Integer getDailyProductionCapacity() {
        return dailyProductionCapacity;
    }
    
    public Integer getAutomationLevel() {
        return automationLevel;
    }
    
    public boolean isHighlyAutomated() {
        return automationLevel != null && automationLevel >= 70;
    }
    
    public boolean isLowCapacity() {
        return dailyProductionCapacity != null && dailyProductionCapacity < 100;
    }
    
    public boolean isHighCapacity() {
        return dailyProductionCapacity != null && dailyProductionCapacity >= 1000;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CompanyCapacity that = (CompanyCapacity) obj;
        return Objects.equals(dailyProductionCapacity, that.dailyProductionCapacity) &&
               Objects.equals(automationLevel, that.automationLevel);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(dailyProductionCapacity, automationLevel);
    }
    
    @Override
    public String toString() {
        return "CompanyCapacity{" +
                "dailyCapacity=" + dailyProductionCapacity +
                ", automation=" + automationLevel + "%" +
                '}';
    }
}