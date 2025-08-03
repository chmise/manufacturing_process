package com.u1mobis.dashboard_backend.domain.inventory.repository;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.inventory.model.KeyMaterial;
import com.u1mobis.dashboard_backend.domain.inventory.model.MaterialId;
import com.u1mobis.dashboard_backend.domain.inventory.model.MaterialType;
import com.u1mobis.dashboard_backend.domain.inventory.model.Priority;
import com.u1mobis.dashboard_backend.domain.inventory.model.QualityStatus;

import java.util.List;
import java.util.Optional;

public interface KeyMaterialRepository {
    KeyMaterial save(KeyMaterial keyMaterial);
    Optional<KeyMaterial> findById(MaterialId id);
    List<KeyMaterial> findByCompanyId(CompanyId companyId);
    List<KeyMaterial> findByCompanyIdAndType(CompanyId companyId, MaterialType type);
    List<KeyMaterial> findByCompanyIdAndPriority(CompanyId companyId, Priority priority);
    List<KeyMaterial> findByCompanyIdAndQualityStatus(CompanyId companyId, QualityStatus status);
    List<KeyMaterial> findCriticalLevelMaterials(CompanyId companyId);
    List<KeyMaterial> findExpiredMaterials(CompanyId companyId);
    List<KeyMaterial> findAvailableForReservation(CompanyId companyId);
    void delete(KeyMaterial keyMaterial);
}