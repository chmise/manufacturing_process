package com.u1mobis.dashboard_backend.domain.quality.repository;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.inventory.model.MaterialId;
import com.u1mobis.dashboard_backend.domain.manufacturing.model.ProductionLineId;
import com.u1mobis.dashboard_backend.domain.quality.model.InspectionType;
import com.u1mobis.dashboard_backend.domain.quality.model.QualityRecord;
import com.u1mobis.dashboard_backend.domain.quality.model.QualityRecordId;
import com.u1mobis.dashboard_backend.domain.quality.model.QualityResult;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface QualityRecordRepository {
    QualityRecord save(QualityRecord qualityRecord);
    Optional<QualityRecord> findById(QualityRecordId id);
    List<QualityRecord> findByCompanyId(CompanyId companyId);
    List<QualityRecord> findByCompanyIdAndResult(CompanyId companyId, QualityResult result);
    List<QualityRecord> findByCompanyIdAndInspectionType(CompanyId companyId, InspectionType inspectionType);
    List<QualityRecord> findByBatchNumber(String batchNumber);
    List<QualityRecord> findByMaterialId(MaterialId materialId);
    List<QualityRecord> findByProductionLineId(ProductionLineId lineId);
    List<QualityRecord> findByInspector(CompanyId companyId, String inspectorId);
    List<QualityRecord> findByDateRange(CompanyId companyId, LocalDateTime startDate, LocalDateTime endDate);
    List<QualityRecord> findPendingInspections(CompanyId companyId);
    List<QualityRecord> findFailedInspections(CompanyId companyId);
    void delete(QualityRecord qualityRecord);
}