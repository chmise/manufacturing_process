package com.u1mobis.dashboard_backend.domain.analytics.repository;

import com.u1mobis.dashboard_backend.domain.analytics.model.KPIData;
import com.u1mobis.dashboard_backend.domain.analytics.model.KPIId;
import com.u1mobis.dashboard_backend.domain.analytics.model.KPIStatus;
import com.u1mobis.dashboard_backend.domain.analytics.model.KPIType;
import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.manufacturing.model.ProductionLineId;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface KPIDataRepository {
    KPIData save(KPIData kpiData);
    Optional<KPIData> findById(KPIId id);
    List<KPIData> findByCompanyId(CompanyId companyId);
    List<KPIData> findByCompanyIdAndType(CompanyId companyId, KPIType type);
    List<KPIData> findByCompanyIdAndStatus(CompanyId companyId, KPIStatus status);
    List<KPIData> findByProductionLineId(ProductionLineId lineId);
    List<KPIData> findByCompanyIdAndLineId(CompanyId companyId, ProductionLineId lineId);
    List<KPIData> findKPIsRequiringAttention(CompanyId companyId);
    List<KPIData> findKPIsUpdatedAfter(CompanyId companyId, LocalDateTime dateTime);
    List<KPIData> findProductionRelatedKPIs(CompanyId companyId);
    List<KPIData> findQualityRelatedKPIs(CompanyId companyId);
    void delete(KPIData kpiData);
}