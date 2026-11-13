package com.u1mobis.dashboard_backend.service;

import com.u1mobis.dashboard_backend.mqtt.MqttClientSupport;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledExecutorService;

/**
 * MQTT 메시지 발행 서비스 (/api/mqtt/publish에서 사용)
 */
@Service
@Slf4j
public class MQTTPublishService {

    @Value("${mqtt.enabled:true}")
    private boolean enabled;

    @Value("${mqtt.broker.url:tcp://localhost:1883}")
    private String brokerUrl;

    @Value("${mqtt.client.id:dashboard_publisher}")
    private String clientId;

    private MqttClient mqttClient;
    private ScheduledExecutorService connector;

    @PostConstruct
    public void initialize() {
        if (!enabled) {
            log.info("MQTT가 꺼져 있어(mqtt.enabled=false) 발행 서비스를 시작하지 않습니다.");
            return;
        }
        try {
            // 고유한 클라이언트 ID 생성. MemoryPersistence로 작업 폴더에 임시 파일을 만들지 않는다
            String uniqueClientId = clientId + "_" + System.currentTimeMillis();
            mqttClient = new MqttClient(brokerUrl, uniqueClientId, new MemoryPersistence());

            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.warn("MQTT 연결이 끊어졌습니다 (자동 재연결): {}",
                            cause != null ? cause.getMessage() : "원인 알 수 없음");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    // Publisher이므로 메시지 수신 처리 안함
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // 발행 완료 (별도 처리 없음)
                }
            });

            connector = MqttClientSupport.connectInBackground(
                    mqttClient, MqttClientSupport.defaultOptions(), "mqtt-publish-service", () -> { });

        } catch (MqttException e) {
            log.error("MQTT 발행 클라이언트 생성 실패 - 브로커: {}, 오류: {}", brokerUrl, e.getMessage());
        }
    }

    @PreDestroy
    public void cleanup() {
        MqttClientSupport.closeQuietly(mqttClient, connector);
    }

    /**
     * MQTT 메시지 발행
     */
    public void publishMessage(String topic, String message, int qos) throws MqttException {
        if (!isConnected()) {
            // 끊긴 경우 Paho 자동 재연결이 처리하므로 여기서는 실패만 알린다
            throw new MqttException(MqttException.REASON_CODE_CLIENT_NOT_CONNECTED);
        }

        MqttMessage mqttMessage = new MqttMessage(message.getBytes(StandardCharsets.UTF_8));
        mqttMessage.setQos(qos);
        mqttMessage.setRetained(false);

        mqttClient.publish(topic, mqttMessage);
        log.info("MQTT 메시지 발행 성공 - Topic: {}", topic);
    }

    /**
     * MQTT 브로커 연결 상태 확인
     */
    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected();
    }
}
