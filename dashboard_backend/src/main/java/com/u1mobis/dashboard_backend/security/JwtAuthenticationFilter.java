package com.u1mobis.dashboard_backend.security;

import com.u1mobis.dashboard_backend.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        final String requestTokenHeader = request.getHeader("Authorization");
        
        String username = null;
        String jwtToken = null;
        
        // JWT Token이 "Bearer " 형식인지 확인
        if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
            jwtToken = requestTokenHeader.substring(7);
            try {
                username = jwtUtil.getUsernameFromToken(jwtToken);
            } catch (Exception e) {
                log.warn("JWT Token에서 사용자명을 가져올 수 없습니다: {}", e.getMessage());
            }
        } else {
            log.debug("JWT Token이 Bearer로 시작하지 않습니다");
        }
        
        // 토큰을 검증하고 SecurityContext에 인증 정보 설정
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
            
            // 토큰이 유효한 경우 Spring Security에 인증 설정
            if (jwtUtil.validateToken(jwtToken, userDetails.getUsername())) {
                
                UsernamePasswordAuthenticationToken authToken = 
                    new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                        
                // JWT 토큰을 details에 저장 (권한 검증에 사용)
                authToken.setDetails(jwtToken);
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                log.debug("사용자 '{}' 인증 완료", username);
            }
        }
        
        filterChain.doFilter(request, response);
    }
}