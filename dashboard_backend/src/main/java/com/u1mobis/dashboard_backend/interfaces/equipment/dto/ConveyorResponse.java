package com.u1mobis.dashboard_backend.interfaces.equipment.dto;

import com.u1mobis.dashboard_backend.domain.equipment.model.Conveyor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConveyorResponse {
    private String conveyorId;
    private String name;
    private String companyId;
    private String lineId;
    private String status;
    private double speed;
    private String direction;
    private boolean sensorActive;
    private boolean emergencyStop;
    private boolean maintenanceMode;
    private long totalRuntimeSeconds;
    private LocalDateTime lastCommandTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static ConveyorResponse from(Conveyor conveyor) {
        return ConveyorResponse.builder()
            .conveyorId(conveyor.getId().getValue())
            .name(conveyor.getName())
            .companyId(conveyor.getCompanyId().getValue().toString())
            .lineId(conveyor.getLineId().getValue())
            .status(conveyor.getStatus().name())
            .speed(conveyor.getSpeed().getValue())
            .direction(conveyor.getDirection().name())
            .sensorActive(conveyor.isSensorActive())
            .emergencyStop(conveyor.isEmergencyStop())
            .maintenanceMode(conveyor.isMaintenanceMode())
            .totalRuntimeSeconds(conveyor.getTotalRuntimeSeconds())
            .lastCommandTime(conveyor.getLastCommandTime())
            .createdAt(conveyor.getCreatedAt())
            .updatedAt(conveyor.getUpdatedAt())
            .build();
    }
}