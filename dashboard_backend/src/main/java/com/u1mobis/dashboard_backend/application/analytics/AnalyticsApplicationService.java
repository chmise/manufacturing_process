package com.u1mobis.dashboard_backend.application.analytics;

import com.u1mobis.dashboard_backend.domain.analytics.model.*;
import com.u1mobis.dashboard_backend.domain.analytics.repository.KPIDataRepository;
import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.manufacturing.model.ProductionLineId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.OptionalDouble;

@Service
@Transactional
public class AnalyticsApplicationService {
    
    private final KPIDataRepository kpiDataRepository;
    
    public AnalyticsApplicationService(KPIDataRepository kpiDataRepository) {
        this.kpiDataRepository = kpiDataRepository;
    }
    
    // KPI Management Operations
    public KPIData createKPI(CompanyId companyId, ProductionLineId lineId, KPIType type,
                           double targetValue, double minimumThreshold, double maximumThreshold,
                           TimePeriod measurementPeriod, String calculationMethod) {
        KPIId kpiId = KPIId.generate();
        MetricValue target = new MetricValue(targetValue, type.getUnit());
        MetricValue minimum = new MetricValue(minimumThreshold, type.getUnit());
        MetricValue maximum = new MetricValue(maximumThreshold, type.getUnit());
        
        KPIData kpiData = new KPIData(kpiId, companyId, lineId, type, target, minimum, maximum,
                                    measurementPeriod, calculationMethod);
        return kpiDataRepository.save(kpiData);
    }
    
    public void updateKPIValue(KPIId kpiId, double value, String source) {
        KPIData kpiData = findKPIById(kpiId);
        MetricValue metricValue = new MetricValue(value, kpiData.getType().getUnit());
        kpiData.updateValue(metricValue, source);
        kpiDataRepository.save(kpiData);
    }
    
    public void updateKPITarget(KPIId kpiId, double newTarget) {
        KPIData kpiData = findKPIById(kpiId);
        MetricValue target = new MetricValue(newTarget, kpiData.getType().getUnit());
        kpiData.updateTarget(target);
        kpiDataRepository.save(kpiData);
    }
    
    public void updateKPIThresholds(KPIId kpiId, double minimumThreshold, double maximumThreshold) {
        KPIData kpiData = findKPIById(kpiId);
        MetricValue minimum = new MetricValue(minimumThreshold, kpiData.getType().getUnit());
        MetricValue maximum = new MetricValue(maximumThreshold, kpiData.getType().getUnit());
        kpiData.updateThresholds(minimum, maximum);
        kpiDataRepository.save(kpiData);
    }
    
    // KPI Calculation Operations
    public void calculateProductionEfficiency(CompanyId companyId, ProductionLineId lineId,
                                            int actualOutput, int plannedOutput, double operatingTime, double availableTime) {
        double availability = availableTime > 0 ? (operatingTime / availableTime) * 100 : 0;
        double performance = plannedOutput > 0 ? ((double) actualOutput / plannedOutput) * 100 : 0;
        double oee = (availability * performance) / 10000; // Convert to percentage
        
        KPIData kpiData = findOrCreateKPI(companyId, lineId, KPIType.PRODUCTION_EFFICIENCY);
        kpiData.updateValue(MetricValue.percentage(oee), "Production Calculation");
        kpiDataRepository.save(kpiData);
    }
    
    public void calculateQualityRate(CompanyId companyId, ProductionLineId lineId,
                                   int goodParts, int totalParts) {
        double qualityRate = totalParts > 0 ? ((double) goodParts / totalParts) * 100 : 0;
        
        KPIData kpiData = findOrCreateKPI(companyId, lineId, KPIType.QUALITY_RATE);
        kpiData.updateValue(MetricValue.percentage(qualityRate), "Quality Calculation");
        kpiDataRepository.save(kpiData);
    }
    
    public void calculateThroughput(CompanyId companyId, ProductionLineId lineId,
                                  int unitsProduced, double timeHours) {
        double throughput = timeHours > 0 ? unitsProduced / timeHours : 0;
        
        KPIData kpiData = findOrCreateKPI(companyId, lineId, KPIType.THROUGHPUT);
        kpiData.updateValue(new MetricValue(throughput, "units/hour"), "Throughput Calculation");
        kpiDataRepository.save(kpiData);
    }
    
    public void calculateCycleTime(CompanyId companyId, ProductionLineId lineId,
                                 double totalProcessingTimeMinutes, int unitsProduced) {
        double cycleTime = unitsProduced > 0 ? totalProcessingTimeMinutes / unitsProduced : 0;
        
        KPIData kpiData = findOrCreateKPI(companyId, lineId, KPIType.CYCLE_TIME);
        kpiData.updateValue(MetricValue.time(cycleTime), "Cycle Time Calculation");
        kpiDataRepository.save(kpiData);
    }
    
    public void calculateDowntimeRate(CompanyId companyId, ProductionLineId lineId,
                                    double downtimeHours, double totalAvailableHours) {
        double downtimeRate = totalAvailableHours > 0 ? (downtimeHours / totalAvailableHours) * 100 : 0;
        
        KPIData kpiData = findOrCreateKPI(companyId, lineId, KPIType.DOWNTIME_RATE);
        kpiData.updateValue(MetricValue.percentage(downtimeRate), "Downtime Calculation");
        kpiDataRepository.save(kpiData);
    }
    
    public void calculateDefectRate(CompanyId companyId, ProductionLineId lineId,
                                  int defectiveParts, int totalParts) {
        double defectRate = totalParts > 0 ? ((double) defectiveParts / totalParts) * 100 : 0;
        
        KPIData kpiData = findOrCreateKPI(companyId, lineId, KPIType.DEFECT_RATE);
        kpiData.updateValue(MetricValue.percentage(defectRate), "Defect Rate Calculation");
        kpiDataRepository.save(kpiData);
    }
    
    // Batch KPI Calculation
    public void calculateAllProductionKPIs(CompanyId companyId, ProductionLineId lineId,
                                         ProductionMetrics metrics) {
        calculateProductionEfficiency(companyId, lineId, metrics.getActualOutput(),
                                    metrics.getPlannedOutput(), metrics.getOperatingTime(), metrics.getAvailableTime());
        
        calculateQualityRate(companyId, lineId, metrics.getGoodParts(), metrics.getTotalParts());
        
        calculateThroughput(companyId, lineId, metrics.getUnitsProduced(), metrics.getTimeHours());
        
        calculateCycleTime(companyId, lineId, metrics.getTotalProcessingTime(), metrics.getUnitsProduced());
        
        calculateDowntimeRate(companyId, lineId, metrics.getDowntimeHours(), metrics.getAvailableTime());
        
        calculateDefectRate(companyId, lineId, metrics.getDefectiveParts(), metrics.getTotalParts());
    }
    
    // Query Operations
    @Transactional(readOnly = true)
    public List<KPIData> findKPIsByCompany(CompanyId companyId) {
        return kpiDataRepository.findByCompanyId(companyId);
    }
    
    @Transactional(readOnly = true)
    public List<KPIData> findKPIsByType(CompanyId companyId, KPIType type) {
        return kpiDataRepository.findByCompanyIdAndType(companyId, type);
    }
    
    @Transactional(readOnly = true)
    public List<KPIData> findKPIsRequiringAttention(CompanyId companyId) {
        return kpiDataRepository.findKPIsRequiringAttention(companyId);
    }
    
    @Transactional(readOnly = true)
    public List<KPIData> findProductionKPIs(CompanyId companyId) {
        return kpiDataRepository.findProductionRelatedKPIs(companyId);
    }
    
    @Transactional(readOnly = true)
    public List<KPIData> findQualityKPIs(CompanyId companyId) {
        return kpiDataRepository.findQualityRelatedKPIs(companyId);
    }
    
    @Transactional(readOnly = true)
    public KPIDashboard generateDashboard(CompanyId companyId) {
        List<KPIData> allKPIs = kpiDataRepository.findByCompanyId(companyId);
        
        long totalKPIs = allKPIs.size();
        long onTargetKPIs = allKPIs.stream().mapToLong(kpi -> kpi.isOnTarget() ? 1 : 0).sum();
        long alertKPIs = allKPIs.stream().mapToLong(kpi -> kpi.isWithinThresholds() ? 0 : 1).sum();
        
        OptionalDouble averagePerformance = allKPIs.stream()
            .filter(kpi -> kpi.getStatus().hasData())
            .mapToDouble(KPIData::getPerformancePercentage)
            .average();
        
        return new KPIDashboard(totalKPIs, onTargetKPIs, alertKPIs,
                              averagePerformance.orElse(0.0), allKPIs);
    }
    
    // Private helper methods
    private KPIData findKPIById(KPIId kpiId) {
        return kpiDataRepository.findById(kpiId)
            .orElseThrow(() -> new IllegalArgumentException("KPI not found: " + kpiId.getValue()));
    }
    
    private KPIData findOrCreateKPI(CompanyId companyId, ProductionLineId lineId, KPIType type) {
        List<KPIData> existing = kpiDataRepository.findByCompanyIdAndType(companyId, type);
        
        // Find KPI for specific line or company-wide
        KPIData kpiData = existing.stream()
            .filter(kpi -> (lineId == null && kpi.getLineId() == null) ||
                          (lineId != null && lineId.equals(kpi.getLineId())))
            .findFirst()
            .orElse(null);
        
        if (kpiData == null) {
            // Create new KPI with default values
            double defaultTarget = getDefaultTarget(type);
            double defaultMin = getDefaultMinimum(type);
            double defaultMax = getDefaultMaximum(type);
            TimePeriod defaultPeriod = TimePeriod.daily(LocalDateTime.now());
            
            kpiData = createKPI(companyId, lineId, type, defaultTarget, defaultMin, defaultMax,
                              defaultPeriod, "Auto-generated");
        }
        
        return kpiData;
    }
    
    private double getDefaultTarget(KPIType type) {
        return switch (type) {
            case PRODUCTION_EFFICIENCY, QUALITY_RATE -> 85.0;
            case ON_TIME_DELIVERY -> 95.0;
            case THROUGHPUT -> 100.0;
            case CYCLE_TIME -> 60.0;
            case DOWNTIME_RATE, DEFECT_RATE -> 5.0;
            default -> 100.0;
        };
    }
    
    private double getDefaultMinimum(KPIType type) {
        return switch (type) {
            case PRODUCTION_EFFICIENCY, QUALITY_RATE -> 70.0;
            case ON_TIME_DELIVERY -> 85.0;
            case THROUGHPUT -> 80.0;
            case CYCLE_TIME -> 45.0;
            case DOWNTIME_RATE, DEFECT_RATE -> 0.0;
            default -> 80.0;
        };
    }
    
    private double getDefaultMaximum(KPIType type) {
        return switch (type) {
            case PRODUCTION_EFFICIENCY, QUALITY_RATE, ON_TIME_DELIVERY -> 100.0;
            case THROUGHPUT -> 150.0;
            case CYCLE_TIME -> 90.0;
            case DOWNTIME_RATE, DEFECT_RATE -> 15.0;
            default -> 120.0;
        };
    }
    
    // Helper classes
    public static class ProductionMetrics {
        private final int actualOutput;
        private final int plannedOutput;
        private final double operatingTime;
        private final double availableTime;
        private final int goodParts;
        private final int totalParts;
        private final int unitsProduced;
        private final double timeHours;
        private final double totalProcessingTime;
        private final double downtimeHours;
        private final int defectiveParts;
        
        public ProductionMetrics(int actualOutput, int plannedOutput, double operatingTime,
                               double availableTime, int goodParts, int totalParts, int unitsProduced,
                               double timeHours, double totalProcessingTime, double downtimeHours,
                               int defectiveParts) {
            this.actualOutput = actualOutput;
            this.plannedOutput = plannedOutput;
            this.operatingTime = operatingTime;
            this.availableTime = availableTime;
            this.goodParts = goodParts;
            this.totalParts = totalParts;
            this.unitsProduced = unitsProduced;
            this.timeHours = timeHours;
            this.totalProcessingTime = totalProcessingTime;
            this.downtimeHours = downtimeHours;
            this.defectiveParts = defectiveParts;
        }
        
        // Getters
        public int getActualOutput() { return actualOutput; }
        public int getPlannedOutput() { return plannedOutput; }
        public double getOperatingTime() { return operatingTime; }
        public double getAvailableTime() { return availableTime; }
        public int getGoodParts() { return goodParts; }
        public int getTotalParts() { return totalParts; }
        public int getUnitsProduced() { return unitsProduced; }
        public double getTimeHours() { return timeHours; }
        public double getTotalProcessingTime() { return totalProcessingTime; }
        public double getDowntimeHours() { return downtimeHours; }
        public int getDefectiveParts() { return defectiveParts; }
    }
    
    public static class KPIDashboard {
        private final long totalKPIs;
        private final long onTargetKPIs;
        private final long alertKPIs;
        private final double averagePerformance;
        private final List<KPIData> kpiData;
        
        public KPIDashboard(long totalKPIs, long onTargetKPIs, long alertKPIs,
                          double averagePerformance, List<KPIData> kpiData) {
            this.totalKPIs = totalKPIs;
            this.onTargetKPIs = onTargetKPIs;
            this.alertKPIs = alertKPIs;
            this.averagePerformance = averagePerformance;
            this.kpiData = kpiData;
        }
        
        public long getTotalKPIs() { return totalKPIs; }
        public long getOnTargetKPIs() { return onTargetKPIs; }
        public long getAlertKPIs() { return alertKPIs; }
        public double getAveragePerformance() { return averagePerformance; }
        public List<KPIData> getKpiData() { return kpiData; }
    }

    // Environment data operations (legacy methods for MQTT compatibility)
    public void saveEnvironmentData(String companyName, double temperature, double humidity, int airQuality) {
        // TODO: Implementation for environment data
        // For now, just log the data
        System.out.println("Environment data saved for " + companyName + ": temp=" + temperature + ", humidity=" + humidity + ", airQuality=" + airQuality);
    }

    // MQTT compatibility method
    public void processKPIData(String companyName, Long lineId, int plannedTime, int downtime, 
                             double targetCycleTime, int goodCount, int totalCount, 
                             int firstTimePassCount, int onTimeDeliveryCount) {
        // TODO: Convert to domain objects and process
        System.out.println("KPI data processed for " + companyName + " line " + lineId);
    }
}