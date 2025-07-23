package com.u1mobis.dashboard_backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.u1mobis.dashboard_backend.entity.RobotStatus;
import com.u1mobis.dashboard_backend.entity.ProductPosition;
import com.u1mobis.dashboard_backend.service.RobotStatusService;
import com.u1mobis.dashboard_backend.service.ProductPositionService;

@RestController
@RequestMapping("/api/digital-twin")
@CrossOrigin(origins = "*")
public class DigitalTwinController {
    
    @Autowired
    private RobotStatusService robotStatusService;
    
    @Autowired
    private ProductPositionService productPositionService;
    
    // 로봇 상태 조회 API
    @GetMapping("/robot-status/{robotId}")
    public ResponseEntity<?> getRobotStatus(@PathVariable Long robotId, 
                                          @RequestParam(defaultValue = "1") Long companyId) {
        try {
            RobotStatusService.RobotStatusInfo robotInfo = robotStatusService.getRobotStatusInfo(robotId, companyId);
            
            if (robotInfo == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(robotInfo);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "로봇 상태 조회 실패", "message", e.getMessage()));
        }
    }
    
    // 공정별 로봇 상태 조회
    @GetMapping("/station/{stationCode}/robots")
    public ResponseEntity<?> getRobotsByStation(@PathVariable String stationCode,
                                              @RequestParam(defaultValue = "1") Long companyId) {
        try {
            List<RobotStatus> robotStatuses = robotStatusService.getRobotStatusByStation(stationCode, companyId);
            return ResponseEntity.ok(robotStatuses);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "공정별 로봇 상태 조회 실패", "message", e.getMessage()));
        }
    }
    
    // 제품 위치 조회 API
    @GetMapping("/product-position/{productId}")
    public ResponseEntity<?> getProductPosition(@PathVariable String productId,
                                              @RequestParam(defaultValue = "1") Long companyId) {
        try {
            ProductPositionService.ProductPositionInfo productInfo = 
                    productPositionService.getProductPositionInfo(productId, companyId);
            
            if (productInfo == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok(productInfo);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "제품 위치 조회 실패", "message", e.getMessage()));
        }
    }
    
    // 공정별 제품 조회
    @GetMapping("/station/{stationCode}/products")
    public ResponseEntity<?> getProductsByStation(@PathVariable String stationCode,
                                                @RequestParam(defaultValue = "1") Long companyId) {
        try {
            List<ProductPosition> products = productPositionService.getProductsByStation(stationCode, companyId);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "공정별 제품 조회 실패", "message", e.getMessage()));
        }
    }
    
    // 전체 로봇 상태 조회
    @GetMapping("/robots/status")
    public ResponseEntity<?> getAllRobotStatus(@RequestParam(defaultValue = "1") Long companyId) {
        try {
            List<RobotStatus> robotStatuses = robotStatusService.getAllRobotStatus(companyId);
            return ResponseEntity.ok(robotStatuses);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "전체 로봇 상태 조회 실패", "message", e.getMessage()));
        }
    }
    
    // 전체 제품 위치 조회
    @GetMapping("/products/positions")
    public ResponseEntity<?> getAllProductPositions(@RequestParam(defaultValue = "1") Long companyId) {
        try {
            List<ProductPosition> productPositions = productPositionService.getAllProductPositions(companyId);
            return ResponseEntity.ok(productPositions);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "전체 제품 위치 조회 실패", "message", e.getMessage()));
        }
    }
    
    // 라인별 제품 조회 (A, B, C, D 라인)
    @GetMapping("/line/{lineCode}/products")
    public ResponseEntity<?> getProductsByLine(@PathVariable String lineCode,
                                             @RequestParam(defaultValue = "1") Long companyId) {
        try {
            List<ProductPosition> products = productPositionService.getProductsByLine(lineCode, companyId);
            return ResponseEntity.ok(products);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "라인별 제품 조회 실패", "message", e.getMessage()));
        }
    }
    
    // 통합 상태 정보 조회 (특정 공정의 로봇 + 제품 정보)
    @GetMapping("/station/{stationCode}/status")
    public ResponseEntity<?> getStationStatus(@PathVariable String stationCode,
                                            @RequestParam(defaultValue = "1") Long companyId) {
        try {
            List<RobotStatus> robots = robotStatusService.getRobotStatusByStation(stationCode, companyId);
            List<ProductPosition> products = productPositionService.getProductsByStation(stationCode, companyId);
            
            Map<String, Object> stationStatus = Map.of(
                "stationCode", stationCode,
                "robots", robots,
                "products", products,
                "timestamp", java.time.LocalDateTime.now()
            );
            
            return ResponseEntity.ok(stationStatus);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "공정 상태 조회 실패", "message", e.getMessage()));
        }
    }
}