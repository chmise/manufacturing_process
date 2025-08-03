package com.u1mobis.dashboard_backend.domain.manufacturing.repository;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.manufacturing.model.ProductionSchedule;
import com.u1mobis.dashboard_backend.domain.manufacturing.model.ProductionScheduleId;
import com.u1mobis.dashboard_backend.domain.manufacturing.model.ProductionStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ProductionScheduleRepository {
    ProductionSchedule save(ProductionSchedule schedule);
    Optional<ProductionSchedule> findById(ProductionScheduleId scheduleId);
    List<ProductionSchedule> findByCompanyId(CompanyId companyId);
    List<ProductionSchedule> findByCompanyIdAndStatus(CompanyId companyId, ProductionStatus status);
    List<ProductionSchedule> findByCompanyIdAndDateRange(CompanyId companyId, LocalDateTime startDate, LocalDateTime endDate);
    List<ProductionSchedule> findActiveSchedulesByCompanyId(CompanyId companyId);
    void delete(ProductionSchedule schedule);
}