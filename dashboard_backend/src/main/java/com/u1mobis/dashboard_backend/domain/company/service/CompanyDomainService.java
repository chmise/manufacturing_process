package com.u1mobis.dashboard_backend.domain.company.service;

import com.u1mobis.dashboard_backend.domain.company.model.*;
import com.u1mobis.dashboard_backend.domain.company.repository.CompanyRepository;

public class CompanyDomainService {
    private final CompanyRepository companyRepository;
    
    public CompanyDomainService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }
    
    public boolean isCompanyCodeAvailable(CompanyCode companyCode) {
        return !companyRepository.existsByCode(companyCode);
    }
    
    public Company createNewCompany(CompanyName name, CompanyCode code) {
        if (!isCompanyCodeAvailable(code)) {
            throw new IllegalArgumentException("Company code already exists: " + code.getValue());
        }
        
        return new Company(name, code);
    }
    
    public void validateCompanyForSetup(Company company) {
        if (company.isSetupCompleted()) {
            throw new IllegalStateException("Company setup is already completed");
        }
    }
    
    public boolean canCompanyStartProduction(Company company) {
        return company.isSetupCompleted() && 
               company.getProfile() != null &&
               company.getProfile().getCapacity() != null &&
               company.getProfile().getCapacity().getDailyProductionCapacity() != null;
    }
}