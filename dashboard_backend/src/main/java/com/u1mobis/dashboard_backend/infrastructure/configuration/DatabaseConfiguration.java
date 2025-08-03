package com.u1mobis.dashboard_backend.infrastructure.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
@Slf4j
public class DatabaseConfiguration {
    
    @Value("${timeseries.mode:SIMULATION}")
    private String timeSeriesMode;
    
    @Value("${timeseries.retention.days:30}")
    private int retentionDays;
    
    @Bean
    @Primary  
    public String timeSeriesDataMode() {
        log.info("시계열 데이터는 {} 모드로 실행됩니다.", timeSeriesMode);
        return timeSeriesMode;
    }
    
    @Bean
    public Integer dataRetentionDays() {
        return retentionDays;
    }
}