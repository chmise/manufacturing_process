package com.u1mobis.dashboard_backend.config;

import com.u1mobis.dashboard_backend.security.ContextAwareSecurityService;
import com.u1mobis.dashboard_backend.security.DynamicRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * 향상된 보안 설정
 * 동적 권한 관리 및 컨텍스트 인식 보안 통합
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
public class EnhancedSecurityConfig {
    
    @Autowired
    private DynamicRoleService dynamicRoleService;
    
    @Autowired
    private ContextAwareSecurityService contextAwareSecurityService;
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // CORS 설정
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // CSRF 비활성화 (API 서버이므로)
            .csrf(csrf -> csrf.disable())
            
            // 세션 관리 - Stateless (JWT 사용)
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 권한 설정
            .authorizeHttpRequests(authz -> authz
                // 공개 엔드포인트
                .requestMatchers(
                    "/api/auth/**",
                    "/api/company/register",
                    "/api/company/verify/**",
                    "/api/invitation/validate/**",
                    "/actuator/health",
                    "/actuator/info"
                ).permitAll()
                
                // 플랫폼 관리자 전용
                .requestMatchers("/api/admin/platform/**")
                .hasRole("PLATFORM_ADMIN")
                
                // 회사 관리자 이상
                .requestMatchers("/api/admin/company/**")
                .hasAnyRole("PLATFORM_ADMIN", "COMPANY_OWNER", "COMPANY_ADMIN")
                
                // 부서장 이상
                .requestMatchers("/api/admin/department/**")
                .hasAnyRole("PLATFORM_ADMIN", "COMPANY_OWNER", "COMPANY_ADMIN", "DEPARTMENT_HEAD")
                
                // 인증된 사용자
                .requestMatchers("/api/**")
                .authenticated()
                
                // 나머지 모든 요청은 인증 필요
                .anyRequest().authenticated()
            )
            
            // 보안 헤더 설정
            .headers(headers -> headers
                // HSTS (HTTP Strict Transport Security)
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000)
                    .includeSubDomains(true)
                    .preload(true)
                )
                
                // Content Security Policy
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives(
                        "default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                        "style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data: https:; " +
                        "font-src 'self' data:; " +
                        "connect-src 'self' ws: wss:; " +
                        "object-src 'none'; " +
                        "base-uri 'self'; " +
                        "frame-ancestors 'none';"
                    )
                )
                
                // 기타 보안 헤더
                .frameOptions(frame -> frame.deny())
                .contentTypeOptions(content -> {})
                .referrerPolicy(policy -> policy.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
            );
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // 허용할 오리진 (개발환경)
        configuration.setAllowedOriginPatterns(Arrays.asList(
            "http://localhost:3000",
            "http://localhost:3001",
            "https://*.ngrok.app",
            "https://*.ngrok.io"
        ));
        
        // 허용할 HTTP 메서드
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));
        
        // 허용할 헤더
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization",
            "Content-Type",
            "X-Requested-With",
            "Accept",
            "Origin",
            "Cache-Control",
            "X-File-Name",
            "X-Company-Code",
            "X-Device-Id",
            "X-Request-ID"
        ));
        
        // 응답에 노출할 헤더
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization",
            "X-Total-Count",
            "X-Current-Page",
            "X-Risk-Score",
            "X-Security-Level"
        ));
        
        // 자격 증명 허용
        configuration.setAllowCredentials(true);
        
        // 캐시 시간 (초)
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        
        return source;
    }
    
    /**
     * 컨텍스트 인식 보안 필터
     */
    private class ContextAwareSecurityFilter implements Filter {
        
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, 
                           FilterChain chain) throws IOException, ServletException {
            
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            
            try {
                // 보안 컨텍스트 평가 (인증된 요청에만)
                if (isAuthenticatedRequest(httpRequest)) {
                    Long userId = extractUserIdFromRequest(httpRequest);
                    if (userId != null) {
                        ContextAwareSecurityService.SecurityContextAssessment assessment = 
                            contextAwareSecurityService.assessSecurityContext(userId, null, httpRequest);
                        
                        // 위험도 점수를 응답 헤더에 추가 (디버깅용)
                        httpResponse.setHeader("X-Risk-Score", String.valueOf(assessment.getRiskScore()));
                        httpResponse.setHeader("X-Security-Level", assessment.getSecurityLevel().name());
                        
                        // 높은 위험도 시 추가 검증
                        if (assessment.getRiskScore() > 75) {
                            httpResponse.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            httpResponse.getWriter().write("{\"error\":\"High risk context detected\"}");
                            return;
                        }
                        
                        // 컨텍스트 제한사항 적용
                        if (assessment.getContextualRestriction() != null) {
                            dynamicRoleService.setContextualRestriction(
                                userId, null, assessment.getContextualRestriction());
                        }
                    }
                }
                
                chain.doFilter(request, response);
                
            } catch (Exception e) {
                // 보안 필터 오류 로깅
                System.err.println("Context-aware security filter error: " + e.getMessage());
                chain.doFilter(request, response);
            }
        }
        
        private boolean isAuthenticatedRequest(HttpServletRequest request) {
            String authorization = request.getHeader("Authorization");
            return authorization != null && authorization.startsWith("Bearer ");
        }
        
        private Long extractUserIdFromRequest(HttpServletRequest request) {
            // 실제 구현에서는 JWT 토큰에서 사용자 ID 추출
            // 현재는 간단한 더미 구현
            String userIdHeader = request.getHeader("X-User-ID");
            if (userIdHeader != null) {
                try {
                    return Long.parseLong(userIdHeader);
                } catch (NumberFormatException e) {
                    return null;
                }
            }
            return null;
        }
    }
    
    /**
     * 보안 감사 필터
     */
    private class SecurityAuditFilter implements Filter {
        
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, 
                           FilterChain chain) throws IOException, ServletException {
            
            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;
            
            long startTime = System.currentTimeMillis();
            
            try {
                chain.doFilter(request, response);
            } finally {
                // 보안 감사 로그 기록
                auditSecurityEvent(httpRequest, httpResponse, startTime);
            }
        }
        
        private void auditSecurityEvent(HttpServletRequest request, HttpServletResponse response, 
                                      long startTime) {
            try {
                long duration = System.currentTimeMillis() - startTime;
                String clientIp = extractClientIp(request);
                String userAgent = request.getHeader("User-Agent");
                String method = request.getMethod();
                String uri = request.getRequestURI();
                int statusCode = response.getStatus();
                
                // 보안 관련 이벤트만 로깅
                if (isSecurityRelevantRequest(uri) || isSecurityRelevantResponse(statusCode)) {
                    String logEntry = String.format(
                        "SECURITY_AUDIT: %s %s - IP:%s UA:%s Status:%d Duration:%dms",
                        method, uri, clientIp, 
                        userAgent != null ? userAgent.substring(0, Math.min(50, userAgent.length())) : "null",
                        statusCode, duration
                    );
                    
                    System.out.println(logEntry);
                    
                    // 실제 구현에서는 보안 로그 저장소에 기록
                    // securityAuditService.logSecurityEvent(logEntry);
                }
                
            } catch (Exception e) {
                // 감사 로깅 실패해도 요청 처리에는 영향 없도록
                System.err.println("Security audit logging failed: " + e.getMessage());
            }
        }
        
        private String extractClientIp(HttpServletRequest request) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            return request.getRemoteAddr();
        }
        
        private boolean isSecurityRelevantRequest(String uri) {
            return uri.contains("/auth/") || 
                   uri.contains("/admin/") || 
                   uri.contains("/company/register") ||
                   uri.contains("/invitation/");
        }
        
        private boolean isSecurityRelevantResponse(int statusCode) {
            return statusCode == 401 || statusCode == 403 || statusCode == 429;
        }
    }
}