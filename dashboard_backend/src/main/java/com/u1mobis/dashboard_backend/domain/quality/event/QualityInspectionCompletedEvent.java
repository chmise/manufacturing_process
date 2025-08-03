package com.u1mobis.dashboard_backend.domain.quality.event;

import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;
import com.u1mobis.dashboard_backend.domain.quality.model.QualityResult;

import java.time.LocalDateTime;

public class QualityInspectionCompletedEvent implements DomainEvent {
    private final LocalDateTime occurredOn;
    private final String qualityRecordId;
    private final String companyId;
    private final String batchNumber;
    private final QualityResult result;
    private final double passRate;
    
    public QualityInspectionCompletedEvent(LocalDateTime occurredOn, String qualityRecordId, String companyId,
                                         String batchNumber, QualityResult result, double passRate) {
        this.occurredOn = occurredOn;
        this.qualityRecordId = qualityRecordId;
        this.companyId = companyId;
        this.batchNumber = batchNumber;
        this.result = result;
        this.passRate = passRate;
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
    
    public double getPassRate() {
        return passRate;
    }
}