package com.u1mobis.dashboard_backend.interfaces.inventory;

import com.u1mobis.dashboard_backend.application.inventory.InventoryApplicationService;
import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.inventory.model.MaterialId;
import com.u1mobis.dashboard_backend.domain.inventory.model.MaterialType;
import com.u1mobis.dashboard_backend.domain.inventory.model.Priority;
import com.u1mobis.dashboard_backend.interfaces.inventory.dto.StockRequest;
import com.u1mobis.dashboard_backend.interfaces.inventory.dto.StockResponse;
import com.u1mobis.dashboard_backend.interfaces.inventory.dto.KeyMaterialRequest;
import com.u1mobis.dashboard_backend.interfaces.inventory.dto.KeyMaterialResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@RestController
@RequestMapping("/api/{companyName}/inventory")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
@RequiredArgsConstructor
@Slf4j
public class InventoryController {
    
    private final InventoryApplicationService inventoryApplicationService;
    
    @PostMapping("/stock")
    public ResponseEntity<StockResponse> createStock(
            @PathVariable String companyName,
            @RequestBody StockRequest request) {
        
        log.info("재고 생성 - 회사: {}, 자재: {}", companyName, request.getMaterialName());
        
        CompanyId companyId = new CompanyId("1"); // TODO: get actual company ID from companyName
        
        var stock = inventoryApplicationService.createStock(
            companyId, request.getMaterialName(), MaterialType.valueOf(request.getType()),
            request.getInitialQuantity(), request.getMinimumThreshold(), request.getMaximumCapacity(),
            request.getUnit(), request.getLocation(), request.getUnitCost()
        );
        
        return ResponseEntity.ok(StockResponse.from(stock));
    }
    
    @PostMapping("/stock/{materialId}/consume")
    public ResponseEntity<Void> consumeStock(
            @PathVariable String companyName,
            @PathVariable String materialId,
            @RequestParam int quantity,
            @RequestParam String reason) {
        
        log.info("재고 소비 - 회사: {}, 자재: {}, 수량: {}", companyName, materialId, quantity);
        
        MaterialId id = new MaterialId(materialId);
        inventoryApplicationService.consumeStock(id, quantity, reason);
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/stock/{materialId}/replenish")
    public ResponseEntity<Void> replenishStock(
            @PathVariable String companyName,
            @PathVariable String materialId,
            @RequestParam int quantity,
            @RequestParam String reason) {
        
        log.info("재고 보충 - 회사: {}, 자재: {}, 수량: {}", companyName, materialId, quantity);
        
        MaterialId id = new MaterialId(materialId);
        inventoryApplicationService.replenishStock(id, quantity, reason);
        
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/stock")
    public ResponseEntity<List<StockResponse>> getStocks(@PathVariable String companyName) {
        
        log.info("재고 목록 조회 - 회사: {}", companyName);
        
        CompanyId companyId = new CompanyId("1"); // TODO: get actual company ID from companyName
        
        var stocks = inventoryApplicationService.findStocksByCompany(companyId);
        
        return ResponseEntity.ok(
            stocks.stream()
                .map(StockResponse::from)
                .toList()
        );
    }
    
    @GetMapping("/stock/low")
    public ResponseEntity<List<StockResponse>> getLowStockItems(@PathVariable String companyName) {
        
        log.info("부족 재고 조회 - 회사: {}", companyName);
        
        CompanyId companyId = new CompanyId("1"); // TODO: get actual company ID from companyName
        
        var lowStocks = inventoryApplicationService.findLowStockItems(companyId);
        
        return ResponseEntity.ok(
            lowStocks.stream()
                .map(StockResponse::from)
                .toList()
        );
    }
    
    @PostMapping("/key-materials")
    public ResponseEntity<KeyMaterialResponse> createKeyMaterial(
            @PathVariable String companyName,
            @RequestBody KeyMaterialRequest request) {
        
        log.info("핵심자재 생성 - 회사: {}, 자재: {}", companyName, request.getMaterialName());
        
        CompanyId companyId = new CompanyId("1"); // TODO: get actual company ID from companyName
        
        var keyMaterial = inventoryApplicationService.createKeyMaterial(
            companyId, request.getMaterialName(), 
            MaterialType.valueOf(request.getMaterialType() != null ? request.getMaterialType() : request.getType()),
            request.getTotalQuantity() != null ? request.getTotalQuantity() : request.getQuantity(),
            request.getCriticalThreshold() != null ? request.getCriticalThreshold() : request.getMinimumStock(), 
            request.getUnit() != null ? request.getUnit() : "EA",
            request.getStorageLocation() != null ? request.getStorageLocation() : "WAREHOUSE",
            request.getUnitPrice() != null ? request.getUnitPrice() : 0.0, 
            Priority.valueOf(request.getPriority() != null ? request.getPriority() : "MEDIUM")
        );
        
        return ResponseEntity.ok(KeyMaterialResponse.from(keyMaterial));
    }
    
    @PostMapping("/key-materials/{materialId}/reserve")
    public ResponseEntity<Void> reserveKeyMaterial(
            @PathVariable String companyName,
            @PathVariable String materialId,
            @RequestParam int quantity,
            @RequestParam String orderId,
            @RequestParam String purpose) {
        
        log.info("핵심자재 예약 - 회사: {}, 자재: {}, 수량: {}", companyName, materialId, quantity);
        
        MaterialId id = new MaterialId(materialId);
        inventoryApplicationService.reserveKeyMaterial(id, quantity, orderId, purpose);
        
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/key-materials/{materialId}/allocate")
    public ResponseEntity<Void> allocateKeyMaterial(
            @PathVariable String companyName,
            @PathVariable String materialId,
            @RequestParam int quantity,
            @RequestParam String workOrderId,
            @RequestParam String purpose) {
        
        log.info("핵심자재 할당 - 회사: {}, 자재: {}, 수량: {}", companyName, materialId, quantity);
        
        MaterialId id = new MaterialId(materialId);
        inventoryApplicationService.allocateKeyMaterial(id, quantity, workOrderId, purpose);
        
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/key-materials")
    public ResponseEntity<List<KeyMaterialResponse>> getKeyMaterials(@PathVariable String companyName) {
        
        log.info("핵심자재 목록 조회 - 회사: {}", companyName);
        
        CompanyId companyId = new CompanyId("1"); // TODO: get actual company ID from companyName
        
        var keyMaterials = inventoryApplicationService.findKeyMaterialsByCompany(companyId);
        
        return ResponseEntity.ok(
            keyMaterials.stream()
                .map(KeyMaterialResponse::from)
                .toList()
        );
    }
    
    @GetMapping("/key-materials/critical")
    public ResponseEntity<List<KeyMaterialResponse>> getCriticalKeyMaterials(@PathVariable String companyName) {
        
        log.info("위험수준 핵심자재 조회 - 회사: {}", companyName);
        
        CompanyId companyId = new CompanyId("1"); // TODO: get actual company ID from companyName
        
        var criticalMaterials = inventoryApplicationService.findCriticalKeyMaterials(companyId);
        
        return ResponseEntity.ok(
            criticalMaterials.stream()
                .map(KeyMaterialResponse::from)
                .toList()
        );
    }
}