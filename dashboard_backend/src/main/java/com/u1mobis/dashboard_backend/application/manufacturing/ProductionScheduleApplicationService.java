package com.u1mobis.dashboard_backend.application.manufacturing;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.manufacturing.model.*;
import com.u1mobis.dashboard_backend.domain.manufacturing.repository.ProductionScheduleRepository;
import com.u1mobis.dashboard_backend.domain.manufacturing.service.ProductionScheduleDomainService;
import com.u1mobis.dashboard_backend.shared.kernel.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ProductionScheduleApplicationService {
    private final ProductionScheduleRepository scheduleRepository;
    private final ProductionScheduleDomainService scheduleDomainService;
    private final ApplicationEventPublisher eventPublisher;
    
    public ProductionScheduleApplicationService(ProductionScheduleRepository scheduleRepository,
                                              ProductionScheduleDomainService scheduleDomainService,
                                              ApplicationEventPublisher eventPublisher) {
        this.scheduleRepository = scheduleRepository;
        this.scheduleDomainService = scheduleDomainService;
        this.eventPublisher = eventPublisher;
    }
    
    public ProductionScheduleId createProductionSchedule(Long companyId, String productName,
                                                       LocalDateTime startDateTime, LocalDateTime endDateTime,
                                                       Integer targetQuantity, String unit,
                                                       String equipmentLine, String assignedWorker,
                                                       Integer priority, String description) {
        CompanyId company = new CompanyId(String.valueOf(companyId));
        ProductName product = new ProductName(productName);
        Quantity quantity = new Quantity(targetQuantity, unit != null ? unit : "개");
        
        ProductionSchedule schedule = scheduleDomainService.createProductionSchedule(
                company, product, startDateTime, endDateTime, quantity, equipmentLine);
        
        if (assignedWorker != null) {
            // Assuming we have a setter or constructor parameter for this
        }
        if (priority != null) {
            // Similar for priority
        }
        if (description != null) {
            schedule.setDescription(description);
        }
        
        ProductionSchedule savedSchedule = scheduleRepository.save(schedule);
        publishDomainEvents(savedSchedule);
        
        return savedSchedule.getId();
    }
    
    public void startProduction(Long scheduleId) {
        ProductionScheduleId id = new ProductionScheduleId(String.valueOf(scheduleId));
        ProductionSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Production schedule not found: " + scheduleId));
        
        if (!scheduleDomainService.canStartProduction(schedule)) {
            throw new IllegalStateException("Cannot start production at this time");
        }
        
        schedule.startProduction();
        scheduleRepository.save(schedule);
        publishDomainEvents(schedule);
    }
    
    public void pauseProduction(Long scheduleId, String reason) {
        ProductionScheduleId id = new ProductionScheduleId(String.valueOf(scheduleId));
        ProductionSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Production schedule not found: " + scheduleId));
        
        schedule.pauseProduction(reason);
        scheduleRepository.save(schedule);
        publishDomainEvents(schedule);
    }
    
    public void completeProduction(Long scheduleId, Integer actualQuantity) {
        ProductionScheduleId id = new ProductionScheduleId(String.valueOf(scheduleId));
        ProductionSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Production schedule not found: " + scheduleId));
        
        Quantity actual = new Quantity(actualQuantity, schedule.getTargetQuantity().getUnit());
        schedule.completeProduction(actual);
        scheduleRepository.save(schedule);
        publishDomainEvents(schedule);
    }
    
    public void updateProductionProgress(Long scheduleId, Integer currentQuantity) {
        ProductionScheduleId id = new ProductionScheduleId(String.valueOf(scheduleId));
        ProductionSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Production schedule not found: " + scheduleId));
        
        Quantity current = new Quantity(currentQuantity, schedule.getTargetQuantity().getUnit());
        schedule.updateProgress(current);
        scheduleRepository.save(schedule);
        publishDomainEvents(schedule);
    }
    
    public void cancelProduction(Long scheduleId, String reason) {
        ProductionScheduleId id = new ProductionScheduleId(String.valueOf(scheduleId));
        ProductionSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Production schedule not found: " + scheduleId));
        
        schedule.cancelProduction(reason);
        scheduleRepository.save(schedule);
        publishDomainEvents(schedule);
    }
    
    @Transactional(readOnly = true)
    public Optional<ProductionSchedule> findScheduleById(Long scheduleId) {
        return scheduleRepository.findById(new ProductionScheduleId(String.valueOf(scheduleId)));
    }
    
    @Transactional(readOnly = true)
    public List<ProductionSchedule> getCompanySchedules(Long companyId) {
        return scheduleRepository.findByCompanyId(new CompanyId(String.valueOf(companyId)));
    }
    
    @Transactional(readOnly = true)
    public List<ProductionSchedule> getActiveSchedules(Long companyId) {
        return scheduleDomainService.getActiveSchedules(new CompanyId(String.valueOf(companyId)));
    }
    
    @Transactional(readOnly = true)
    public List<ProductionSchedule> getSchedulesByStatus(Long companyId, String status) {
        ProductionStatus productionStatus = ProductionStatus.valueOf(status);
        return scheduleRepository.findByCompanyIdAndStatus(new CompanyId(String.valueOf(companyId)), productionStatus);
    }
    
    @Transactional(readOnly = true)
    public List<ProductionSchedule> getSchedulesByDateRange(Long companyId, LocalDateTime startDate, LocalDateTime endDate) {
        return scheduleRepository.findByCompanyIdAndDateRange(new CompanyId(String.valueOf(companyId)), startDate, endDate);
    }
    
    private void publishDomainEvents(ProductionSchedule schedule) {
        List<DomainEvent> events = schedule.getDomainEvents();
        events.forEach(eventPublisher::publishEvent);
        schedule.clearDomainEvents();
    }
}