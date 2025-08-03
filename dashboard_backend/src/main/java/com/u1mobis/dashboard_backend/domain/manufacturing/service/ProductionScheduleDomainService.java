package com.u1mobis.dashboard_backend.domain.manufacturing.service;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.manufacturing.model.*;
import com.u1mobis.dashboard_backend.domain.manufacturing.repository.ProductionScheduleRepository;

import java.time.LocalDateTime;
import java.util.List;

public class ProductionScheduleDomainService {
    private final ProductionScheduleRepository scheduleRepository;
    
    public ProductionScheduleDomainService(ProductionScheduleRepository scheduleRepository) {
        this.scheduleRepository = scheduleRepository;
    }
    
    public boolean hasScheduleConflict(CompanyId companyId, LocalDateTime startTime, LocalDateTime endTime, 
                                     String equipmentLine, ProductionScheduleId excludeScheduleId) {
        List<ProductionSchedule> existingSchedules = scheduleRepository
                .findByCompanyIdAndDateRange(companyId, startTime, endTime);
        
        return existingSchedules.stream()
                .filter(schedule -> excludeScheduleId == null || !schedule.getId().equals(excludeScheduleId))
                .filter(schedule -> schedule.getEquipmentLine() != null && 
                                  schedule.getEquipmentLine().equals(equipmentLine))
                .filter(schedule -> schedule.getStatus() != ProductionStatus.CANCELLED &&
                                  schedule.getStatus() != ProductionStatus.COMPLETED)
                .anyMatch(schedule -> isTimeOverlapping(schedule, startTime, endTime));
    }
    
    private boolean isTimeOverlapping(ProductionSchedule schedule, LocalDateTime startTime, LocalDateTime endTime) {
        LocalDateTime scheduleStart = schedule.getStartDateTime();
        LocalDateTime scheduleEnd = schedule.getEndDateTime();
        
        return startTime.isBefore(scheduleEnd) && endTime.isAfter(scheduleStart);
    }
    
    public ProductionSchedule createProductionSchedule(CompanyId companyId, ProductName productName,
                                                     LocalDateTime startDateTime, LocalDateTime endDateTime,
                                                     Quantity targetQuantity, String equipmentLine) {
        validateScheduleTime(startDateTime, endDateTime);
        
        if (hasScheduleConflict(companyId, startDateTime, endDateTime, equipmentLine, null)) {
            throw new IllegalArgumentException("Equipment line is already scheduled for the specified time period");
        }
        
        return new ProductionSchedule(companyId, productName, startDateTime, endDateTime, 
                                    targetQuantity, 2, null, equipmentLine);
    }
    
    public void validateScheduleTime(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            throw new IllegalArgumentException("Schedule start and end times cannot be null");
        }
        
        if (!startDateTime.isBefore(endDateTime)) {
            throw new IllegalArgumentException("Schedule start time must be before end time");
        }
        
        if (startDateTime.isBefore(LocalDateTime.now().minusDays(1))) {
            throw new IllegalArgumentException("Cannot schedule production more than 1 day in the past");
        }
    }
    
    public List<ProductionSchedule> getActiveSchedules(CompanyId companyId) {
        return scheduleRepository.findActiveSchedulesByCompanyId(companyId);
    }
    
    public boolean canStartProduction(ProductionSchedule schedule) {
        return schedule.getStatus() == ProductionStatus.SCHEDULED && 
               !LocalDateTime.now().isBefore(schedule.getStartDateTime().minusMinutes(30));
    }
}