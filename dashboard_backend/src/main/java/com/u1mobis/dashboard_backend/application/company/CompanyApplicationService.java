package com.u1mobis.dashboard_backend.application.company;

import com.u1mobis.dashboard_backend.domain.company.event.CompanySetupCompletedEvent;
import com.u1mobis.dashboard_backend.domain.company.model.*;
import com.u1mobis.dashboard_backend.domain.company.repository.CompanyRepository;
import com.u1mobis.dashboard_backend.domain.company.repository.IndustryTypeRepository;
import com.u1mobis.dashboard_backend.domain.company.service.CompanyDomainService;
import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CompanyApplicationService {
    private final CompanyRepository companyRepository;
    private final IndustryTypeRepository industryTypeRepository;
    private final CompanyDomainService companyDomainService;
    private final ApplicationEventPublisher eventPublisher;
    
    public CompanyApplicationService(CompanyRepository companyRepository,
                                   IndustryTypeRepository industryTypeRepository,
                                   CompanyDomainService companyDomainService,
                                   ApplicationEventPublisher eventPublisher) {
        this.companyRepository = companyRepository;
        this.industryTypeRepository = industryTypeRepository;
        this.companyDomainService = companyDomainService;
        this.eventPublisher = eventPublisher;
    }
    
    public CompanyId createCompany(String name, String code) {
        CompanyName companyName = new CompanyName(name);
        CompanyCode companyCode = new CompanyCode(code);
        
        Company company = companyDomainService.createNewCompany(companyName, companyCode);
        Company savedCompany = companyRepository.save(company);
        
        publishDomainEvents(savedCompany);
        
        return savedCompany.getId();
    }
    
    public void setupCompanyProfile(Long companyId, Long industryTypeId, String companySize,
                                   String productionType, Integer dailyCapacity, Integer automationLevel,
                                   String qualityStandards, String specialRequirements) {
        CompanyId id = new CompanyId(String.valueOf(companyId));
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));
        
        companyDomainService.validateCompanyForSetup(company);
        
        IndustryTypeId industryId = new IndustryTypeId(String.valueOf(industryTypeId));
        IndustryType industry = industryTypeRepository.findById(industryId)
                .orElseThrow(() -> new IllegalArgumentException("Industry type not found: " + industryTypeId));
        
        CompanySize size = CompanySize.valueOf(companySize);
        ProductionType type = ProductionType.valueOf(productionType);
        CompanyCapacity capacity = new CompanyCapacity(dailyCapacity, automationLevel);
        
        company.setupProfile(industryId, size, type, capacity, qualityStandards, specialRequirements);
        
        companyRepository.save(company);
        publishDomainEvents(company);
    }
    
    public void completeCompanySetup(Long companyId) {
        CompanyId id = new CompanyId(String.valueOf(companyId));
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found: " + companyId));
        
        company.completeSetup();
        
        IndustryType industry = industryTypeRepository.findById(company.getProfile().getIndustryTypeId())
                .orElseThrow(() -> new IllegalStateException("Industry type not found for company profile"));
        
        // Raise setup completed event
        CompanySetupCompletedEvent event = new CompanySetupCompletedEvent(
                LocalDateTime.now(), String.valueOf(companyId), industry.getCode());
        eventPublisher.publishEvent(event);
        
        companyRepository.save(company);
        publishDomainEvents(company);
    }
    
    @Transactional(readOnly = true)
    public Optional<Company> findCompanyById(Long companyId) {
        return companyRepository.findById(new CompanyId(String.valueOf(companyId)));
    }
    
    @Transactional(readOnly = true)
    public Optional<Company> findCompanyByCode(String code) {
        return companyRepository.findByCode(new CompanyCode(code));
    }
    
    @Transactional(readOnly = true)
    public List<IndustryType> getAllActiveIndustryTypes() {
        return industryTypeRepository.findAllActive();
    }
    
    @Transactional(readOnly = true)
    public boolean isCompanySetupCompleted(Long companyId) {
        return companyRepository.findById(new CompanyId(String.valueOf(companyId)))
                .map(Company::isSetupCompleted)
                .orElse(false);
    }
    
    private void publishDomainEvents(Company company) {
        List<DomainEvent> events = company.getDomainEvents();
        events.forEach(eventPublisher::publishEvent);
        company.clearDomainEvents();
    }
}