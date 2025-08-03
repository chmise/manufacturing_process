package com.u1mobis.dashboard_backend.application.quality;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.inventory.model.MaterialId;
import com.u1mobis.dashboard_backend.domain.manufacturing.model.ProductionLineId;
import com.u1mobis.dashboard_backend.domain.quality.model.*;
import com.u1mobis.dashboard_backend.domain.quality.repository.QualityRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class QualityApplicationService {
    
    private final QualityRecordRepository qualityRecordRepository;
    
    public QualityApplicationService(QualityRecordRepository qualityRecordRepository) {
        this.qualityRecordRepository = qualityRecordRepository;
    }
    
    // Quality Record Operations
    public QualityRecord createQualityRecord(CompanyId companyId, ProductionLineId lineId,
                                           MaterialId materialId, String batchNumber, String productCode,
                                           InspectionType inspectionType, String inspectorId, String inspectorName) {
        QualityRecordId recordId = QualityRecordId.generate();
        QualityRecord record = new QualityRecord(recordId, companyId, lineId, materialId,
                                                batchNumber, productCode, inspectionType, inspectorId, inspectorName);
        return qualityRecordRepository.save(record);
    }
    
    public void startInspection(QualityRecordId recordId) {
        QualityRecord record = findQualityRecordById(recordId);
        record.startInspection();
        qualityRecordRepository.save(record);
    }
    
    public void addQualityMetric(QualityRecordId recordId, String metricName, double measuredValue,
                               double expectedValue, double tolerance, String unit) {
        QualityRecord record = findQualityRecordById(recordId);
        QualityMetric metric = new QualityMetric(metricName, measuredValue, expectedValue, tolerance, unit);
        record.addMetric(metric);
        qualityRecordRepository.save(record);
    }
    
    public void addDefect(QualityRecordId recordId, String defect) {
        QualityRecord record = findQualityRecordById(recordId);
        record.addDefect(defect);
        qualityRecordRepository.save(record);
    }
    
    public void addComment(QualityRecordId recordId, String comment) {
        QualityRecord record = findQualityRecordById(recordId);
        record.addComment(comment);
        qualityRecordRepository.save(record);
    }
    
    public void completeInspection(QualityRecordId recordId, QualityResult result, String correctiveAction) {
        QualityRecord record = findQualityRecordById(recordId);
        record.completeInspection(result, correctiveAction);
        qualityRecordRepository.save(record);
    }
    
    public void updateCorrectiveAction(QualityRecordId recordId, String correctiveAction) {
        QualityRecord record = findQualityRecordById(recordId);
        record.updateCorrectiveAction(correctiveAction);
        qualityRecordRepository.save(record);
    }
    
    // Batch Quality Operations
    public QualityRecord performBatchInspection(CompanyId companyId, ProductionLineId lineId,
                                              MaterialId materialId, String batchNumber, String productCode,
                                              InspectionType inspectionType, String inspectorId, String inspectorName,
                                              List<QualityMetricData> metrics) {
        // Create and start inspection
        QualityRecord record = createQualityRecord(companyId, lineId, materialId, batchNumber,
                                                 productCode, inspectionType, inspectorId, inspectorName);
        record.startInspection();
        
        // Add all metrics
        for (QualityMetricData metricData : metrics) {
            QualityMetric metric = new QualityMetric(metricData.getName(), metricData.getMeasuredValue(),
                                                   metricData.getExpectedValue(), metricData.getTolerance(), metricData.getUnit());
            record.addMetric(metric);
        }
        
        // Determine result based on metrics
        QualityResult result = determineQualityResult(record);
        String correctiveAction = result.isAcceptable() ? null : "Review failed metrics and take corrective action";
        
        record.completeInspection(result, correctiveAction);
        return qualityRecordRepository.save(record);
    }
    
    private QualityResult determineQualityResult(QualityRecord record) {
        if (record.hasDefects()) {
            return QualityResult.FAIL;
        }
        
        if (record.allMetricsWithinSpec()) {
            return QualityResult.PASS;
        }
        
        // If some metrics fail but no critical defects, might be conditional pass
        double passRate = record.getOverallPassRate();
        if (passRate >= 80.0) {
            return QualityResult.CONDITIONAL_PASS;
        } else if (passRate >= 60.0) {
            return QualityResult.REWORK_REQUIRED;
        } else {
            return QualityResult.FAIL;
        }
    }
    
    // Query Operations
    @Transactional(readOnly = true)
    public List<QualityRecord> findQualityRecordsByCompany(CompanyId companyId) {
        return qualityRecordRepository.findByCompanyId(companyId);
    }
    
    @Transactional(readOnly = true)
    public List<QualityRecord> findPendingInspections(CompanyId companyId) {
        return qualityRecordRepository.findPendingInspections(companyId);
    }
    
    @Transactional(readOnly = true)
    public List<QualityRecord> findFailedInspections(CompanyId companyId) {
        return qualityRecordRepository.findFailedInspections(companyId);
    }
    
    @Transactional(readOnly = true)
    public List<QualityRecord> findInspectionsByType(CompanyId companyId, InspectionType inspectionType) {
        return qualityRecordRepository.findByCompanyIdAndInspectionType(companyId, inspectionType);
    }
    
    @Transactional(readOnly = true)
    public List<QualityRecord> findInspectionsByDateRange(CompanyId companyId, LocalDateTime startDate, LocalDateTime endDate) {
        return qualityRecordRepository.findByDateRange(companyId, startDate, endDate);
    }
    
    @Transactional(readOnly = true)
    public List<QualityRecord> findInspectionsByBatch(String batchNumber) {
        return qualityRecordRepository.findByBatchNumber(batchNumber);
    }
    
    @Transactional(readOnly = true)
    public List<QualityRecord> findInspectionsByInspector(CompanyId companyId, String inspectorId) {
        return qualityRecordRepository.findByInspector(companyId, inspectorId);
    }
    
    @Transactional(readOnly = true)
    public QualityStatistics calculateQualityStatistics(CompanyId companyId, LocalDateTime startDate, LocalDateTime endDate) {
        List<QualityRecord> records = qualityRecordRepository.findByDateRange(companyId, startDate, endDate);
        
        long totalInspections = records.size();
        long passedInspections = records.stream()
            .mapToLong(record -> record.getResult().isAcceptable() ? 1 : 0)
            .sum();
        long failedInspections = totalInspections - passedInspections;
        
        double passRate = totalInspections > 0 ? (double) passedInspections / totalInspections * 100.0 : 0.0;
        
        double averageInspectionTime = records.stream()
            .filter(QualityRecord::isCompleted)
            .mapToLong(QualityRecord::getInspectionDurationMinutes)
            .average()
            .orElse(0.0);
        
        return new QualityStatistics(totalInspections, passedInspections, failedInspections,
                                   passRate, averageInspectionTime);
    }
    
    // Private helper methods
    private QualityRecord findQualityRecordById(QualityRecordId recordId) {
        return qualityRecordRepository.findById(recordId)
            .orElseThrow(() -> new IllegalArgumentException("Quality record not found: " + recordId.getValue()));
    }
    
    // Helper classes
    public static class QualityMetricData {
        private final String name;
        private final double measuredValue;
        private final double expectedValue;
        private final double tolerance;
        private final String unit;
        
        public QualityMetricData(String name, double measuredValue, double expectedValue, double tolerance, String unit) {
            this.name = name;
            this.measuredValue = measuredValue;
            this.expectedValue = expectedValue;
            this.tolerance = tolerance;
            this.unit = unit;
        }
        
        public String getName() { return name; }
        public double getMeasuredValue() { return measuredValue; }
        public double getExpectedValue() { return expectedValue; }
        public double getTolerance() { return tolerance; }
        public String getUnit() { return unit; }
    }
    
    public static class QualityStatistics {
        private final long totalInspections;
        private final long passedInspections;
        private final long failedInspections;
        private final double passRate;
        private final double averageInspectionTime;
        
        public QualityStatistics(long totalInspections, long passedInspections, long failedInspections,
                               double passRate, double averageInspectionTime) {
            this.totalInspections = totalInspections;
            this.passedInspections = passedInspections;
            this.failedInspections = failedInspections;
            this.passRate = passRate;
            this.averageInspectionTime = averageInspectionTime;
        }
        
        public long getTotalInspections() { return totalInspections; }
        public long getPassedInspections() { return passedInspections; }
        public long getFailedInspections() { return failedInspections; }
        public double getPassRate() { return passRate; }
        public double getAverageInspectionTime() { return averageInspectionTime; }
    }
}