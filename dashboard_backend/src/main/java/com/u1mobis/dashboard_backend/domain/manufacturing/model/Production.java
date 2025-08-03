package com.u1mobis.dashboard_backend.domain.manufacturing.model;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.shared.kernel.AggregateRoot;
import com.u1mobis.dashboard_backend.domain.manufacturing.event.ProductionStartedEvent;
import com.u1mobis.dashboard_backend.domain.manufacturing.event.ProductionCompletedEvent;
import com.u1mobis.dashboard_backend.domain.manufacturing.event.ProductionProgressUpdatedEvent;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

public class Production extends AggregateRoot<ProductionOrderId> {
    private ProductionOrderId id;
    private CompanyId companyId;
    private ProductionLineId lineId;
    private ProductName productName;
    private ProductColor color;
    private ProductionState currentState;
    private ProductionProgress progress;
    private LocalDateTime startTime;
    private LocalDateTime dueDate;
    private LocalDateTime completedTime;
    private Map<String, Boolean> workStationStatus;
    private Position currentPosition;
    private int reworkCount;
    
    protected Production() {
        this.workStationStatus = new HashMap<>();
    }
    
    public Production(ProductionOrderId id, CompanyId companyId, ProductionLineId lineId,
                     ProductName productName, ProductColor color, LocalDateTime dueDate) {
        this.id = id;
        this.companyId = companyId;
        this.lineId = lineId;
        this.productName = productName;
        this.color = color;
        this.dueDate = dueDate;
        this.currentState = ProductionState.SCHEDULED;
        this.progress = new ProductionProgress(0);
        this.workStationStatus = new HashMap<>();
        this.currentPosition = Position.initial();
        this.reworkCount = 0;
        
        raiseEvent(new ProductionStartedEvent(
            LocalDateTime.now(),
            id.getValue(),
            companyId.getValue(),
            productName.getValue()
        ));
    }
    
    public void startProduction() {
        if (currentState != ProductionState.SCHEDULED) {
            throw new IllegalStateException("Cannot start production from current state: " + currentState);
        }
        
        this.currentState = ProductionState.IN_PROGRESS;
        this.startTime = LocalDateTime.now();
        this.progress = new ProductionProgress(5); // 시작 단계 5%
        
        raiseEvent(new ProductionStartedEvent(
            LocalDateTime.now(),
            id.getValue(),
            companyId.getValue(),
            productName.getValue()
        ));
    }
    
    public void moveToStation(WorkStation station) {
        if (currentState != ProductionState.IN_PROGRESS) {
            throw new IllegalStateException("Cannot move product when not in progress");
        }
        
        this.currentPosition = station.getPosition();
        updateProgress(station.getProgressPercentage());
        
        raiseEvent(new ProductionProgressUpdatedEvent(
            LocalDateTime.now(),
            id.getValue(),
            progress.getPercentage(),
            station.getName()
        ));
    }
    
    public void completeWorkAtStation(String stationName) {
        if (currentState != ProductionState.IN_PROGRESS) {
            throw new IllegalStateException("Cannot complete work when not in progress");
        }
        
        workStationStatus.put(stationName, true);
        
        // 모든 필수 작업이 완료되었는지 확인
        if (areAllRequiredWorkStationsCompleted()) {
            completeProduction();
        }
    }
    
    private void completeProduction() {
        this.currentState = ProductionState.COMPLETED;
        this.completedTime = LocalDateTime.now();
        this.progress = new ProductionProgress(100);
        
        raiseEvent(new ProductionCompletedEvent(
            LocalDateTime.now(),
            id.getValue(),
            companyId.getValue(),
            calculateCycleTime(),
            isOnTime()
        ));
    }
    
    public void requireRework(String reason) {
        if (currentState == ProductionState.COMPLETED) {
            throw new IllegalStateException("Cannot rework completed production");
        }
        
        this.reworkCount++;
        this.currentState = ProductionState.REWORK_REQUIRED;
        
        // 해당 스테이션 작업 상태 초기화
        workStationStatus.clear();
    }
    
    private void updateProgress(int progressPercentage) {
        this.progress = new ProductionProgress(progressPercentage);
    }
    
    private boolean areAllRequiredWorkStationsCompleted() {
        // 최소 3개 주요 스테이션 완료 필요 (예: Robot Work, Inspection, Final Assembly)
        String[] requiredStations = {"ROBOT_WORK", "INSPECTION", "FINAL_ASSEMBLY"};
        
        for (String station : requiredStations) {
            if (!workStationStatus.getOrDefault(station, false)) {
                return false;
            }
        }
        return true;
    }
    
    private double calculateCycleTime() {
        if (startTime == null || completedTime == null) {
            return 0.0;
        }
        
        return java.time.Duration.between(startTime, completedTime).toSeconds();
    }
    
    public boolean isOnTime() {
        if (completedTime == null) {
            return LocalDateTime.now().isBefore(dueDate);
        }
        return completedTime.isBefore(dueDate);
    }
    
    public boolean isDelayed() {
        return !isOnTime();
    }
    
    public boolean isFirstTimePass() {
        return reworkCount == 0;
    }
    
    @Override
    public ProductionOrderId getId() {
        return id;
    }
    
    public CompanyId getCompanyId() {
        return companyId;
    }
    
    public ProductionLineId getLineId() {
        return lineId;
    }
    
    public ProductName getProductName() {
        return productName;
    }
    
    public ProductColor getColor() {
        return color;
    }
    
    public ProductionState getCurrentState() {
        return currentState;
    }
    
    public ProductionProgress getProgress() {
        return progress;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
    
    public LocalDateTime getDueDate() {
        return dueDate;
    }
    
    public LocalDateTime getCompletedTime() {
        return completedTime;
    }
    
    public Position getCurrentPosition() {
        return currentPosition;
    }
    
    public int getReworkCount() {
        return reworkCount;
    }
    
    public Map<String, Boolean> getWorkStationStatus() {
        return new HashMap<>(workStationStatus);
    }
}