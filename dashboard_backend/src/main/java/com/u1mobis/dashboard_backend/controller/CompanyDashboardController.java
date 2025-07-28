package com.u1mobis.dashboard_backend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.u1mobis.dashboard_backend.security.CustomUserDetailsService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
public class CompanyDashboardController {

    // 회사별 대시보드 데이터 조회
    @GetMapping("/api/{companyName}/dashboard")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getDashboardData(@PathVariable String companyName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
            (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        
        log.info("회사별 대시보드 데이터 요청: {}, 사용자: {}", companyName, userPrincipal.getUsername());
        
        Map<String, Object> dashboardData = new HashMap<>();
        dashboardData.put("companyName", companyName);
        dashboardData.put("userId", userPrincipal.getUserId());
        dashboardData.put("companyId", userPrincipal.getCompanyId());
        dashboardData.put("message", companyName + " 회사의 대시보드 데이터입니다.");
        
        return ResponseEntity.ok(dashboardData);
    }

    // 회사별 실시간 KPI 데이터
    @GetMapping("/api/{companyName}/kpi/realtime")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getRealTimeKPI(@PathVariable String companyName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
            (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        
        log.info("회사별 실시간 KPI 요청: {}, 사용자: {}", companyName, userPrincipal.getUsername());
        
        Map<String, Object> kpiData = new HashMap<>();
        kpiData.put("companyName", companyName);
        kpiData.put("productionRate", 85.5);
        kpiData.put("efficiency", 92.3);
        kpiData.put("qualityScore", 98.1);
        kpiData.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(kpiData);
    }

    // 회사별 환경 데이터
    @GetMapping("/api/{companyName}/environment/current")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getCurrentEnvironment(@PathVariable String companyName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
            (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        
        log.info("회사별 환경 데이터 요청: {}, 사용자: {}", companyName, userPrincipal.getUsername());
        
        Map<String, Object> envData = new HashMap<>();
        envData.put("companyName", companyName);
        envData.put("temperature", 23.5);
        envData.put("humidity", 45.2);
        envData.put("airQuality", "Good");
        envData.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(envData);
    }

    // 회사별 생산 상태
    @GetMapping("/api/{companyName}/production/status")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getProductionStatus(@PathVariable String companyName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
            (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        
        log.info("회사별 생산 상태 요청: {}, 사용자: {}", companyName, userPrincipal.getUsername());
        
        Map<String, Object> productionData = new HashMap<>();
        productionData.put("companyName", companyName);
        productionData.put("status", "RUNNING");
        productionData.put("totalProduced", 1245);
        productionData.put("targetProduction", 1500);
        productionData.put("efficiency", 83.0);
        
        return ResponseEntity.ok(productionData);
    }

    // 회사별 재고 정보
    @GetMapping("/api/{companyName}/stock")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getStock(@PathVariable String companyName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetailsService.CustomUserPrincipal userPrincipal = 
            (CustomUserDetailsService.CustomUserPrincipal) authentication.getPrincipal();
        
        log.info("회사별 재고 정보 요청: {}, 사용자: {}", companyName, userPrincipal.getUsername());
        
        Map<String, Object> stockData = new HashMap<>();
        stockData.put("companyName", companyName);
        stockData.put("rawMaterials", 89);
        stockData.put("finishedGoods", 234);
        stockData.put("workInProgress", 67);
        
        return ResponseEntity.ok(stockData);
    }
}