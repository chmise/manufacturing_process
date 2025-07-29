package com.u1mobis.dashboard_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.u1mobis.dashboard_backend.dto.MqttRobotDataDto;
import com.u1mobis.dashboard_backend.service.RobotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.messaging.MessagingException;
import org.springframework.scheduling.annotation.EnableAsync;

@Configuration
@EnableAsync
@RequiredArgsConstructor  // ← 추가
public class MqttConfig {

    private final RobotService robotService;  // ← 추가

    @Value("${mqtt.broker.url:tcp://localhost:1883}")
    private String brokerUrl;

    @Value("${mqtt.client.id:robot-backend}")
    private String clientId;

    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        
        options.setServerURIs(new String[]{brokerUrl});
        options.setCleanSession(true);
        options.setConnectionTimeout(30);
        options.setKeepAliveInterval(60);
        
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    @Bean
    public MessageProducer inbound() {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(clientId, mqttClientFactory(), "robot/data");
        
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return new MessageHandler() {  // ← 익명 클래스로 변경
            private final ObjectMapper objectMapper = new ObjectMapper();

            @Override
            public void handleMessage(Message<?> message) throws MessagingException {
                try {
                    String payload = message.getPayload().toString();
                    System.out.println("MQTT 메시지 수신: " + payload);

                    MqttRobotDataDto mqttData = objectMapper.readValue(payload, MqttRobotDataDto.class);
                    robotService.updateRobotFromMqtt(mqttData);

                } catch (Exception e) {
                    System.err.println("MQTT 메시지 처리 중 오류: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
    }
}