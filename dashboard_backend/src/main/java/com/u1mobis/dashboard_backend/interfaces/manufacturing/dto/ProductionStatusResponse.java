package com.u1mobis.dashboard_backend.interfaces.manufacturing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionStatusResponse {
    private int processingCount;
    private long todayCompleted;
    private long todayGood;
    private int productionTarget;
    private double hourlyRate;
    private double avgCycleTime;
    private double oeePercentage;
    private double qualityRate;
    
    public static ProductionStatusResponse from(Object status) {
        // TODO: Implement proper mapping from domain object
        return ProductionStatusResponse.builder()
            .processingCount(0)
            .todayCompleted(0)
            .todayGood(0)
            .productionTarget(0)
            .hourlyRate(0.0)
            .avgCycleTime(0.0)
            .oeePercentage(0.0)
            .qualityRate(0.0)
            .build();
    }
}