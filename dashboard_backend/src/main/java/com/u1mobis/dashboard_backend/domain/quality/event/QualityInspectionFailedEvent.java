package com.u1mobis.dashboard_backend.domain.quality.event;

import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;
import com.u1mobis.dashboard_backend.domain.quality.model.QualityResult;

import java.time.LocalDateTime;
import java.util.List;

public class QualityInspectionFailedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String qualityRecordId;
    private final String companyId;
    private final String batchNumber;
    private final QualityResult result;
    private final List<String> defects;
    private final String correctiveAction;
    
    public QualityInspectionFailedEvent(LocalDateTime occurredOn, String qualityRecordId, String companyId,
                                      String batchNumber, QualityResult result, List<String> defects,
                                      String correctiveAction) {
        this.occurredOn = occurredOn;
        this.qualityRecordId = qualityRecordId;
        this.companyId = companyId;
        this.batchNumber = batchNumber;
        this.result = result;
        this.defects = defects;
        this.correctiveAction = correctiveAction;
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
    
    public QualityResult getResult() {
        return result;
    }
    
    public List<String> getDefects() {
        return defects;
    }
    
    public String getCorrectiveAction() {
        return correctiveAction;
    }
}