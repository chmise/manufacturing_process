package com.u1mobis.dashboard_backend.domain.quality.event;

import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;
import com.u1mobis.dashboard_backend.domain.quality.model.InspectionType;

import java.time.LocalDateTime;

public class QualityInspectionStartedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String qualityRecordId;
    private final String companyId;
    private final String batchNumber;
    private final InspectionType inspectionType;
    private final String inspectorId;
    
    public QualityInspectionStartedEvent(LocalDateTime occurredOn, String qualityRecordId, String companyId,
                                       String batchNumber, InspectionType inspectionType, String inspectorId) {
        this.occurredOn = occurredOn;
        this.qualityRecordId = qualityRecordId;
        this.companyId = companyId;
        this.batchNumber = batchNumber;
        this.inspectionType = inspectionType;
        this.inspectorId = inspectorId;
    }
    
    @Override
    public LocalDateTime occurredOn() {
        return occurredOn;
    }
    
    @Override
    public String aggregateId() {
        return qualityRecordId;
    }
    
    public String getQualityRecordId() {
        return qualityRecordId;
    }
    
    public String getCompanyId() {
        return companyId;
    }
    
    public String getBatchNumber() {
        return batchNumber;
    }
    
    public InspectionType getInspectionType() {
        return inspectionType;
    }
    
    public String getInspectorId() {
        return inspectorId;
    }
}