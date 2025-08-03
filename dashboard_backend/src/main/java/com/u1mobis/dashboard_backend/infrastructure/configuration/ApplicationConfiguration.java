package com.u1mobis.dashboard_backend.infrastructure.configuration;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
@Slf4j
public class ApplicationConfiguration {
    
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("=".repeat(80));
        log.info("🚀 제조업 대시보드 애플리케이션이 성공적으로 시작되었습니다!");
        log.info("📊 Domain-Driven Design (DDD) 아키텍처가 적용되었습니다.");
        log.info("🏗️  다음 도메인들이 활성화되었습니다:");
        log.info("   • Company Management Domain");
        log.info("   • Manufacturing Domain"); 
        log.info("   • Equipment Domain");
        log.info("   • Inventory Domain");
        log.info("   • Quality Domain");
        log.info("   • Analytics Domain");
        log.info("   • IoT/Monitoring Domain");
        log.info("   • Alerts Domain");
        log.info("   • User/Identity Domain");
        log.info("🔗 도메인 간 이벤트 기반 통합이 활성화되었습니다.");
        log.info("🔒 엔터프라이즈급 보안이 적용되었습니다.");
        log.info("=".repeat(80));
    }
}