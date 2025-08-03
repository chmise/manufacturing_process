package com.u1mobis.dashboard_backend.domain.equipment.repository;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.equipment.model.Conveyor;
import com.u1mobis.dashboard_backend.domain.equipment.model.EquipmentId;
import com.u1mobis.dashboard_backend.domain.equipment.model.EquipmentStatus;
import com.u1mobis.dashboard_backend.domain.manufacturing.model.ProductionLineId;

import java.util.List;
import java.util.Optional;

public interface ConveyorRepository {
    Conveyor save(Conveyor conveyor);
    Optional<Conveyor> findById(EquipmentId id);
    List<Conveyor> findByCompanyId(CompanyId companyId);
    List<Conveyor> findByLineId(ProductionLineId lineId);
    List<Conveyor> findByCompanyIdAndStatus(CompanyId companyId, EquipmentStatus status);
    void delete(Conveyor conveyor);
}