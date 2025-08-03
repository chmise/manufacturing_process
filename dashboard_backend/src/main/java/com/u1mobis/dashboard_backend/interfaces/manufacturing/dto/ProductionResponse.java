package com.u1mobis.dashboard_backend.interfaces.manufacturing.dto;

import com.u1mobis.dashboard_backend.domain.manufacturing.model.Production;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionResponse {
    private String productionId;
    private String productName;
    private String companyId;
    private String lineId;
    private int targetQuantity;
    private int currentQuantity;
    private String status;
    private String currentStation;
    private LocalDateTime startTime;
    private LocalDateTime dueDate;
    private LocalDateTime completedTime;
    private Double cycleTime;
    private String quality;
    private Integer reworkCount;
    
    public static ProductionResponse from(Production production) {
        return ProductionResponse.builder()
            .productionId(production.getId().getValue())
            .productName(production.getProductName().getValue())
            .companyId(String.valueOf(production.getCompanyId().getValue()))
            .lineId(production.getLineId().getValue())
            .targetQuantity(100) // Default target quantity
            .currentQuantity(production.getProgress().getPercentage())
            .status(production.getCurrentState().name())
            .currentStation(production.getCurrentPosition() != null ? production.getCurrentPosition().toString() : "INITIAL")
            .startTime(production.getStartTime())
            .dueDate(production.getDueDate())
            .completedTime(production.getCompletedTime())
            .cycleTime(production.getCompletedTime() != null && production.getStartTime() != null ? 
                      (double) java.time.Duration.between(production.getStartTime(), production.getCompletedTime()).toSeconds() : null)
            .quality(production.isFirstTimePass() ? "PASS" : "REWORK")
            .reworkCount(production.getReworkCount())
            .build();
    }
}