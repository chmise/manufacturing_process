package com.u1mobis.dashboard_backend.config;

import com.u1mobis.dashboard_backend.security.JwtAuthenticationFilter;
import jakarta.servlet.DispatcherType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // @PreAuthorize, @PostAuthorize 사용 가능
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    // 쉼표로 구분한 허용 출처 목록 (CORS_ALLOWED_ORIGINS)
    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String[] allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .csrf(csrf -> csrf.disable()) // JWT(헤더 방식)라 CSRF 토큰 불필요
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 로그인 안 한 요청은 403이 아니라 401로 응답 (프론트가 재로그인 처리)
            .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
            .authorizeHttpRequests(authz -> authz
                // 에러 응답(/error)은 그대로 내보낸다
                .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                .requestMatchers("/error").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // 로그인 · 회원가입 · 토큰 갱신
                .requestMatchers(HttpMethod.POST, "/api/user/login", "/api/user/register", "/api/user/refresh-token").permitAll()

                // 회사 등록 화면에서 로그인 전에 쓰는 API
                .requestMatchers(HttpMethod.POST, "/api/company/register").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/company/check-name/*").permitAll()

                // 웹소켓 핸드셰이크. 토큰 검사는 STOMP CONNECT 단계에서 한다 (StompAuthChannelInterceptor)
                .requestMatchers("/ws/**").permitAll()

                // Unity WebGL 빌드가 토큰 없이 직접 부르는 API.
                // Unity 원본 프로젝트가 저장소에 없어 빌드를 고칠 수 없으므로 읽기 전용 API만 열어둔다.
                .requestMatchers(HttpMethod.GET,
                        "/api/production/orders/line/*",
                        "/api/production/door/signals/line/*",
                        "/api/conveyor/status",
                        "/api/unity/health").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/interaction/click").permitAll()

                // 컨테이너 헬스체크
                .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()

                // 나머지는 모두 로그인 필요. 회사별 경로(/api/{companyName}/...)는
                // TenantAccessInterceptor가 소속 회사인지 한 번 더 확인한다.
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(allowedOriginPatterns(allowedOrigins));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L); // preflight 캐시 시간

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    static List<String> allowedOriginPatterns(String[] origins) {
        if (origins == null) {
            return List.of();
        }
        return Arrays.stream(origins)
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
}
