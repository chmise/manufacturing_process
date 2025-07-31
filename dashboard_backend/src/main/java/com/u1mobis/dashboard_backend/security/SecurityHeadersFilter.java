package com.u1mobis.dashboard_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * 보안 헤더 추가 필터
 * OWASP 권장사항에 따른 보안 헤더 자동 추가
 */
@Component
@Order(1) // 가장 먼저 실행되도록 설정
public class SecurityHeadersFilter extends OncePerRequestFilter {
    
    @Value("${security.headers.hsts.enabled:true}")
    private boolean hstsEnabled;
    
    @Value("${security.headers.hsts.max-age:31536000}")
    private long hstsMaxAge;
    
    @Value("${security.headers.hsts.include-subdomains:true}")
    private boolean hstsIncludeSubdomains;
    
    @Value("${security.headers.csp.enabled:true}")
    private boolean cspEnabled;
    
    @Value("${security.headers.csp.policy:default-src 'self'; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' data:; connect-src 'self' ws: wss:; object-src 'none'; base-uri 'self';}")
    private String cspPolicy;
    
    @Value("${security.headers.frame-options:DENY}")
    private String frameOptions;
    
    @Value("${security.headers.content-type-options:nosniff}")
    private String contentTypeOptions;
    
    @Value("${security.headers.referrer-policy:strict-origin-when-cross-origin}")
    private String referrerPolicy;
    
    @Value("${security.headers.permissions-policy:geolocation=(), microphone=(), camera=()}")
    private String permissionsPolicy;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        addSecurityHeaders(request, response);
        filterChain.doFilter(request, response);
    }
    
    /**
     * 보안 헤더 추가
     */
    private void addSecurityHeaders(HttpServletRequest request, HttpServletResponse response) {
        
        // 1. HSTS (HTTP Strict Transport Security)
        if (hstsEnabled && isHttps(request)) {
            StringBuilder hstsValue = new StringBuilder();
            hstsValue.append("max-age=").append(hstsMaxAge);
            
            if (hstsIncludeSubdomains) {
                hstsValue.append("; includeSubDomains");
            }
            
            // Preload는 주의깊게 사용 (한번 설정하면 제거하기 어려움)
            // hstsValue.append("; preload");
            
            response.setHeader("Strict-Transport-Security", hstsValue.toString());
        }
        
        // 2. CSP (Content Security Policy)
        if (cspEnabled) {
            response.setHeader("Content-Security-Policy", cspPolicy);
            
            // IE/Edge 호환성을 위한 X-Content-Security-Policy
            response.setHeader("X-Content-Security-Policy", cspPolicy);
            
            // 웹킷 기반 브라우저 호환성을 위한 X-WebKit-CSP
            response.setHeader("X-WebKit-CSP", cspPolicy);
        }
        
        // 3. X-Frame-Options (클릭재킹 방지)
        response.setHeader("X-Frame-Options", frameOptions);
        
        // 4. X-Content-Type-Options (MIME 스니핑 방지)
        response.setHeader("X-Content-Type-Options", contentTypeOptions);
        
        // 5. X-XSS-Protection (XSS 필터 활성화)
        response.setHeader("X-XSS-Protection", "1; mode=block");
        
        // 6. Referrer-Policy (리퍼러 정보 제어)
        response.setHeader("Referrer-Policy", referrerPolicy);
        
        // 7. Permissions-Policy (기능 정책 - 이전 Feature-Policy)
        response.setHeader("Permissions-Policy", permissionsPolicy);
        
        // 8. X-Permitted-Cross-Domain-Policies (Flash/PDF 정책)
        response.setHeader("X-Permitted-Cross-Domain-Policies", "none");
        
        // 9. X-Download-Options (IE 다운로드 옵션)
        response.setHeader("X-Download-Options", "noopen");
        
        // 10. Cache-Control (민감한 정보 캐싱 방지)
        if (isSensitiveEndpoint(request)) {
            response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, private");
            response.setHeader("Pragma", "no-cache");
            response.setHeader("Expires", "0");
        }
        
        // 11. Server 헤더 제거 (서버 정보 숨김)
        response.setHeader("Server", "");
        
        // 12. Cross-Origin-Embedder-Policy
        response.setHeader("Cross-Origin-Embedder-Policy", "require-corp");
        
        // 13. Cross-Origin-Opener-Policy
        response.setHeader("Cross-Origin-Opener-Policy", "same-origin");
        
        // 14. Cross-Origin-Resource-Policy
        response.setHeader("Cross-Origin-Resource-Policy", "same-origin");
    }
    
    /**
     * HTTPS 요청인지 확인
     */
    private boolean isHttps(HttpServletRequest request) {
        return "https".equalsIgnoreCase(request.getScheme()) ||
               "https".equalsIgnoreCase(request.getHeader("X-Forwarded-Proto")) ||
               "on".equalsIgnoreCase(request.getHeader("X-Forwarded-Ssl"));
    }
    
    /**
     * 민감한 엔드포인트인지 확인 (캐싱 방지 대상)
     */
    private boolean isSensitiveEndpoint(HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        
        // 인증 관련 엔드포인트
        if (requestURI.contains("/login") || 
            requestURI.contains("/register") || 
            requestURI.contains("/user/me") ||
            requestURI.contains("/refresh-token")) {
            return true;
        }
        
        // 관리자 또는 민감한 데이터 엔드포인트
        if (requestURI.contains("/admin") || 
            requestURI.contains("/password") ||
            requestURI.contains("/security")) {
            return true;
        }
        
        // API 키나 토큰 관련 엔드포인트
        if (requestURI.contains("/api-key") || 
            requestURI.contains("/token") ||
            requestURI.contains("/secret")) {
            return true;
        }
        
        return false;
    }
}