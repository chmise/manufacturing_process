package com.u1mobis.dashboard_backend.domain.equipment.repository;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.equipment.model.EquipmentId;
import com.u1mobis.dashboard_backend.domain.equipment.model.EquipmentStatus;
import com.u1mobis.dashboard_backend.domain.equipment.model.Robot;
import com.u1mobis.dashboard_backend.domain.manufacturing.model.ProductionLineId;

import java.util.List;
import java.util.Optional;

public interface RobotRepository {
    Robot save(Robot robot);
    Optional<Robot> findById(EquipmentId id);
    List<Robot> findByCompanyId(CompanyId companyId);
    List<Robot> findByLineId(ProductionLineId lineId);
    List<Robot> findByCompanyIdAndStatus(CompanyId companyId, EquipmentStatus status);
    void delete(Robot robot);
}