package com.u1mobis.dashboard_backend.domain.analytics.model;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.manufacturing.model.ProductionLineId;
import com.u1mobis.dashboard_backend.shared.kernel.AggregateRoot;
import com.u1mobis.dashboard_backend.domain.analytics.event.KPIThresholdExceededEvent;
import com.u1mobis.dashboard_backend.domain.analytics.event.KPICalculatedEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;

public class KPIData extends AggregateRoot<KPIId> {
    private KPIId id;
    private CompanyId companyId;
    private ProductionLineId lineId;
    private KPIType type;
    private MetricValue currentValue;
    private MetricValue targetValue;
    private MetricValue minimumThreshold;
    private MetricValue maximumThreshold;
    private TimePeriod measurementPeriod;
    private List<HistoricalDataPoint> historicalData;
    private KPIStatus status;
    private String calculationMethod;
    private LocalDateTime lastCalculatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    protected KPIData() {
        this.historicalData = new ArrayList<>();
    }
    
    public KPIData(KPIId id, CompanyId companyId, ProductionLineId lineId, KPIType type,
                  MetricValue targetValue, MetricValue minimumThreshold, MetricValue maximumThreshold,
                  TimePeriod measurementPeriod, String calculationMethod) {
        this.id = id;
        this.companyId = companyId;
        this.lineId = lineId;
        this.type = type;
        this.targetValue = targetValue;
        this.minimumThreshold = minimumThreshold;
        this.maximumThreshold = maximumThreshold;
        this.measurementPeriod = measurementPeriod;
        this.calculationMethod = calculationMethod;
        this.currentValue = new MetricValue(0.0, type.getUnit());
        this.historicalData = new ArrayList<>();
        this.status = KPIStatus.NOT_MEASURED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void updateValue(MetricValue newValue, String source) {
        if (!newValue.getUnit().equals(type.getUnit())) {
            throw new IllegalArgumentException("Value unit must match KPI type unit");
        }
        
        MetricValue previousValue = this.currentValue;
        this.currentValue = newValue;
        this.lastCalculatedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        // Add to historical data
        HistoricalDataPoint dataPoint = new HistoricalDataPoint(
            previousValue, newValue, LocalDateTime.now(), source
        );
        this.historicalData.add(dataPoint);
        
        // Limit historical data to last 1000 points
        if (historicalData.size() > 1000) {
            historicalData.remove(0);
        }
        
        // Update status based on thresholds
        updateStatus();
        
        // Raise events
        raiseEvent(new KPICalculatedEvent(
            LocalDateTime.now(),
            id.getValue(),
            companyId.getValue(),
            type,
            newValue.getValue(),
            targetValue.getValue()
        ));
        
        // Check threshold violations
        checkThresholds();
    }
    
    public void updateTarget(MetricValue newTarget) {
        this.targetValue = newTarget;
        this.updatedAt = LocalDateTime.now();
        updateStatus();
    }
    
    public void updateThresholds(MetricValue newMinimum, MetricValue newMaximum) {
        if (newMinimum.getValue() > newMaximum.getValue()) {
            throw new IllegalArgumentException("Minimum threshold cannot be greater than maximum threshold");
        }
        
        this.minimumThreshold = newMinimum;
        this.maximumThreshold = newMaximum;
        this.updatedAt = LocalDateTime.now();
        updateStatus();
        checkThresholds();
    }
    
    public void updateMeasurementPeriod(TimePeriod newPeriod) {
        this.measurementPeriod = newPeriod;
        this.updatedAt = LocalDateTime.now();
    }
    
    private void updateStatus() {
        if (currentValue.isZero() && lastCalculatedAt == null) {
            this.status = KPIStatus.NOT_MEASURED;
            return;
        }
        
        double current = currentValue.getValue();
        double min = minimumThreshold.getValue();
        double max = maximumThreshold.getValue();
        double target = targetValue.getValue();
        
        if (current < min) {
            this.status = KPIStatus.BELOW_THRESHOLD;
        } else if (current > max) {
            this.status = KPIStatus.ABOVE_THRESHOLD;
        } else if (Math.abs(current - target) <= target * 0.05) { // Within 5% of target
            this.status = KPIStatus.ON_TARGET;
        } else {
            this.status = KPIStatus.WITHIN_RANGE;
        }
    }
    
    private void checkThresholds() {
        double current = currentValue.getValue();
        
        if (current < minimumThreshold.getValue() || current > maximumThreshold.getValue()) {
            raiseEvent(new KPIThresholdExceededEvent(
                LocalDateTime.now(),
                id.getValue(),
                companyId.getValue(),
                type,
                current,
                minimumThreshold.getValue(),
                maximumThreshold.getValue(),
                current < minimumThreshold.getValue() ? "BELOW" : "ABOVE"
            ));
        }
    }
    
    public double getPerformancePercentage() {
        if (targetValue.isZero()) {
            return 0.0;
        }
        return (currentValue.getValue() / targetValue.getValue()) * 100.0;
    }
    
    public MetricValue getVarianceFromTarget() {
        return currentValue.subtract(targetValue);
    }
    
    public boolean isOnTarget() {
        return status == KPIStatus.ON_TARGET;
    }
    
    public boolean isWithinThresholds() {
        return status != KPIStatus.BELOW_THRESHOLD && status != KPIStatus.ABOVE_THRESHOLD;
    }
    
    public OptionalDouble getAverageValue(int lastNDataPoints) {
        if (historicalData.isEmpty()) {
            return OptionalDouble.empty();
        }
        
        int startIndex = Math.max(0, historicalData.size() - lastNDataPoints);
        return historicalData.subList(startIndex, historicalData.size())
            .stream()
            .mapToDouble(point -> point.getCurrentValue().getValue())
            .average();
    }
    
    public List<HistoricalDataPoint> getRecentHistory(int count) {
        if (historicalData.isEmpty()) {
            return new ArrayList<>();
        }
        
        int startIndex = Math.max(0, historicalData.size() - count);
        return new ArrayList<>(historicalData.subList(startIndex, historicalData.size()));
    }
    
    @Override
    public KPIId getId() {
        return id;
    }
    
    public CompanyId getCompanyId() {
        return companyId;
    }
    
    public ProductionLineId getLineId() {
        return lineId;
    }
    
    public KPIType getType() {
        return type;
    }
    
    public MetricValue getCurrentValue() {
        return currentValue;
    }
    
    public MetricValue getTargetValue() {
        return targetValue;
    }
    
    public MetricValue getMinimumThreshold() {
        return minimumThreshold;
    }
    
    public MetricValue getMaximumThreshold() {
        return maximumThreshold;
    }
    
    public TimePeriod getMeasurementPeriod() {
        return measurementPeriod;
    }
    
    public List<HistoricalDataPoint> getHistoricalData() {
        return new ArrayList<>(historicalData);
    }
    
    public KPIStatus getStatus() {
        return status;
    }
    
    public String getCalculationMethod() {
        return calculationMethod;
    }
    
    public LocalDateTime getLastCalculatedAt() {
        return lastCalculatedAt;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}