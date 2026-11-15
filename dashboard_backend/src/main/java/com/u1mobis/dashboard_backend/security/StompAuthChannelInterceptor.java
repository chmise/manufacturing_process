package com.u1mobis.dashboard_backend.security;

import com.u1mobis.dashboard_backend.security.CustomUserDetailsService.CustomUserPrincipal;
import com.u1mobis.dashboard_backend.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.security.Principal;

/**
 * STOMP(웹소켓) 메시지 인증.
 *
 * - CONNECT: Authorization 헤더의 액세스 토큰을 검사해서 세션에 사용자를 붙인다.
 * - SUBSCRIBE: 자기 회사 알림(/topic/alerts/{회사명})만 구독할 수 있다.
 * - SEND: 로그인한 세션만 보낼 수 있다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    static final String ALERT_TOPIC_PREFIX = "/topic/alerts/";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (command == StompCommand.CONNECT) {
            accessor.setUser(authenticate(accessor));
        } else if (command == StompCommand.SUBSCRIBE) {
            checkSubscription(accessor);
        } else if (command == StompCommand.SEND) {
            requireUser(accessor.getUser());
        }
        return message;
    }

    private UsernamePasswordAuthenticationToken authenticate(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader("Authorization");
        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            throw new MessagingException("웹소켓 연결에는 로그인 토큰이 필요합니다.");
        }
        String token = header.substring(BEARER_PREFIX.length());

        try {
            String username = jwtUtil.getUsernameFromToken(token);
            UserDetails user = userDetailsService.loadUserByUsername(username);
            if (!jwtUtil.validateAccessToken(token, user.getUsername())) {
                throw new MessagingException("유효하지 않은 토큰입니다.");
            }
            return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        } catch (MessagingException e) {
            throw e;
        } catch (UsernameNotFoundException e) {
            throw new MessagingException("토큰의 사용자를 찾을 수 없습니다.");
        } catch (RuntimeException e) {
            log.debug("웹소켓 토큰 검증 실패: {}", e.getMessage());
            throw new MessagingException("유효하지 않은 토큰입니다.");
        }
    }

    private void checkSubscription(StompHeaderAccessor accessor) {
        CustomUserPrincipal principal = requireUser(accessor.getUser());
        String destination = accessor.getDestination();
        if (destination == null || !destination.startsWith(ALERT_TOPIC_PREFIX)) {
            throw new MessagingException("구독할 수 없는 경로입니다: " + destination);
        }
        String company = destination.substring(ALERT_TOPIC_PREFIX.length());
        if (!company.equals(principal.getCompanyName())) {
            log.warn("다른 회사 알림 구독 차단 - 사용자: {}, 요청: {}", principal.getUsername(), destination);
            throw new MessagingException("다른 회사의 알림은 구독할 수 없습니다.");
        }
    }

    private CustomUserPrincipal requireUser(Principal user) {
        if (user instanceof UsernamePasswordAuthenticationToken auth
                && auth.getPrincipal() instanceof CustomUserPrincipal principal) {
            return principal;
        }
        throw new MessagingException("로그인이 필요합니다.");
    }
}
