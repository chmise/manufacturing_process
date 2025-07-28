package com.u1mobis.dashboard_backend.mqtt;

import java.time.LocalDateTime;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.u1mobis.dashboard_backend.service.ConveyorService;
import com.u1mobis.dashboard_backend.service.EnvironmentService;
import com.u1mobis.dashboard_backend.service.KPICalculationService;
import com.u1mobis.dashboard_backend.service.ProductionService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
public class MQTTMessageProcessor {
    
    private final ProductionService productionService;
    private final KPICalculationService kpiCalculationService;
    private final EnvironmentService environmentService;
    private final ConveyorService conveyorService;
    
    // MQTT 메시지 처리 (가상의 메서드 - 실제로는 MQTT 라이브러리 사용)
    public void processMQTTMessage(String topic, String payload) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode data = mapper.readTree(payload);
            
            
            if (topic.contains("/production/completed")) {
                // 생산 완료 처리
                productionService.completeProduction(
                    data.get("product_id").asText(),
                    data.get("cycle_time").asDouble(),
                    data.get("quality").asText(),
                    LocalDateTime.parse(data.get("due_date").asText())
                );
            }
            else if (topic.contains("/operations")) {
                // KPI 데이터 처리
                kpiCalculationService.processKPIData(
                    data.get("planned_time").asInt(),
                    data.get("downtime").asInt(),
                    data.get("target_cycle_time").asDouble(),
                    data.get("good_count").asInt(),
                    data.get("total_count").asInt(),
                    data.get("first_time_pass_count").asInt(),
                    data.get("on_time_delivery_count").asInt()
                );
            }
            else if (topic.contains("/environment")) {
                // 환경 데이터 처리
                environmentService.saveEnvironmentData(
                    data.get("temperature").asDouble(),
                    data.get("humidity").asDouble(),
                    data.get("air_quality").asInt()
                );
            }
            else if (topic.contains("/conveyor")) {
                // 컨베이어 제어 처리 (추가)
                conveyorService.saveConveyorStatus(
                    data.get("command").asText(),
                    data.get("reason").asText()
                );
                log.info("컨베이어 상태 저장: {}", data.get("command").asText());
            }
            else {
                log.warn("처리되지 않은 토픽: {}", topic);
            }
            
        } catch (Exception e) {
            log.error("MQTT 메시지 처리 실패 - topic: {}, error: {}", topic, e.getMessage());
        }
    }
}