package com.u1mobis.dashboard_backend.config;

import com.u1mobis.dashboard_backend.security.StompAuthChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    // REST와 같은 허용 출처 목록 (CORS_ALLOWED_ORIGINS). 같은 출처 요청은 항상 허용된다.
    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String[] allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 메시지 브로커 설정 - 클라이언트가 구독할 경로
        config.enableSimpleBroker("/topic");
        // 클라이언트가 메시지를 전송할 때 사용할 prefix
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(SecurityConfig.allowedOriginPatterns(allowedOrigins).toArray(String[]::new))
                .withSockJS(); // SockJS fallback 옵션
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // CONNECT 때 토큰 확인, SUBSCRIBE 때 자기 회사 알림인지 확인
        registration.interceptors(stompAuthChannelInterceptor);
    }
}
