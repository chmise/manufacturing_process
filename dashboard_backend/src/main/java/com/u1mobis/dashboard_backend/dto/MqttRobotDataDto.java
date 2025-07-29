package com.u1mobis.dashboard_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MqttRobotDataDto {
    @JsonProperty("robot_id")
    private String robotId;
    
    @JsonProperty("status_text")
    private String statusText;
    
    private Double temperature;
    
    @JsonProperty("cycle_time")
    private Integer cycleTime;
    
    @JsonProperty("power_consumption")
    private Double powerConsumption;
    
    private Long timestamp;
}