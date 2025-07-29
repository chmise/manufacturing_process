package com.u1mobis.dashboard_backend.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RobotDto {
    private String robotId;
    private String robotName;
    private String robotType;
    private String stationCode;
    private String statusText;
    private Integer motorStatus;
    private Integer ledStatus;
    private Integer cycleTime;
    private Integer productionCount;
    private Double quality;
    private Double temperature;
    private Double powerConsumption;
    private LocalDateTime lastUpdate;
    private Long companyId;
    private Long lineId;
}