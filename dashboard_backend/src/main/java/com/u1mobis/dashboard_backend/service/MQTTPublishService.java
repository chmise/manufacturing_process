package com.u1mobis.dashboard_backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * MQTT 메시지 발행 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MQTTPublishService {
    
    @Value("${mqtt.broker.url:tcp://localhost:1883}")
    private String brokerUrl;
    
    @Value("${mqtt.client.id:dashboard_publisher}")
    private String clientId;
    
    private MqttClient mqttClient;
    private volatile boolean isConnected = false;
    
    @PostConstruct
    public void initialize() {
        try {
            // 고유한 클라이언트 ID 생성
            String uniqueClientId = clientId + "_" + System.currentTimeMillis();
            mqttClient = new MqttClient(brokerUrl, uniqueClientId);
            
            // 연결 옵션 설정
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setConnectionTimeout(10);
            options.setKeepAliveInterval(20);
            options.setAutomaticReconnect(true);
            
            // 콜백 설정
            mqttClient.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    log.warn("MQTT 연결이 끊어졌습니다: {}", cause.getMessage());
                    isConnected = false;
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    // Publisher이므로 메시지 수신 처리 안함
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    log.debug("MQTT 메시지 발행 완료: {}", token.getTopics()[0]);
                }
            });
            
            // MQTT 브로커에 연결
            mqttClient.connect(options);
            isConnected = true;
            log.info("MQTT 브로커에 연결되었습니다: {}", brokerUrl);
            
        } catch (Exception e) {
            log.error("MQTT 브로커 연결 실패: {}", e.getMessage(), e);
            isConnected = false;
        }
    }
    
    @PreDestroy
    public void cleanup() {
        try {
            if (mqttClient != null && mqttClient.isConnected()) {
                mqttClient.disconnect();
                mqttClient.close();
                log.info("MQTT 연결이 해제되었습니다");
            }
        } catch (Exception e) {
            log.error("MQTT 연결 해제 중 오류: {}", e.getMessage(), e);
        }
    }
    
    /**
     * MQTT 메시지 발행
     */
    public void publishMessage(String topic, String message, int qos) throws MqttException {
        if (!isConnected()) {
            // 연결이 끊어진 경우 재연결 시도
            reconnect();
        }
        
        if (!isConnected()) {
            throw new MqttException(MqttException.REASON_CODE_CLIENT_NOT_CONNECTED);
        }
        
        try {
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            mqttMessage.setQos(qos);
            mqttMessage.setRetained(false);
            
            mqttClient.publish(topic, mqttMessage);
            log.info("MQTT 메시지 발행 성공 - Topic: {}, Message: {}", topic, message);
            
        } catch (Exception e) {
            log.error("MQTT 메시지 발행 실패 - Topic: {}, Error: {}", topic, e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * MQTT 브로커 연결 상태 확인
     */
    public boolean isConnected() {
        return mqttClient != null && mqttClient.isConnected() && isConnected;
    }
    
    /**
     * MQTT 브로커 재연결
     */
    private void reconnect() {
        try {
            if (mqttClient != null && !mqttClient.isConnected()) {
                log.info("MQTT 브로커 재연결 시도...");
                mqttClient.reconnect();
                isConnected = true;
                log.info("MQTT 브로커 재연결 성공");
            }
        } catch (Exception e) {
            log.error("MQTT 브로커 재연결 실패: {}", e.getMessage(), e);
            isConnected = false;
        }
    }
}