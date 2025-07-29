package com.u1mobis.dashboard_backend.controller;

import com.u1mobis.dashboard_backend.entity.Alert;
import com.u1mobis.dashboard_backend.service.AlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class AlertController {

    private final AlertService alertService;

    @MessageMapping("/subscribe")
    public void subscribeToAlerts(@Payload Map<String, String> payload, 
                                SimpMessageHeaderAccessor headerAccessor) {
        String companyName = payload.get("companyName");
        String userId = payload.get("userId");
        
        // 세션에 회사 이름 저장
        headerAccessor.getSessionAttributes().put("companyName", companyName);
        headerAccessor.getSessionAttributes().put("userId", userId);
        
        log.info("사용자 {}가 회사 {} 알림 구독", userId, companyName);
    }

    // 회사별 알림 목록 조회
    @GetMapping("/{companyName}/alerts")
    public ResponseEntity<Map<String, Object>> getAlerts(@PathVariable String companyName) {
        try {
            List<Alert> alerts = alertService.getAlertsByCompany(companyName);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", alerts);
            response.put("count", alerts.size());
            
            log.info("알림 목록 조회 - 회사: {}, 개수: {}", companyName, alerts.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("알림 목록 조회 실패 - 회사: {}, 오류: {}", companyName, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "알림 목록 조회에 실패했습니다.");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // 개별 알림 삭제
    @DeleteMapping("/{companyName}/alerts/{alertId}")
    public ResponseEntity<Map<String, Object>> deleteAlert(@PathVariable String companyName, 
                                                          @PathVariable Long alertId) {
        try {
            boolean deleted = alertService.deleteAlert(companyName, alertId);
            Map<String, Object> response = new HashMap<>();
            
            if (deleted) {
                response.put("success", true);
                response.put("message", "알림이 삭제되었습니다.");
                log.info("개별 알림 삭제 성공 - 회사: {}, 알림ID: {}", companyName, alertId);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "삭제할 알림을 찾을 수 없습니다.");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("개별 알림 삭제 실패 - 회사: {}, 알림ID: {}, 오류: {}", companyName, alertId, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "알림 삭제에 실패했습니다.");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    // 모든 알림 삭제
    @DeleteMapping("/{companyName}/alerts")
    public ResponseEntity<Map<String, Object>> deleteAllAlerts(@PathVariable String companyName) {
        try {
            boolean deleted = alertService.deleteAllAlerts(companyName);
            Map<String, Object> response = new HashMap<>();
            
            if (deleted) {
                response.put("success", true);
                response.put("message", "모든 알림이 삭제되었습니다.");
                log.info("전체 알림 삭제 성공 - 회사: {}", companyName);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "회사를 찾을 수 없습니다.");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("전체 알림 삭제 실패 - 회사: {}, 오류: {}", companyName, e.getMessage());
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "알림 삭제에 실패했습니다.");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}