package com.u1mobis.dashboard_backend.infrastructure.persistence.company;

import com.u1mobis.dashboard_backend.domain.company.model.IndustryType;
import com.u1mobis.dashboard_backend.domain.company.model.IndustryTypeId;
import com.u1mobis.dashboard_backend.domain.company.repository.IndustryTypeRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class IndustryTypeRepositoryImpl implements IndustryTypeRepository {
    private final IndustryTypeJpaRepository jpaRepository;
    private final CompanyMapper mapper;
    
    public IndustryTypeRepositoryImpl(IndustryTypeJpaRepository jpaRepository, CompanyMapper mapper) {
        this.jpaRepository = jpaRepository;
        this.mapper = mapper;
    }
    
    @Override
    public List<IndustryType> findAllActive() {
        return jpaRepository.findAllActiveOrderByName()
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
    
    @Override
    public Optional<IndustryType> findById(IndustryTypeId industryTypeId) {
        return jpaRepository.findById(Long.valueOf(industryTypeId.getValue()))
                .map(mapper::toDomain);
    }
    
    @Override
    public Optional<IndustryType> findByCode(String industryCode) {
        return jpaRepository.findByIndustryCode(industryCode)
                .map(mapper::toDomain);
    }
}