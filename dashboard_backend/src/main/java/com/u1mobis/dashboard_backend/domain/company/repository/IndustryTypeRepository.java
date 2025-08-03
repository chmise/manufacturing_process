package com.u1mobis.dashboard_backend.domain.company.repository;

import com.u1mobis.dashboard_backend.domain.company.model.IndustryType;
import com.u1mobis.dashboard_backend.domain.company.model.IndustryTypeId;

import java.util.List;
import java.util.Optional;

public interface IndustryTypeRepository {
    List<IndustryType> findAllActive();
    Optional<IndustryType> findById(IndustryTypeId industryTypeId);
    Optional<IndustryType> findByCode(String industryCode);
}