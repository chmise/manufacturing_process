package com.u1mobis.dashboard_backend.service;

import com.u1mobis.dashboard_backend.entity.Alert;
import com.u1mobis.dashboard_backend.entity.Company;
import com.u1mobis.dashboard_backend.entity.EnvironmentSensor;
import com.u1mobis.dashboard_backend.repository.AlertRepository;
import com.u1mobis.dashboard_backend.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AlertService {

    private final SimpMessagingTemplate messagingTemplate;
    private final AlertRepository alertRepository;
    private final CompanyRepository companyRepository;

    public void sendThresholdAlert(Company company, String alertType, String message, Object value) {
        // 알림을 데이터베이스에 저장
        Alert alertEntity = Alert.builder()
                .alertType(alertType)
                .message(message)
                .type("danger")
                .value(value != null ? value.toString() : null)
                .company(company)
                .timestamp(LocalDateTime.now())
                .isRead(false)
                .build();
        
        alertRepository.save(alertEntity);
        
        // WebSocket으로 실시간 알림 전송
        Map<String, Object> alert = new HashMap<>();
        alert.put("id", alertEntity.getAlertId());
        alert.put("type", "danger");
        alert.put("alertType", alertType);
        alert.put("message", message);
        alert.put("value", value);
        alert.put("timestamp", LocalDateTime.now().toString());
        alert.put("companyId", company.getCompanyId());
        alert.put("companyName", company.getCompanyName());

        // 해당 회사에만 알림 전송
        String destination = "/topic/alerts/" + company.getCompanyName();
        messagingTemplate.convertAndSend(destination, alert);
        
        log.warn("임계값 알림 전송 - 회사: {}, 타입: {}, 메시지: {}", 
                company.getCompanyName(), alertType, message);
    }

    public void sendEnvironmentAlert(Company company, EnvironmentSensor sensor) {
        if (sensor.getTemperature() > 35.0) {
            sendThresholdAlert(company, "TEMPERATURE", 
                    String.format("고온 경보: %.1f°C", sensor.getTemperature()),
                    sensor.getTemperature());
        }
        
        if (sensor.getHumidity() > 80.0) {
            sendThresholdAlert(company, "HUMIDITY", 
                    String.format("고습도 경보: %.1f%", sensor.getHumidity()),
                    sensor.getHumidity());
        }
        
        if (sensor.getAirQuality() > 300) {
            sendThresholdAlert(company, "AIR_QUALITY", 
                    String.format("공기질 경보: %d", sensor.getAirQuality()),
                    sensor.getAirQuality());
        }
    }

    public void sendProductionAlert(Company company, String productId, String alertType, String message) {
        // 알림을 데이터베이스에 저장
        Alert alertEntity = Alert.builder()
                .alertType(alertType)
                .message(message)
                .type("warning")
                .productId(productId)
                .company(company)
                .timestamp(LocalDateTime.now())
                .isRead(false)
                .build();
        
        alertRepository.save(alertEntity);
        
        // WebSocket으로 실시간 알림 전송
        Map<String, Object> alert = new HashMap<>();
        alert.put("id", alertEntity.getAlertId());
        alert.put("type", "warning");
        alert.put("alertType", alertType);
        alert.put("message", message);
        alert.put("productId", productId);
        alert.put("timestamp", LocalDateTime.now().toString());
        alert.put("companyId", company.getCompanyId());
        alert.put("companyName", company.getCompanyName());

        String destination = "/topic/alerts/" + company.getCompanyName();
        messagingTemplate.convertAndSend(destination, alert);
        
        log.warn("생산 알림 전송 - 회사: {}, 제품: {}, 타입: {}, 메시지: {}", 
                company.getCompanyName(), productId, alertType, message);
    }

    // 회사별 알림 목록 조회
    public List<Alert> getAlertsByCompany(String companyName) {
        Optional<Company> company = companyRepository.findByCompanyName(companyName);
        return company.map(c -> alertRepository.findByCompanyOrderByTimestampDesc(c))
                     .orElse(List.of());
    }

    // 개별 알림 삭제
    @Transactional
    public boolean deleteAlert(String companyName, Long alertId) {
        Optional<Company> companyOpt = companyRepository.findByCompanyName(companyName);
        if (companyOpt.isEmpty()) {
            log.warn("회사를 찾을 수 없음: {}", companyName);
            return false;
        }
        
        Alert alert = alertRepository.findByAlertIdAndCompany(alertId, companyOpt.get());
        if (alert != null) {
            alertRepository.delete(alert);
            log.info("알림 삭제 완료 - 회사: {}, 알림ID: {}", companyName, alertId);
            return true;
        }
        
        log.warn("삭제할 알림을 찾을 수 없음 - 회사: {}, 알림ID: {}", companyName, alertId);
        return false;
    }

    // 회사별 모든 알림 삭제
    @Transactional
    public boolean deleteAllAlerts(String companyName) {
        Optional<Company> companyOpt = companyRepository.findByCompanyName(companyName);
        if (companyOpt.isEmpty()) {
            log.warn("회사를 찾을 수 없음: {}", companyName);
            return false;
        }
        
        alertRepository.deleteByCompany(companyOpt.get());
        log.info("모든 알림 삭제 완료 - 회사: {}", companyName);
        return true;
    }
}