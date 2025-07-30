package com.u1mobis.dashboard_backend.mqtt;

import java.time.LocalDateTime;
import java.util.Map;

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
            log.info("=== MQTT 메시지 수신 시작 ===");
            log.info("토픽: {}", topic);
            log.info("페이로드: {}", payload);
            
            // 토픽에서 회사명 추출: factory/{companyName}/...
            String companyName = extractCompanyNameFromTopic(topic);
            if (companyName == null) {
                log.warn("토픽에서 회사명을 추출할 수 없습니다: {}", topic);
                return;
            }
            log.info("추출된 회사명: {}", companyName);
            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode data = mapper.readTree(payload);
            
            
            if (topic.contains("/production/started")) {
                log.info("생산 시작 처리 시작 - 회사: {}", companyName);
                
                // 토픽에서 lineId 추출
                Long lineId = extractLineIdFromTopic(topic);
                
                // 생산 시작 처리
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
                log.info("생산 완료 처리 시작 - 회사: {}", companyName);
                
                // 토픽에서 lineId 추출
                Long lineId = extractLineIdFromTopic(topic);
                
                // 생산 완료 처리
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
                log.info("KPI 데이터 처리 시작 - 회사: {}", companyName);
                
                // 토픽에서 lineId 추출 (factory/ms/b/operations -> lineId 2L)
                Long lineId = extractLineIdFromTopic(topic);
                
                // KPI 데이터 처리
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
                // 환경 데이터 처리
                environmentService.saveEnvironmentData(
                    companyName,
                    data.get("temperature").asDouble(),
                    data.get("humidity").asDouble(),
                    data.get("air_quality").asInt()
                );
            }
            else if (topic.contains("/conveyor")) {
                log.info("컨베이어 데이터 처리 시작 - 회사: {}", companyName);
                
                // 토픽에서 lineId 추출
                Long lineId = extractLineIdFromTopic(topic);
                
                // 컨베이어 제어 처리
                conveyorService.saveConveyorStatus(
                    companyName,
                    lineId,
                    data.get("command").asText(),
                    data.get("reason").asText()
                );
                log.info("컨베이어 상태 저장 완료 - 회사: {}, 라인: {}, 명령: {}", companyName, lineId, data.get("command").asText());
            }
            else if (topic.contains("/product/moved")) {
                log.info("제품 이동 처리 시작 - 회사: {}", companyName);
                
                // 제품 이동 데이터 처리 - 필요시 데이터베이스 업데이트
                // 현재는 로깅으로만 처리하고, 향후 Unity 연동 시 활용
                log.info("제품 이동 완료 - 제품: {}, {}에서 {}로", 
                    data.get("product_id").asText(), 
                    data.get("from_station").asText(), 
                    data.get("to_station").asText());
            }
            else if (topic.contains("/product/arrived/")) {
                log.info("제품 구역 도착 처리 시작 - 회사: {}", companyName);
                
                // 제품 도착 데이터 처리
                log.info("제품 구역 도착 완료 - 제품: {}, 구역: {}", 
                    data.get("product_id").asText(), 
                    data.get("area_type").asText());
            }
            else if (topic.contains("/work/started")) {
                log.info("로봇 작업 시작 처리 - 회사: {}", companyName);
                
                // 토픽에서 robotId 추출: factory/company/lineId/robotId/work/started
                String robotId = extractRobotIdFromTopic(topic);
                
                log.info("로봇 작업 시작 완료 - 로봇: {}, 제품: {}, 작업: {}", 
                    robotId, 
                    data.get("product_id").asText(), 
                    data.get("door_type").asText());
            }
            else if (topic.contains("/work/completed")) {
                log.info("로봇 작업 완료 처리 - 회사: {}", companyName);
                
                // 토픽에서 robotId 추출
                String robotId = extractRobotIdFromTopic(topic);
                
                log.info("로봇 작업 완료 - 로봇: {}, 제품: {}, 소요시간: {}초", 
                    robotId, 
                    data.get("product_id").asText(), 
                    data.get("actual_work_time").asInt());
            }
            else if (topic.contains("/robots/all/completed")) {
                log.info("전체 로봇 작업 완료 처리 - 회사: {}", companyName);
                
                Long lineId = extractLineIdFromTopic(topic);
                
                log.info("전체 로봇 작업 완료 - 라인: {}, 제품: {}, 총 소요시간: {}초", 
                    lineId, 
                    data.get("product_id").asText(), 
                    data.get("total_work_time").asInt());
            }
            else if (topic.contains("/inspection/started")) {
                log.info("수밀검사 시작 처리 - 회사: {}", companyName);
                
                Long lineId = extractLineIdFromTopic(topic);
                
                log.info("수밀검사 시작 완료 - 라인: {}, 제품: {}, 검사타입: {}", 
                    lineId, 
                    data.get("product_id").asText(), 
                    data.get("inspection_type").asText());
            }
            else if (topic.contains("/inspection/completed")) {
                log.info("수밀검사 완료 처리 - 회사: {}", companyName);
                
                Long lineId = extractLineIdFromTopic(topic);
                
                log.info("수밀검사 완료 - 라인: {}, 제품: {}, 결과: {}, 누수감지: {}", 
                    lineId, 
                    data.get("product_id").asText(), 
                    data.get("result").asText(),
                    data.get("leak_detected").asBoolean());
            }
            else {
                log.warn("처리되지 않은 토픽: {}", topic);
            }
            
        } catch (Exception e) {
            log.error("MQTT 메시지 처리 실패 - topic: {}, error: {}", topic, e.getMessage());
            log.error("에러 스택 트레이스:", e);
        }
    }
    
    /**
     * 토픽에서 회사 코드를 추출합니다.
     * 토픽 형식: factory/{companyCode}/...
     * companyCode를 통해 실제 회사명을 조회합니다.
     */
    private String extractCompanyNameFromTopic(String topic) {
        try {
            String[] parts = topic.split("/");
            log.info("토픽 파싱 결과: {}", String.join(", ", parts));
            if (parts.length >= 2 && "factory".equals(parts[0])) {
                String companyCode = parts[1];  // companyCode 추출
                log.info("추출된 회사 코드: '{}'", companyCode);
                
                // companyCode로 실제 회사명 조회
                return getCompanyNameByCode(companyCode);
            }
            log.warn("토픽 형식이 올바르지 않음: {}", topic);
            return null;
        } catch (Exception e) {
            log.error("토픽 파싱 실패: {}", topic, e);
            return null;
        }
    }
    
    /**
     * 회사 코드로 회사명 조회
     */
    private String getCompanyNameByCode(String companyCode) {
        try {
            // Company 엔티티에서 companyCode로 조회 후 companyName 반환
            // 실제 구현에서는 CompanyRepository 또는 CompanyService 사용
            // 현재는 간단히 매핑 처리
            Map<String, String> companyMapping = Map.of(
                "hyundai", "현대자동차",
                "samsung", "삼성전자", 
                "lg", "LG전자",
                "sk", "SK하이닉스",
                "ms", "현대자동차"  // 기존 호환성 유지
            );
            
            String companyName = companyMapping.get(companyCode.toLowerCase());
            if (companyName != null) {
                log.info("회사 코드 '{}' → 회사명 '{}'", companyCode, companyName);
                return companyName;
            } else {
                log.warn("등록되지 않은 회사 코드: {}", companyCode);
                return companyCode;  // 매핑이 없으면 코드 그대로 사용
            }
            
        } catch (Exception e) {
            log.error("회사명 조회 실패 - 코드: {}", companyCode, e);
            return companyCode;
        }
    }

    /**
     * 토픽에서 라인 ID를 추출합니다.
     * 토픽 형식: factory/{companyName}/{lineId}/operations
     * lineId는 숫자로 전달 (예: factory/ms/1/operations -> 1L, factory/ms/2/operations -> 2L)
     */
    private Long extractLineIdFromTopic(String topic) {
        try {
            String[] parts = topic.split("/");
            if (parts.length >= 3) {
                String lineIdStr = parts[2]; // 1, 2, 3 등
                Long lineId = Long.parseLong(lineIdStr);
                log.info("추출된 라인 ID: {}", lineId);
                return lineId;
            }
            log.warn("라인 ID를 추출할 수 없음, 기본값 1 사용: {}", topic);
            return 1L; // 기본값
        } catch (Exception e) {
            log.error("라인 ID 추출 실패, 기본값 1 사용: {}", topic, e);
            return 1L; // 기본값
        }
    }

    /**
     * 토픽에서 로봇 ID를 추출합니다.
     * 토픽 형식: factory/{companyCode}/{lineId}/{robotId}/work/started
     * robotId는 4번째 부분이지만, 실제 데이터베이스의 robotId는 회사ID가 포함된 형식
     * 예: factory/hyundai/1/L1_ROBOT_01/work/started -> 1_L1_ROBOT_01 (companyId_robotId)
     */
    private String extractRobotIdFromTopic(String topic) {
        try {
            String[] parts = topic.split("/");
            if (parts.length >= 4) {
                String companyCode = parts[1]; // hyundai, samsung 등
                String lineId = parts[2]; // 1, 2 등
                String robotIdFromTopic = parts[3]; // L1_ROBOT_01, L2_ROBOT_02 등
                
                // 회사 코드를 회사 ID로 변환 (실제로는 CompanyRepository에서 조회해야 함)
                Long companyId = getCompanyIdByCode(companyCode);
                
                // 데이터베이스 형식의 robotId 생성: {companyId}_{robotId}
                String fullRobotId = companyId + "_" + robotIdFromTopic;
                
                log.info("토픽에서 추출된 로봇 ID: {} -> 데이터베이스 로봇 ID: {}", robotIdFromTopic, fullRobotId);
                return fullRobotId;
            }
            log.warn("로봇 ID를 추출할 수 없음: {}", topic);
            return "UNKNOWN_ROBOT";
        } catch (Exception e) {
            log.error("로봇 ID 추출 실패: {}", topic, e);
            return "UNKNOWN_ROBOT";
        }
    }
    
    /**
     * 회사 코드로 회사 ID 조회 (간단한 매핑)
     */
    private Long getCompanyIdByCode(String companyCode) {
        // 실제로는 CompanyRepository에서 조회해야 하지만, 현재는 간단히 매핑
        Map<String, Long> companyIdMapping = Map.of(
            "hyundai", 1L,
            "samsung", 2L, 
            "lg", 3L,
            "sk", 4L,
            "ms", 1L  // 기존 호환성 유지
        );
        
        return companyIdMapping.getOrDefault(companyCode.toLowerCase(), 1L);
    }

}