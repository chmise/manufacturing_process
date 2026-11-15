package com.u1mobis.dashboard_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.u1mobis.dashboard_backend.security.TenantAccessInterceptor;

import lombok.RequiredArgsConstructor;

// CORS는 SecurityConfig 한 곳에서만 설정한다 (두 곳에서 설정하면 서로 충돌할 수 있음)
@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final TenantAccessInterceptor tenantAccessInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // /api/{companyName}/... 요청은 로그인한 사용자의 회사와 같은 회사일 때만 통과
        registry.addInterceptor(tenantAccessInterceptor)
                .addPathPatterns("/api/**")
                // 회사 등록·이름 확인은 로그인 전에 쓰는 API라 검사에서 제외
                .excludePathPatterns("/api/company/**", "/api/user/login", "/api/user/register",
                        "/api/user/refresh-token");
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }
}
