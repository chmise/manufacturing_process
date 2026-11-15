package com.u1mobis.dashboard_backend.controller;

import com.u1mobis.dashboard_backend.security.CustomUserDetailsService.CustomUserPrincipal;
import com.u1mobis.dashboard_backend.service.MQTTPublishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * MQTT 메시지 발행을 위한 컨트롤러 (로그인 필요)
 *
 * 자기 회사 토픽(factory/{회사코드}/...)에만 발행할 수 있다.
 */
@RestController
@RequestMapping("/api/mqtt")
@RequiredArgsConstructor
@Slf4j
public class MQTTController {

    private final MQTTPublishService mqttPublishService;

    /**
     * MQTT 메시지 발행
     */
    @PostMapping("/publish")
    public ResponseEntity<Map<String, Object>> publishMessage(@RequestBody Map<String, Object> request,
                                                              @AuthenticationPrincipal CustomUserPrincipal principal) {
        String topic = request.get("topic") instanceof String t ? t : null;
        String message = request.get("message") instanceof String m ? m : null;
        int qos = request.get("qos") instanceof Number n ? n.intValue() : 1;

        if (topic == null || message == null || qos < 0 || qos > 2) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "topic, message가 필요하고 qos는 0~2 사이여야 합니다",
                "timestamp", System.currentTimeMillis()
            ));
        }
        if (principal == null || !isOwnCompanyTopic(topic, principal.getCompanyCode())) {
            log.warn("다른 회사 토픽으로의 MQTT 발행 차단 - 사용자: {}, 토픽: {}",
                    principal != null ? principal.getUsername() : null, topic);
            return ResponseEntity.status(403).body(Map.of(
                "success", false,
                "message", "자기 회사 토픽(factory/{회사코드}/...)에만 발행할 수 있습니다",
                "timestamp", System.currentTimeMillis()
            ));
        }

        try {
            log.info("MQTT 메시지 발행 요청 - Topic: {}", topic);
            mqttPublishService.publishMessage(topic, message, qos);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "MQTT 메시지가 성공적으로 발행되었습니다",
                "topic", topic,
                "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            log.error("MQTT 메시지 발행 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "message", "MQTT 메시지 발행 실패: " + e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }

    /**
     * factory/{회사코드}/... 형식이고 회사코드가 로그인한 사용자의 회사와 같은지 확인.
     * 와일드카드(+, #)는 발행 토픽에 쓸 수 없다.
     */
    static boolean isOwnCompanyTopic(String topic, String companyCode) {
        if (companyCode == null || topic.contains("+") || topic.contains("#")) {
            return false;
        }
        String[] parts = topic.split("/");
        return parts.length >= 3
                && "factory".equals(parts[0])
                && parts[1].equalsIgnoreCase(companyCode);
    }

    /**
     * MQTT 브로커 연결 상태 확인
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getBrokerStatus() {
        try {
            boolean isConnected = mqttPublishService.isConnected();

            return ResponseEntity.ok(Map.of(
                "success", true,
                "connected", isConnected,
                "message", isConnected ? "MQTT 브로커에 연결됨" : "MQTT 브로커 연결 안됨",
                "timestamp", System.currentTimeMillis()
            ));

        } catch (Exception e) {
            log.error("MQTT 브로커 상태 확인 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                "success", false,
                "connected", false,
                "message", "브로커 상태 확인 실패: " + e.getMessage(),
                "timestamp", System.currentTimeMillis()
            ));
        }
    }
}
