package com.u1mobis.dashboard_backend.domain.company.repository;

import com.u1mobis.dashboard_backend.domain.company.model.*;

import java.util.Optional;

public interface CompanyRepository {
    Company save(Company company);
    Optional<Company> findById(CompanyId companyId);
    Optional<Company> findByCode(CompanyCode companyCode);
    boolean existsByCode(CompanyCode companyCode);
    void delete(Company company);
}