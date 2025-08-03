package com.u1mobis.dashboard_backend.infrastructure.event;

import com.u1mobis.dashboard_backend.domain.alerts.model.Alert;
import com.u1mobis.dashboard_backend.domain.alerts.model.AlertId;
import com.u1mobis.dashboard_backend.domain.alerts.model.AlertSeverity;
import com.u1mobis.dashboard_backend.domain.alerts.model.AlertType;
import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.iot.event.SensorThresholdExceededEvent;
import com.u1mobis.dashboard_backend.domain.iot.event.SensorOfflineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CrossDomainEventHandler {
    
    // 센서 임계값 초과 시 알림 생성
    @EventListener
    @Async
    public void handleSensorThresholdExceeded(SensorThresholdExceededEvent event) {
        log.info("센서 임계값 초과 이벤트 처리: {}", event.getSensorId());
        
        AlertSeverity severity = event.isCriticalThreshold() ? AlertSeverity.CRITICAL : AlertSeverity.WARNING;
        AlertType alertType = switch (event.getSensorType()) {
            case TEMPERATURE -> AlertType.TEMPERATURE_HIGH;
            case HUMIDITY -> AlertType.HUMIDITY_HIGH;
            case AIR_QUALITY -> AlertType.AIR_QUALITY_POOR;
            default -> AlertType.SYSTEM_ANOMALY;
        };
        
        Alert alert = new Alert(
            AlertId.generate(),
            CompanyId.of(event.getCompanyId()),
            alertType,
            severity,
            "센서 임계값 초과",
            String.format("%s 센서에서 임계값을 초과했습니다. 현재값: %.2f, 임계값: %.2f", 
                         event.getSensorType().getDisplayName(), 
                         event.getCurrentValue(), 
                         event.getThresholdValue()),
            "SENSOR",
            event.getSensorId()
        );
        
        log.info("센서 임계값 초과 알림 생성됨: {}", alert.getId().getValue());
    }
    
    // 센서 오프라인 시 알림 생성
    @EventListener
    @Async
    public void handleSensorOffline(SensorOfflineEvent event) {
        log.info("센서 오프라인 이벤트 처리: {}", event.getSensorId());
        
        Alert alert = new Alert(
            AlertId.generate(),
            CompanyId.of(event.getCompanyId()),
            AlertType.SENSOR_OFFLINE,
            event.isCriticalOutage() ? AlertSeverity.CRITICAL : AlertSeverity.WARNING,
            "센서 통신 장애",
            String.format("%s 센서가 %d분간 오프라인 상태입니다", 
                         event.getSensorType().getDisplayName(), 
                         event.getOfflineDurationMinutes()),
            "SENSOR",
            event.getSensorId()
        );
        
        log.info("센서 오프라인 알림 생성됨: {}", alert.getId().getValue());
    }
    
    // 일반적인 시스템 알림 생성을 위한 헬퍼 메서드
    public void createSystemAlert(String companyId, AlertType type, AlertSeverity severity, 
                                 String title, String message, String source, String sourceId) {
        try {
            Alert alert = new Alert(
                AlertId.generate(),
                CompanyId.of(companyId),
                type,
                severity,
                title,
                message,
                source,
                sourceId
            );
            
            log.info("시스템 알림 생성됨: {} - {}", alert.getId().getValue(), title);
        } catch (Exception e) {
            log.error("시스템 알림 생성 실패: {}", title, e);
        }
    }
}