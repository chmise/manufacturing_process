package com.u1mobis.dashboard_backend.domain.quality.model;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.inventory.model.MaterialId;
import com.u1mobis.dashboard_backend.domain.manufacturing.model.ProductionLineId;
import com.u1mobis.dashboard_backend.shared.kernel.AggregateRoot;
import com.u1mobis.dashboard_backend.domain.quality.event.QualityInspectionCompletedEvent;
import com.u1mobis.dashboard_backend.domain.quality.event.QualityInspectionFailedEvent;
import com.u1mobis.dashboard_backend.domain.quality.event.QualityInspectionStartedEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class QualityRecord extends AggregateRoot<QualityRecordId> {
    private QualityRecordId id;
    private CompanyId companyId;
    private ProductionLineId lineId;
    private MaterialId materialId;
    private String batchNumber;
    private String productCode;
    private InspectionType inspectionType;
    private QualityResult result;
    private String inspectorId;
    private String inspectorName;
    private List<QualityMetric> metrics;
    private List<String> defects;
    private List<String> comments;
    private String correctiveAction;
    private LocalDateTime inspectionStartTime;
    private LocalDateTime inspectionEndTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    protected QualityRecord() {
        this.metrics = new ArrayList<>();
        this.defects = new ArrayList<>();
        this.comments = new ArrayList<>();
    }
    
    public QualityRecord(QualityRecordId id, CompanyId companyId, ProductionLineId lineId,
                        MaterialId materialId, String batchNumber, String productCode,
                        InspectionType inspectionType, String inspectorId, String inspectorName) {
        this.id = id;
        this.companyId = companyId;
        this.lineId = lineId;
        this.materialId = materialId;
        this.batchNumber = batchNumber;
        this.productCode = productCode;
        this.inspectionType = inspectionType;
        this.inspectorId = inspectorId;
        this.inspectorName = inspectorName;
        this.result = QualityResult.PENDING;
        this.metrics = new ArrayList<>();
        this.defects = new ArrayList<>();
        this.comments = new ArrayList<>();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void startInspection() {
        if (inspectionStartTime != null) {
            throw new IllegalStateException("Inspection already started");
        }
        
        this.inspectionStartTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        raiseEvent(new QualityInspectionStartedEvent(
            LocalDateTime.now(),
            id.getValue(),
            companyId.getValue(),
            batchNumber,
            inspectionType,
            inspectorId
        ));
    }
    
    public void addMetric(QualityMetric metric) {
        if (inspectionStartTime == null) {
            throw new IllegalStateException("Cannot add metrics before starting inspection");
        }
        if (result != QualityResult.PENDING) {
            throw new IllegalStateException("Cannot add metrics after inspection is completed");
        }
        
        this.metrics.add(metric);
        this.updatedAt = LocalDateTime.now();
    }
    
    public void addDefect(String defect) {
        if (defect == null || defect.trim().isEmpty()) {
            throw new IllegalArgumentException("Defect description cannot be empty");
        }
        
        this.defects.add(defect.trim());
        this.updatedAt = LocalDateTime.now();
    }
    
    public void addComment(String comment) {
        if (comment == null || comment.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment cannot be empty");
        }
        
        this.comments.add(comment.trim());
        this.updatedAt = LocalDateTime.now();
    }
    
    public void completeInspection(QualityResult finalResult, String correctiveAction) {
        if (inspectionStartTime == null) {
            throw new IllegalStateException("Cannot complete inspection that was not started");
        }
        if (result != QualityResult.PENDING) {
            throw new IllegalStateException("Inspection already completed");
        }
        if (finalResult == QualityResult.PENDING) {
            throw new IllegalArgumentException("Final result cannot be pending");
        }
        
        this.result = finalResult;
        this.correctiveAction = correctiveAction;
        this.inspectionEndTime = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        if (finalResult.isAcceptable()) {
            raiseEvent(new QualityInspectionCompletedEvent(
                LocalDateTime.now(),
                id.getValue(),
                companyId.getValue(),
                batchNumber,
                finalResult,
                getOverallPassRate()
            ));
        } else {
            raiseEvent(new QualityInspectionFailedEvent(
                LocalDateTime.now(),
                id.getValue(),
                companyId.getValue(),
                batchNumber,
                finalResult,
                new ArrayList<>(defects),
                correctiveAction
            ));
        }
    }
    
    public void updateCorrectiveAction(String newCorrectiveAction) {
        this.correctiveAction = newCorrectiveAction;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isCompleted() {
        return result != QualityResult.PENDING && inspectionEndTime != null;
    }
    
    public boolean isInProgress() {
        return inspectionStartTime != null && !isCompleted();
    }
    
    public boolean hasDefects() {
        return !defects.isEmpty();
    }
    
    public boolean allMetricsWithinSpec() {
        return metrics.stream().allMatch(QualityMetric::isWithinSpec);
    }
    
    public double getOverallPassRate() {
        if (metrics.isEmpty()) {
            return hasDefects() ? 0.0 : 100.0;
        }
        
        long passedMetrics = metrics.stream()
            .mapToLong(metric -> metric.isWithinSpec() ? 1 : 0)
            .sum();
        
        return (double) passedMetrics / metrics.size() * 100.0;
    }
    
    public List<QualityMetric> getFailedMetrics() {
        return metrics.stream()
            .filter(metric -> !metric.isWithinSpec())
            .collect(Collectors.toList());
    }
    
    public long getInspectionDurationMinutes() {
        if (inspectionStartTime == null || inspectionEndTime == null) {
            return 0;
        }
        
        return java.time.Duration.between(inspectionStartTime, inspectionEndTime).toMinutes();
    }
    
    @Override
    public QualityRecordId getId() {
        return id;
    }
    
    public CompanyId getCompanyId() {
        return companyId;
    }
    
    public ProductionLineId getLineId() {
        return lineId;
    }
    
    public MaterialId getMaterialId() {
        return materialId;
    }
    
    public String getBatchNumber() {
        return batchNumber;
    }
    
    public String getProductCode() {
        return productCode;
    }
    
    public InspectionType getInspectionType() {
        return inspectionType;
    }
    
    public QualityResult getResult() {
        return result;
    }
    
    public String getInspectorId() {
        return inspectorId;
    }
    
    public String getInspectorName() {
        return inspectorName;
    }
    
    public List<QualityMetric> getMetrics() {
        return new ArrayList<>(metrics);
    }
    
    public List<String> getDefects() {
        return new ArrayList<>(defects);
    }
    
    public List<String> getComments() {
        return new ArrayList<>(comments);
    }
    
    public String getCorrectiveAction() {
        return correctiveAction;
    }
    
    public LocalDateTime getInspectionStartTime() {
        return inspectionStartTime;
    }
    
    public LocalDateTime getInspectionEndTime() {
        return inspectionEndTime;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}