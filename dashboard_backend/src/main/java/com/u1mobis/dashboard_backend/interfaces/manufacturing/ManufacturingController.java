package com.u1mobis.dashboard_backend.interfaces.manufacturing;

import com.u1mobis.dashboard_backend.application.manufacturing.ProductionApplicationService;
import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.manufacturing.model.ProductionLineId;
import com.u1mobis.dashboard_backend.domain.manufacturing.exception.ProductionNotFoundException;
import com.u1mobis.dashboard_backend.infrastructure.security.SecurityContext;
import com.u1mobis.dashboard_backend.interfaces.manufacturing.dto.ProductionRequest;
import com.u1mobis.dashboard_backend.interfaces.manufacturing.dto.ProductionResponse;
import com.u1mobis.dashboard_backend.interfaces.manufacturing.dto.ProductionStatusResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/{companyName}/manufacturing")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@RequiredArgsConstructor
@Slf4j
public class ManufacturingController {
    
    private final ProductionApplicationService productionApplicationService;
    private final SecurityContext securityContext;
    
    @PostMapping("/production/start")
    public ResponseEntity<ProductionResponse> startProduction(
            @PathVariable String companyName,
            @RequestParam Long lineId,
            @RequestBody @Valid ProductionRequest request) {
        
        log.info("생산 시작 요청 - 회사: {}, 라인: {}, 제품: {}", companyName, lineId, request.getProductName());
        
        CompanyId companyId = securityContext.requireCurrentCompanyId();
        ProductionLineId productionLineId = new ProductionLineId(String.valueOf(lineId));
        
        String productionId = productionApplicationService.startNewProduction(
            Long.valueOf(companyId.getValue()), lineId, request.getProductName(), "DEFAULT"
        );
        var production = productionApplicationService.findProductionById(productionId)
                .orElseThrow(() -> new ProductionNotFoundException(productionId));
        
        return ResponseEntity.ok(ProductionResponse.from(production));
    }
    
    @PostMapping("/production/{productionId}/complete")
    public ResponseEntity<ProductionResponse> completeProduction(
            @PathVariable String companyName,
            @PathVariable String productionId,
            @RequestParam Double cycleTime,
            @RequestParam String quality) {
        
        log.info("생산 완료 요청 - 회사: {}, 생산ID: {}", companyName, productionId);
        
        CompanyId companyId = securityContext.requireCurrentCompanyId();
        
        var production = productionApplicationService.findProductionById(productionId)
                .orElseThrow(() -> new ProductionNotFoundException(productionId));
        
        return ResponseEntity.ok(ProductionResponse.from(production));
    }
    
    @PostMapping("/production/{productionId}/move")
    public ResponseEntity<Void> moveToNextStation(
            @PathVariable String companyName,
            @PathVariable String productionId,
            @RequestParam String stationId) {
        
        log.info("공정 이동 요청 - 회사: {}, 생산ID: {}, 다음공정: {}", companyName, productionId, stationId);
        
        CompanyId companyId = securityContext.requireCurrentCompanyId();
        productionApplicationService.moveProductionToStation(productionId, stationId, 0.0, 0.0, 50);
        
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/production/status")
    public ResponseEntity<ProductionStatusResponse> getProductionStatus(
            @PathVariable String companyName,
            @RequestParam(required = false) Long lineId) {
        
        log.info("생산 현황 조회 - 회사: {}, 라인: {}", companyName, lineId);
        
        CompanyId companyId = securityContext.requireCurrentCompanyId();
        ProductionLineId productionLineId = lineId != null ? new ProductionLineId(String.valueOf(lineId)) : null;
        
        var activeProductions = productionApplicationService.getActiveProductions(Long.valueOf(companyId.getValue()));
        
        ProductionStatusResponse status = new ProductionStatusResponse();
        return ResponseEntity.ok(status);
    }
    
    @GetMapping("/production/active")
    public ResponseEntity<List<ProductionResponse>> getActiveProductions(
            @PathVariable String companyName,
            @RequestParam(required = false) Long lineId) {
        
        log.info("진행중인 생산 조회 - 회사: {}, 라인: {}", companyName, lineId);
        
        CompanyId companyId = securityContext.requireCurrentCompanyId();
        ProductionLineId productionLineId = lineId != null ? new ProductionLineId(String.valueOf(lineId)) : null;
        
        var productions = productionApplicationService.getActiveProductions(Long.valueOf(companyId.getValue()));
        
        return ResponseEntity.ok(
            productions.stream()
                .map(ProductionResponse::from)
                .toList()
        );
    }
    
    @GetMapping("/production/history")
    public ResponseEntity<List<ProductionResponse>> getProductionHistory(
            @PathVariable String companyName,
            @RequestParam(required = false) Long lineId,
            @RequestParam(defaultValue = "10") int limit) {
        
        log.info("생산 이력 조회 - 회사: {}, 라인: {}, 제한: {}", companyName, lineId, limit);
        
        CompanyId companyId = securityContext.requireCurrentCompanyId();
        ProductionLineId productionLineId = lineId != null ? new ProductionLineId(String.valueOf(lineId)) : null;
        
        var history = productionApplicationService.getCompanyProductions(Long.valueOf(companyId.getValue()));
        
        return ResponseEntity.ok(
            history.stream()
                .map(ProductionResponse::from)
                .limit(limit)
                .toList()
        );
    }
}