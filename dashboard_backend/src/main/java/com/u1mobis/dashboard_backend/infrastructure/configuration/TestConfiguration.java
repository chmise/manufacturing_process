package com.u1mobis.dashboard_backend.infrastructure.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile({"test", "integration"})
@Slf4j
public class TestConfiguration {
    
    @Bean
    @ConditionalOnProperty(name = "test.mode", havingValue = "integration")
    public String integrationTestMode() {
        log.info("통합 테스트 모드가 활성화되었습니다.");
        return "INTEGRATION_TEST";
    }
    
    @Bean
    @ConditionalOnProperty(name = "test.simulation", havingValue = "true", matchIfMissing = true)
    public String simulationMode() {
        log.info("시뮬레이션 테스트 모드가 활성화되었습니다.");
        return "SIMULATION";
    }
}