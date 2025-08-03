package com.u1mobis.dashboard_backend.interfaces.equipment.dto;

import com.u1mobis.dashboard_backend.domain.equipment.model.Robot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RobotResponse {
    private String robotId;
    private String name;
    private String companyId;
    private String lineId;
    private String type;
    private String status;
    private String position;
    private String configuration;
    private long productionCount;
    private LocalDateTime lastMaintenanceDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static RobotResponse from(Robot robot) {
        return RobotResponse.builder()
            .robotId(robot.getId().getValue())
            .name(robot.getName())
            .companyId(robot.getCompanyId().getValue().toString())
            .lineId(robot.getLineId().getValue())
            .type(robot.getType().name())
            .status(robot.getStatus().name())
            .position(robot.getPosition().toString())
            .configuration(robot.getConfiguration().toString())
            .productionCount(robot.getMetrics().getProductionCount())
            .lastMaintenanceDate(robot.getLastMaintenanceDate())
            .createdAt(robot.getCreatedAt())
            .updatedAt(robot.getUpdatedAt())
            .build();
    }
}