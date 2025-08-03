package com.u1mobis.dashboard_backend.domain.equipment.repository;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.equipment.model.EquipmentId;
import com.u1mobis.dashboard_backend.domain.equipment.model.EquipmentStatus;
import com.u1mobis.dashboard_backend.domain.equipment.model.WorkStation;
import com.u1mobis.dashboard_backend.domain.equipment.model.WorkStationType;
import com.u1mobis.dashboard_backend.domain.manufacturing.model.ProductionLineId;

import java.util.List;
import java.util.Optional;

public interface WorkStationRepository {
    WorkStation save(WorkStation workStation);
    Optional<WorkStation> findById(EquipmentId id);
    List<WorkStation> findByCompanyId(CompanyId companyId);
    List<WorkStation> findByLineId(ProductionLineId lineId);
    List<WorkStation> findByCompanyIdAndStatus(CompanyId companyId, EquipmentStatus status);
    List<WorkStation> findByType(WorkStationType type);
    void delete(WorkStation workStation);
}