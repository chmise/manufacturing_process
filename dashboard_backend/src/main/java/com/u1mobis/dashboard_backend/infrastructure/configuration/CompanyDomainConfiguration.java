package com.u1mobis.dashboard_backend.infrastructure.configuration;

import com.u1mobis.dashboard_backend.domain.company.repository.CompanyRepository;
import com.u1mobis.dashboard_backend.domain.company.service.CompanyDomainService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CompanyDomainConfiguration {
    
    @Bean
    public CompanyDomainService companyDomainService(CompanyRepository companyRepository) {
        return new CompanyDomainService(companyRepository);
    }
}