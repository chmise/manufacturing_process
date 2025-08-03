package com.u1mobis.dashboard_backend.domain.manufacturing.repository;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.manufacturing.model.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductionRepository {
    Production save(Production production);
    Optional<Production> findById(ProductionOrderId productionId);
    List<Production> findByCompanyId(CompanyId companyId);
    List<Production> findByCompanyIdAndState(CompanyId companyId, ProductionState state);
    List<Production> findActiveProductionsByLineId(ProductionLineId lineId);
    List<Production> findByCompanyIdAndDateRange(CompanyId companyId, LocalDateTime startDate, LocalDateTime endDate);
    List<Production> findCompletedProductionsByCompanyId(CompanyId companyId);
    List<Production> findDelayedProductions(CompanyId companyId);
    void delete(Production production);
    
    // 통계용 메서드들
    long countActiveProductionsByCompanyId(CompanyId companyId);
    long countCompletedProductionsByCompanyIdToday(CompanyId companyId);
    double getAverageCycleTimeByCompanyId(CompanyId companyId);
}