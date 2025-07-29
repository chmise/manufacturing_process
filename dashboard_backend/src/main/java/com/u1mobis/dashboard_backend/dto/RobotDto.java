package com.u1mobis.dashboard_backend.dto;

import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder(toBuilder = true)
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
    
    // 계산된 값들 (백엔드에서 계산)
    private Double health;           // 건강도 (0-100)
    private Double utilization;      // 가동률 (0-100)
    private String alarmStatus;      // 알람 상태 (정상/경고/심각)
    private String connectionStatus; // 통신 상태 (온라인/오프라인)
}