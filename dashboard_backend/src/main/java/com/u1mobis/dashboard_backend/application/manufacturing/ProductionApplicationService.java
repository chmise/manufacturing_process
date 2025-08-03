package com.u1mobis.dashboard_backend.application.manufacturing;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.manufacturing.model.*;
import com.u1mobis.dashboard_backend.domain.manufacturing.repository.ProductionRepository;
import com.u1mobis.dashboard_backend.domain.manufacturing.service.ProductionDomainService;
import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductionApplicationService {
    private final ProductionRepository productionRepository;
    private final ProductionDomainService productionDomainService;
    private final ApplicationEventPublisher eventPublisher;
    
    public ProductionApplicationService(ProductionRepository productionRepository,
                                      ProductionDomainService productionDomainService,
                                      ApplicationEventPublisher eventPublisher) {
        this.productionRepository = productionRepository;
        this.productionDomainService = productionDomainService;
        this.eventPublisher = eventPublisher;
    }
    
    public String startNewProduction(Long companyId, Long lineId, String productName, String color) {
        CompanyId company = new CompanyId(String.valueOf(companyId));
        ProductionLineId line = new ProductionLineId(String.valueOf(lineId));
        ProductName product = new ProductName(productName);
        ProductColor productColor = new ProductColor(color);
        
        // 입력 검증
        validateProductionInputs(productName, lineId);
        
        // 생산 용량 검증
        productionDomainService.validateProductionCapacity(company);
        
        if (!productionDomainService.canStartProduction(line)) {
            throw new com.u1mobis.dashboard_backend.domain.manufacturing.exception.ProductionCapacityExceededException(String.valueOf(lineId));
        }
        
        // 새 생산 생성
        Production production = productionDomainService.createNewProduction(company, line, product, productColor);
        
        // 생산 시작
        production.startProduction();
        
        Production savedProduction = productionRepository.save(production);
        publishDomainEvents(savedProduction);
        
        return savedProduction.getId().getValue();
    }
    
    public void moveProductionToStation(String productionId, String stationName, double x, double y, int progressPercentage) {
        ProductionOrderId id = new ProductionOrderId(productionId);
        Production production = productionRepository.findById(id)
                .orElseThrow(() -> new com.u1mobis.dashboard_backend.domain.manufacturing.exception.ProductionNotFoundException(productionId));
        
        // 입력 검증
        validateStationInputs(stationName, progressPercentage);
        
        Position stationPosition = Position.at(x, y);
        WorkStation station = new WorkStation(stationName, stationPosition, progressPercentage, 
                                            determineStationType(stationName));
        
        production.moveToStation(station);
        
        productionRepository.save(production);
        publishDomainEvents(production);
    }
    
    public void completeWorkAtStation(String productionId, String stationName) {
        ProductionOrderId id = new ProductionOrderId(productionId);
        Production production = productionRepository.findById(id)
                .orElseThrow(() -> new com.u1mobis.dashboard_backend.domain.manufacturing.exception.ProductionNotFoundException(productionId));
        
        // 입력 검증
        if (stationName == null || stationName.trim().isEmpty()) {
            throw new IllegalArgumentException("Station name cannot be null or empty");
        }
        
        production.completeWorkAtStation(stationName);
        
        productionRepository.save(production);
        publishDomainEvents(production);
    }
    
    public void requireRework(String productionId, String reason) {
        ProductionOrderId id = new ProductionOrderId(productionId);
        Production production = productionRepository.findById(id)
                .orElseThrow(() -> new com.u1mobis.dashboard_backend.domain.manufacturing.exception.ProductionNotFoundException(productionId));
        
        // 입력 검증
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Rework reason cannot be null or empty");
        }
        
        production.requireRework(reason);
        
        productionRepository.save(production);
        publishDomainEvents(production);
    }
    
    @Transactional(readOnly = true)
    public Optional<Production> findProductionById(String productionId) {
        return productionRepository.findById(new ProductionOrderId(productionId));
    }
    
    @Transactional(readOnly = true)
    public List<Production> getActiveProductions(Long companyId) {
        return productionRepository.findByCompanyIdAndState(new CompanyId(String.valueOf(companyId)), ProductionState.IN_PROGRESS);
    }
    
    @Transactional(readOnly = true)
    public List<Production> getCompanyProductions(Long companyId) {
        return productionRepository.findByCompanyId(new CompanyId(String.valueOf(companyId)));
    }
    
    @Transactional(readOnly = true)
    public List<Production> getProductionsByDateRange(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        return productionRepository.findByCompanyIdAndDateRange(new CompanyId(String.valueOf(companyId)), startDate, endDate);
    }
    
    @Transactional(readOnly = true)
    public ProductionDomainService.ProductionPerformanceMetrics getPerformanceMetrics(Long companyId) {
        return productionDomainService.calculatePerformanceMetrics(new CompanyId(String.valueOf(companyId)));
    }
    
    @Transactional(readOnly = true)
    public List<Production> getDelayedProductions(Long companyId) {
        return productionDomainService.getDelayedProductions(new CompanyId(String.valueOf(companyId)));
    }
    
    private WorkStation.WorkStationType determineStationType(String stationName) {
        String upperName = stationName.toUpperCase();
        
        if (upperName.contains("ROBOT")) {
            return WorkStation.WorkStationType.ROBOT_WORK;
        } else if (upperName.contains("INSPECTION") || upperName.contains("QUALITY")) {
            return WorkStation.WorkStationType.INSPECTION;
        } else if (upperName.contains("ASSEMBLY")) {
            return WorkStation.WorkStationType.ASSEMBLY;
        } else if (upperName.contains("PAINT")) {
            return WorkStation.WorkStationType.PAINTING;
        } else if (upperName.contains("PACKAGE")) {
            return WorkStation.WorkStationType.PACKAGING;
        } else {
            return WorkStation.WorkStationType.GENERAL;
        }
    }
    
    private void publishDomainEvents(Production production) {
        List<DomainEvent> events = production.getDomainEvents();
        events.forEach(eventPublisher::publishEvent);
        production.clearDomainEvents();
    }

    private void validateProductionInputs(String productName, Long lineId) {
        if (productName == null || productName.trim().isEmpty()) {
            throw new IllegalArgumentException("Product name cannot be null or empty");
        }
        if (lineId == null || lineId <= 0) {
            throw new IllegalArgumentException("Line ID must be a positive number");
        }
    }
    
    private void validateStationInputs(String stationName, int progressPercentage) {
        if (stationName == null || stationName.trim().isEmpty()) {
            throw new IllegalArgumentException("Station name cannot be null or empty");
        }
        if (progressPercentage < 0 || progressPercentage > 100) {
            throw new IllegalArgumentException("Progress percentage must be between 0 and 100");
        }
    }

    // MQTT compatibility methods
    public void startProduction(String companyName, Long lineId, String productId, 
                              Integer targetQuantity, LocalDateTime dueDate) {
        // TODO: Convert to domain objects and process production start
        System.out.println("Production started for " + companyName + " line " + lineId + 
                         " product " + productId);
    }

    public void completeProduction(String companyName, Long lineId, String productId, 
                                 Double cycleTime, String quality, LocalDateTime dueDate) {
        // TODO: Convert to domain objects and process production completion
        System.out.println("Production completed for " + companyName + " line " + lineId + 
                         " product " + productId + " quality: " + quality);
    }
}