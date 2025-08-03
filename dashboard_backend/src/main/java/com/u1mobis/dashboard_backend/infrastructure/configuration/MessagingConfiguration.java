package com.u1mobis.dashboard_backend.infrastructure.configuration;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@Slf4j
public class MessagingConfiguration implements WebSocketMessageBrokerConfigurer {
    
    @Value("${mqtt.broker.url:tcp://localhost:1883}")
    private String mqttBrokerUrl;
    
    @Value("${mqtt.client.id:dashboard-backend}")
    private String mqttClientId;
    
    @Value("${mqtt.username:}")
    private String mqttUsername;
    
    @Value("${mqtt.password:}")
    private String mqttPassword;
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // Enable a simple memory-based message broker to carry the greeting
        // messages back to the client on destinations prefixed with "/topic".
        config.enableSimpleBroker("/topic", "/queue");
        
        // Designate the "/app" prefix for messages that are bound
        // for @MessageMapping-annotated methods.
        config.setApplicationDestinationPrefixes("/app");
        
        // Use a dedicated task executor for the message broker
        config.setPreservePublishOrder(true);
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register the "/ws" endpoint, enabling the SockJS protocol.
        // SockJS is used (both client and server side) to allow alternative
        // messaging options if WebSocket is not available.
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
    
    @Bean
    public MqttClient mqttClient() {
        try {
            String clientId = mqttClientId + "_" + System.currentTimeMillis();
            MqttClient client = new MqttClient(mqttBrokerUrl, clientId, new MemoryPersistence());
            
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setKeepAliveInterval(60);
            connOpts.setConnectionTimeout(30);
            connOpts.setAutomaticReconnect(true);
            
            if (!mqttUsername.isEmpty()) {
                connOpts.setUserName(mqttUsername);
            }
            if (!mqttPassword.isEmpty()) {
                connOpts.setPassword(mqttPassword.toCharArray());
            }
            
            try {
                client.connect(connOpts);
                log.info("MQTT 클라이언트 연결 성공: {}", mqttBrokerUrl);
            } catch (MqttException e) {
                log.warn("MQTT 브로커에 연결할 수 없습니다: {}. 시뮬레이션 모드로 실행됩니다.", e.getMessage());
            }
            
            return client;
        } catch (MqttException e) {
            log.error("MQTT 클라이언트 생성 실패: ", e);
            throw new RuntimeException("MQTT 클라이언트 초기화 실패", e);
        }
    }
}