package com.u1mobis.dashboard_backend.interfaces.rest.company;

import com.u1mobis.dashboard_backend.application.company.CompanyApplicationService;
import com.u1mobis.dashboard_backend.domain.company.model.Company;
import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.company.model.IndustryType;
import com.u1mobis.dashboard_backend.interfaces.dto.company.CompanyCreateRequest;
import com.u1mobis.dashboard_backend.interfaces.dto.company.CompanyProfileSetupRequest;
import com.u1mobis.dashboard_backend.interfaces.dto.company.CompanyResponse;
import com.u1mobis.dashboard_backend.interfaces.dto.company.IndustryTypeResponse;
import com.u1mobis.dashboard_backend.interfaces.mapper.CompanyDtoMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {
    private final CompanyApplicationService companyApplicationService;
    private final CompanyDtoMapper dtoMapper;
    
    public CompanyController(CompanyApplicationService companyApplicationService, CompanyDtoMapper dtoMapper) {
        this.companyApplicationService = companyApplicationService;
        this.dtoMapper = dtoMapper;
    }
    
    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(@RequestBody CompanyCreateRequest request) {
        try {
            CompanyId companyId = companyApplicationService.createCompany(request.getName(), request.getCode());
            Optional<Company> company = companyApplicationService.findCompanyById(Long.valueOf(companyId.getValue()));
            
            if (company.isPresent()) {
                CompanyResponse response = dtoMapper.toResponse(company.get());
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{companyId}")
    public ResponseEntity<CompanyResponse> getCompany(@PathVariable Long companyId) {
        Optional<Company> company = companyApplicationService.findCompanyById(companyId);
        
        if (company.isPresent()) {
            CompanyResponse response = dtoMapper.toResponse(company.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @GetMapping("/code/{companyCode}")
    public ResponseEntity<CompanyResponse> getCompanyByCode(@PathVariable String companyCode) {
        Optional<Company> company = companyApplicationService.findCompanyByCode(companyCode);
        
        if (company.isPresent()) {
            CompanyResponse response = dtoMapper.toResponse(company.get());
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.notFound().build();
        }
    }
    
    @PostMapping("/{companyId}/profile")
    public ResponseEntity<Void> setupCompanyProfile(@PathVariable Long companyId, 
                                                   @RequestBody CompanyProfileSetupRequest request) {
        try {
            companyApplicationService.setupCompanyProfile(
                    companyId,
                    request.getIndustryTypeId(),
                    request.getCompanySize(),
                    request.getProductionType(),
                    request.getDailyCapacity(),
                    request.getAutomationLevel(),
                    request.getQualityStandards(),
                    request.getSpecialRequirements()
            );
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{companyId}/setup/complete")
    public ResponseEntity<Void> completeSetup(@PathVariable Long companyId) {
        try {
            companyApplicationService.completeCompanySetup(companyId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{companyId}/setup/status")
    public ResponseEntity<Boolean> getSetupStatus(@PathVariable Long companyId) {
        boolean isCompleted = companyApplicationService.isCompanySetupCompleted(companyId);
        return ResponseEntity.ok(isCompleted);
    }
    
    @GetMapping("/industry-types")
    public ResponseEntity<List<IndustryTypeResponse>> getAllIndustryTypes() {
        List<IndustryType> industryTypes = companyApplicationService.getAllActiveIndustryTypes();
        List<IndustryTypeResponse> responses = industryTypes.stream()
                .map(dtoMapper::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }
}