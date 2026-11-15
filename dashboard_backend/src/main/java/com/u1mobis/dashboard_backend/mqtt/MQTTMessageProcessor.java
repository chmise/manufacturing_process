package com.u1mobis.dashboard_backend.mqtt;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.u1mobis.dashboard_backend.dto.MqttRobotDataDto;
import com.u1mobis.dashboard_backend.entity.Company;
import com.u1mobis.dashboard_backend.repository.CompanyRepository;
import com.u1mobis.dashboard_backend.service.ConveyorService;
import com.u1mobis.dashboard_backend.service.EnvironmentService;
import com.u1mobis.dashboard_backend.service.KPICalculationService;
import com.u1mobis.dashboard_backend.service.ProductionService;
import com.u1mobis.dashboard_backend.service.RobotService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * MQTT 메시지 처리.
 *
 * 토픽 형식: factory/{회사코드}/... (예: factory/AB12CD34/1/operations)
 * 회사코드는 DB(companies.company_code)에서 찾는다. 등록되지 않은 회사의 메시지는 버린다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MQTTMessageProcessor {

    private final ProductionService productionService;
    private final KPICalculationService kpiCalculationService;
    private final EnvironmentService environmentService;
    private final ConveyorService conveyorService;
    private final RobotService robotService;
    private final CompanyRepository companyRepository;

    // 장비마다 보내는 필드가 조금씩 달라서, 모르는 필드는 무시한다
    private final ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public void processMQTTMessage(String topic, String payload) {
        try {
            log.debug("MQTT 메시지 수신 - 토픽: {}, 페이로드: {}", topic, payload);

            Optional<Company> companyOpt = resolveCompany(topic);
            if (companyOpt.isEmpty()) {
                log.warn("등록되지 않은 회사의 MQTT 메시지를 무시합니다: {}", topic);
                return;
            }
            Company company = companyOpt.get();
            String companyName = company.getCompanyName();

            JsonNode data = objectMapper.readTree(payload);

            if (isRobotTelemetryTopic(topic)) {
                // factory/{회사코드}/robot : 로봇 상태 (자기 회사 로봇만 갱신)
                MqttRobotDataDto robotData = objectMapper.treeToValue(data, MqttRobotDataDto.class);
                robotService.updateRobotFromMqtt(company.getCompanyId(), robotData);
            }
            else if (topic.contains("/production/started")) {
                Long lineId = extractLineIdFromTopic(topic);
                productionService.startProduction(
                    companyName,
                    lineId,
                    data.get("product_id").asText(),
                    data.get("target_quantity").asInt(),
                    LocalDateTime.parse(data.get("due_date").asText())
                );
                log.info("생산 시작 처리 완료 - 회사: {}, 라인: {}", companyName, lineId);
            }
            else if (topic.contains("/production/completed")) {
                Long lineId = extractLineIdFromTopic(topic);
                productionService.completeProduction(
                    companyName,
                    lineId,
                    data.get("product_id").asText(),
                    data.get("cycle_time").asDouble(),
                    data.get("quality").asText(),
                    LocalDateTime.parse(data.get("due_date").asText())
                );
                log.info("생산 완료 처리 완료 - 회사: {}, 라인: {}", companyName, lineId);
            }
            else if (topic.contains("/operations")) {
                Long lineId = extractLineIdFromTopic(topic);
                kpiCalculationService.processKPIData(
                    companyName,
                    lineId,
                    data.get("planned_time").asInt(),
                    data.get("downtime").asInt(),
                    data.get("target_cycle_time").asDouble(),
                    data.get("good_count").asInt(),
                    data.get("total_count").asInt(),
                    data.get("first_time_pass_count").asInt(),
                    data.get("on_time_delivery_count").asInt()
                );
                log.info("KPI 데이터 처리 완료 - 회사: {}", companyName);
            }
            else if (topic.contains("/environment")) {
                environmentService.saveEnvironmentData(
                    companyName,
                    data.get("temperature").asDouble(),
                    data.get("humidity").asDouble(),
                    data.get("air_quality").asInt()
                );
            }
            else if (topic.contains("/conveyor")) {
                Long lineId = extractLineIdFromTopic(topic);
                conveyorService.saveConveyorStatus(
                    companyName,
                    lineId,
                    data.get("command").asText(),
                    data.get("reason").asText()
                );
                log.info("컨베이어 상태 저장 완료 - 회사: {}, 라인: {}, 명령: {}",
                    companyName, lineId, data.get("command").asText());
            }
            else if (topic.contains("/product/moved")) {
                // 현재는 로그로만 남기고, 화면은 DB의 생산 현황을 사용한다
                log.info("제품 이동 - 회사: {}, 제품: {}, {} → {}",
                    companyName,
                    data.get("product_id").asText(),
                    data.get("from_station").asText(),
                    data.get("to_station").asText());
            }
            else if (topic.contains("/product/arrived/")) {
                log.info("제품 구역 도착 - 회사: {}, 제품: {}, 구역: {}",
                    companyName,
                    data.get("product_id").asText(),
                    data.get("area_type").asText());
            }
            else if (topic.contains("/work/started")) {
                log.info("로봇 작업 시작 - 회사: {}, 로봇: {}, 제품: {}, 작업: {}",
                    companyName,
                    extractRobotIdFromTopic(topic, company.getCompanyId()),
                    data.get("product_id").asText(),
                    data.get("door_type").asText());
            }
            else if (topic.contains("/work/completed")) {
                log.info("로봇 작업 완료 - 회사: {}, 로봇: {}, 제품: {}, 소요시간: {}초",
                    companyName,
                    extractRobotIdFromTopic(topic, company.getCompanyId()),
                    data.get("product_id").asText(),
                    data.get("actual_work_time").asInt());
            }
            else if (topic.contains("/robots/all/completed")) {
                log.info("전체 로봇 작업 완료 - 회사: {}, 라인: {}, 제품: {}, 총 소요시간: {}초",
                    companyName,
                    extractLineIdFromTopic(topic),
                    data.get("product_id").asText(),
                    data.get("total_work_time").asInt());
            }
            else if (topic.contains("/inspection/started")) {
                log.info("수밀검사 시작 - 회사: {}, 라인: {}, 제품: {}, 검사타입: {}",
                    companyName,
                    extractLineIdFromTopic(topic),
                    data.get("product_id").asText(),
                    data.get("inspection_type").asText());
            }
            else if (topic.contains("/inspection/completed")) {
                log.info("수밀검사 완료 - 회사: {}, 라인: {}, 제품: {}, 결과: {}, 누수감지: {}",
                    companyName,
                    extractLineIdFromTopic(topic),
                    data.get("product_id").asText(),
                    data.get("result").asText(),
                    data.get("leak_detected").asBoolean());
            }
            else {
                log.debug("처리하지 않는 토픽: {}", topic);
            }

        } catch (Exception e) {
            log.error("MQTT 메시지 처리 실패 - topic: {}, error: {}", topic, e.getMessage(), e);
        }
    }

    /**
     * factory/{회사코드}/... 토픽에서 회사를 찾는다.
     * 회사코드로 먼저 찾고, 없으면 예전 테스트 스크립트 호환을 위해 회사명으로 한 번 더 찾는다.
     */
    Optional<Company> resolveCompany(String topic) {
        String[] parts = topic.split("/");
        if (parts.length < 2 || !"factory".equals(parts[0]) || parts[1].isBlank()) {
            return Optional.empty();
        }
        String identifier = parts[1];
        Optional<Company> byCode = companyRepository.findByCompanyCode(identifier.toUpperCase());
        if (byCode.isPresent()) {
            return byCode;
        }
        return companyRepository.findByCompanyName(identifier);
    }

    /**
     * 로봇 상태 토픽(factory/{회사코드}/robot)인지 확인
     */
    static boolean isRobotTelemetryTopic(String topic) {
        String[] parts = topic.split("/");
        return parts.length == 3 && "factory".equals(parts[0]) && "robot".equals(parts[2]);
    }

    /**
     * 토픽에서 라인 ID를 추출합니다.
     * 토픽 형식: factory/{회사코드}/{lineId}/...
     */
    private Long extractLineIdFromTopic(String topic) {
        String[] parts = topic.split("/");
        if (parts.length >= 3) {
            try {
                return Long.parseLong(parts[2]);
            } catch (NumberFormatException e) {
                log.warn("라인 ID가 숫자가 아님, 기본값 1 사용: {}", topic);
                return 1L;
            }
        }
        log.warn("라인 ID를 추출할 수 없음, 기본값 1 사용: {}", topic);
        return 1L;
    }

    /**
     * 토픽에서 로봇 ID를 추출합니다. (로그 표시용)
     * 토픽 형식: factory/{회사코드}/{lineId}/{robotId}/work/started
     * DB의 robotId는 회사 ID가 붙은 형식이다. 예: 1_L1_ROBOT_01
     */
    private String extractRobotIdFromTopic(String topic, Long companyId) {
        String[] parts = topic.split("/");
        if (parts.length >= 4) {
            return companyId + "_" + parts[3];
        }
        return "UNKNOWN_ROBOT";
    }
}
