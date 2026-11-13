package com.u1mobis.dashboard_backend.mqtt;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 공장 데이터 MQTT 구독. 받은 메시지는 MQTTMessageProcessor가 처리한다.
 * 모든 토픽은 factory/{회사코드}/... 형식이다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MQTTSubscriber {

    static final String[] TOPICS = {
        "factory/+/environment",
        "factory/+/robot",
        "factory/+/+/operations",
        "factory/+/+/production/started",
        "factory/+/+/production/completed",
        "factory/+/+/conveyor",
        "factory/+/+/product/moved",
        "factory/+/+/product/arrived/+",
        "factory/+/+/+/work/started",
        "factory/+/+/+/work/completed",
        "factory/+/+/+/status",
        "factory/+/+/robots/all/completed",
        "factory/+/+/inspection/started",
        "factory/+/+/inspection/completed"
    };
    private static final int QOS = 1;

    private final MQTTMessageProcessor messageProcessor;

    @Value("${mqtt.enabled:true}")
    private boolean enabled;

    @Value("${mqtt.broker.url:tcp://localhost:1883}")
    private String brokerUrl;

    private MqttClient mqttClient;
    private ScheduledExecutorService connector;

    @PostConstruct
    public void initialize() {
        if (!enabled) {
            log.info("MQTT가 꺼져 있어(mqtt.enabled=false) 구독을 시작하지 않습니다.");
            return;
        }
        try {
            // MemoryPersistence: 작업 폴더에 Paho 임시 파일(.lck)을 만들지 않는다
            mqttClient = new MqttClient(brokerUrl, "dashboard-backend-" + System.currentTimeMillis(),
                    new MemoryPersistence());
            mqttClient.setCallback(new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    if (reconnect) {
                        // 재연결되면 구독이 사라지므로 다시 구독. 콜백 스레드를 막지 않도록 별도 스레드에서 실행
                        Thread thread = new Thread(MQTTSubscriber.this::subscribeAll, "mqtt-resubscribe");
                        thread.setDaemon(true);
                        thread.start();
                    }
                }

                @Override
                public void connectionLost(Throwable cause) {
                    log.warn("MQTT 연결 끊어짐, 자동으로 다시 연결합니다: {}",
                            cause != null ? cause.getMessage() : "원인 알 수 없음");
                }

                @Override
                public void messageArrived(String topic, MqttMessage message) {
                    String payload = new String(message.getPayload(), StandardCharsets.UTF_8);
                    messageProcessor.processMQTTMessage(topic, payload);
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // 구독 전용 클라이언트라 사용하지 않음
                }
            });

            connector = MqttClientSupport.connectInBackground(
                    mqttClient, MqttClientSupport.defaultOptions(), "mqtt-subscriber", this::subscribeAll);

        } catch (MqttException e) {
            log.error("MQTT 구독 클라이언트 생성 실패 - 브로커: {}, 오류: {}", brokerUrl, e.getMessage());
        }
    }

    private void subscribeAll() {
        try {
            int[] qos = new int[TOPICS.length];
            Arrays.fill(qos, QOS);
            mqttClient.subscribe(TOPICS, qos);
            log.info("MQTT 구독 완료 - 브로커: {}, 토픽 {}개", brokerUrl, TOPICS.length);
        } catch (MqttException e) {
            log.error("MQTT 구독 실패: {}", e.getMessage());
        }
    }

    @PreDestroy
    public void cleanup() {
        MqttClientSupport.closeQuietly(mqttClient, connector);
    }
}
