package com.u1mobis.dashboard_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 스케줄링 설정
 * JWT 키 로테이션 등을 위한 스케줄링 활성화
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}