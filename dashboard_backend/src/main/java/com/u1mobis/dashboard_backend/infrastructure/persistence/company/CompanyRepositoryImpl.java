package com.u1mobis.dashboard_backend.infrastructure.persistence.company;

import com.u1mobis.dashboard_backend.domain.company.model.*;
import com.u1mobis.dashboard_backend.domain.company.repository.CompanyRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CompanyRepositoryImpl implements CompanyRepository {
    private final CompanyJpaRepository jpaRepository;
    private final CompanyMapper mapper;
    
    public CompanyRepositoryImpl(CompanyJpaRepository jpaRepository, CompanyMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    public Company save(Company company) {
        CompanyJpaEntity jpaEntity = mapper.toJpaEntity(company);
        CompanyJpaEntity savedEntity = jpaRepository.save(jpaEntity);
        Company savedCompany = mapper.toDomain(savedEntity);
        
        // ID가 새로 생성된 경우 설정
        if (company.getId() == null && savedEntity.getCompanyId() != null) {
            savedCompany.setId(new CompanyId(String.valueOf(savedEntity.getCompanyId())));
        }
        
        return savedCompany;
    }
    
    @Override
    public Optional<Company> findById(CompanyId companyId) {
        return jpaRepository.findByIdWithProfile(Long.valueOf(companyId.getValue()))
                .map(mapper::toDomain);
    }
    
    @Override
    public Optional<Company> findByCode(CompanyCode companyCode) {
        return jpaRepository.findByCompanyCodeWithProfile(companyCode.getValue())
                .map(mapper::toDomain);
    }
    
    @Override
    public boolean existsByCode(CompanyCode companyCode) {
        return jpaRepository.existsByCompanyCode(companyCode.getValue());
    }
    
    @Override
    public void delete(Company company) {
        if (company.getId() != null) {
            jpaRepository.deleteById(Long.valueOf(company.getId().getValue()));
        }
    }
}