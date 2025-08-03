package com.u1mobis.dashboard_backend.application.inventory;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.inventory.model.*;
import com.u1mobis.dashboard_backend.domain.inventory.repository.KeyMaterialRepository;
import com.u1mobis.dashboard_backend.domain.inventory.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class InventoryApplicationService {
    
    private final StockRepository stockRepository;
    private final KeyMaterialRepository keyMaterialRepository;
    
    public InventoryApplicationService(StockRepository stockRepository,
                                     KeyMaterialRepository keyMaterialRepository) {
        this.stockRepository = stockRepository;
        this.keyMaterialRepository = keyMaterialRepository;
    }
    
    // Stock Operations
    public Stock createStock(CompanyId companyId, String materialName, MaterialType type,
                           int initialQuantity, int minimumThreshold, int maximumCapacity,
                           String unit, String location, double unitCost) {
        MaterialId materialId = MaterialId.generate();
        MaterialName name = new MaterialName(materialName);
        Quantity initial = Quantity.of(initialQuantity);
        Quantity minimum = Quantity.of(minimumThreshold);
        Quantity maximum = Quantity.of(maximumCapacity);
        
        Stock stock = new Stock(materialId, name, companyId, type, initial, minimum, maximum,
                              unit, location, unitCost);
        return stockRepository.save(stock);
    }
    
    public void consumeStock(MaterialId materialId, int quantity, String reason) {
        Stock stock = findStockById(materialId);
        stock.consume(Quantity.of(quantity), reason);
        stockRepository.save(stock);
    }
    
    public void replenishStock(MaterialId materialId, int quantity, String reason) {
        Stock stock = findStockById(materialId);
        stock.replenish(Quantity.of(quantity), reason);
        stockRepository.save(stock);
    }
    
    public void adjustStock(MaterialId materialId, int newQuantity, String reason) {
        Stock stock = findStockById(materialId);
        stock.adjustQuantity(Quantity.of(newQuantity), reason);
        stockRepository.save(stock);
    }
    
    public void updateStockThresholds(MaterialId materialId, int minimumThreshold, int maximumCapacity) {
        Stock stock = findStockById(materialId);
        stock.updateThresholds(Quantity.of(minimumThreshold), Quantity.of(maximumCapacity));
        stockRepository.save(stock);
    }
    
    public void updateStockSupplier(MaterialId materialId, String supplier) {
        Stock stock = findStockById(materialId);
        stock.updateSupplier(supplier);
        stockRepository.save(stock);
    }
    
    // KeyMaterial Operations
    public KeyMaterial createKeyMaterial(CompanyId companyId, String materialName, MaterialType type,
                                       int totalQuantity, int criticalThreshold, String unit,
                                       String storageLocation, double unitPrice, Priority priority) {
        MaterialId materialId = MaterialId.generate();
        MaterialName name = new MaterialName(materialName);
        Quantity total = Quantity.of(totalQuantity);
        Quantity critical = Quantity.of(criticalThreshold);
        
        KeyMaterial keyMaterial = new KeyMaterial(materialId, name, companyId, type, total, critical,
                                                unit, storageLocation, unitPrice, priority);
        return keyMaterialRepository.save(keyMaterial);
    }
    
    public void reserveKeyMaterial(MaterialId materialId, int quantity, String orderId, String purpose) {
        KeyMaterial keyMaterial = findKeyMaterialById(materialId);
        keyMaterial.reserve(Quantity.of(quantity), orderId, purpose);
        keyMaterialRepository.save(keyMaterial);
    }
    
    public void allocateKeyMaterial(MaterialId materialId, int quantity, String workOrderId, String purpose) {
        KeyMaterial keyMaterial = findKeyMaterialById(materialId);
        keyMaterial.allocate(Quantity.of(quantity), workOrderId, purpose);
        keyMaterialRepository.save(keyMaterial);
    }
    
    public void releaseKeyMaterialReservation(MaterialId materialId, int quantity, String orderId, String reason) {
        KeyMaterial keyMaterial = findKeyMaterialById(materialId);
        keyMaterial.releaseReservation(Quantity.of(quantity), orderId, reason);
        keyMaterialRepository.save(keyMaterial);
    }
    
    public void receiveKeyMaterialStock(MaterialId materialId, int quantity, String batchNumber) {
        KeyMaterial keyMaterial = findKeyMaterialById(materialId);
        keyMaterial.receiveStock(Quantity.of(quantity), batchNumber);
        keyMaterialRepository.save(keyMaterial);
    }
    
    public void approveKeyMaterialQuality(MaterialId materialId) {
        KeyMaterial keyMaterial = findKeyMaterialById(materialId);
        keyMaterial.approveQuality();
        keyMaterialRepository.save(keyMaterial);
    }
    
    public void rejectKeyMaterialQuality(MaterialId materialId, String reason) {
        KeyMaterial keyMaterial = findKeyMaterialById(materialId);
        keyMaterial.rejectQuality(reason);
        keyMaterialRepository.save(keyMaterial);
    }
    
    public void setKeyMaterialExpiration(MaterialId materialId, LocalDateTime expirationDate) {
        KeyMaterial keyMaterial = findKeyMaterialById(materialId);
        keyMaterial.setExpirationDate(expirationDate);
        keyMaterialRepository.save(keyMaterial);
    }
    
    // Query Operations
    @Transactional(readOnly = true)
    public List<Stock> findStocksByCompany(CompanyId companyId) {
        return stockRepository.findByCompanyId(companyId);
    }
    
    @Transactional(readOnly = true)
    public List<Stock> findLowStockItems(CompanyId companyId) {
        return stockRepository.findLowStockItems(companyId);
    }
    
    @Transactional(readOnly = true)
    public List<Stock> findOutOfStockItems(CompanyId companyId) {
        return stockRepository.findOutOfStockItems(companyId);
    }
    
    @Transactional(readOnly = true)
    public List<KeyMaterial> findKeyMaterialsByCompany(CompanyId companyId) {
        return keyMaterialRepository.findByCompanyId(companyId);
    }
    
    @Transactional(readOnly = true)
    public List<KeyMaterial> findCriticalKeyMaterials(CompanyId companyId) {
        return keyMaterialRepository.findCriticalLevelMaterials(companyId);
    }
    
    @Transactional(readOnly = true)
    public List<KeyMaterial> findExpiredKeyMaterials(CompanyId companyId) {
        return keyMaterialRepository.findExpiredMaterials(companyId);
    }
    
    @Transactional(readOnly = true)
    public List<KeyMaterial> findAvailableKeyMaterials(CompanyId companyId) {
        return keyMaterialRepository.findAvailableForReservation(companyId);
    }
    
    @Transactional(readOnly = true)
    public boolean canConsumeStock(MaterialId materialId, int quantity) {
        Stock stock = findStockById(materialId);
        return stock.canConsume(Quantity.of(quantity));
    }
    
    @Transactional(readOnly = true)
    public boolean canReserveKeyMaterial(MaterialId materialId, int quantity) {
        KeyMaterial keyMaterial = findKeyMaterialById(materialId);
        return keyMaterial.canReserve(Quantity.of(quantity));
    }
    
    // Private helper methods
    private Stock findStockById(MaterialId materialId) {
        return stockRepository.findById(materialId)
            .orElseThrow(() -> new IllegalArgumentException("Stock not found: " + materialId.getValue()));
    }
    
    private KeyMaterial findKeyMaterialById(MaterialId materialId) {
        return keyMaterialRepository.findById(materialId)
            .orElseThrow(() -> new IllegalArgumentException("Key material not found: " + materialId.getValue()));
    }
}