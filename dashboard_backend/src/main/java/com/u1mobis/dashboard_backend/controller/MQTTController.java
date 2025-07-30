package com.u1mobis.dashboard_backend.controller;

import com.u1mobis.dashboard_backend.service.MQTTPublishService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * MQTT 메시지 발행을 위한 컨트롤러
 */
@RestController
@RequestMapping("/api/mqtt")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@RequiredArgsConstructor
@Slf4j
public class MQTTController {
    
    private final MQTTPublishService mqttPublishService;
    
    /**
     * MQTT 메시지 발행
     */
    @PostMapping("/publish")
    public ResponseEntity<Map<String, Object>> publishMessage(@RequestBody Map<String, Object> request) {
        try {
            String topic = (String) request.get("topic");
            String message = (String) request.get("message");
            Integer qos = (Integer) request.getOrDefault("qos", 1);
            
            log.info("MQTT 메시지 발행 요청 - Topic: {}, Message: {}", topic, message);
            
            // MQTT 메시지 발행
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