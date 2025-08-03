package com.u1mobis.dashboard_backend.interfaces.inventory.dto;

import com.u1mobis.dashboard_backend.domain.inventory.model.Stock;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockResponse {
    private String id;
    private String companyName;
    private String materialName;
    private Integer quantity;
    private Integer safetyStock;
    private String location;
    private String status;
    private String unit;
    private Double unitCost;
    
    public static StockResponse from(Stock stock) {
        return StockResponse.builder()
            .id(stock.getId().getValue())
            .materialName(stock.getName().getValue())
            .quantity(stock.getCurrentQuantity().getValue())
            .safetyStock(stock.getMinimumThreshold().getValue())
            .location(stock.getLocation())
            .status(stock.isLowStock() ? "LOW" : "NORMAL")
            .unit(stock.getUnit())
            .unitCost(stock.getUnitCost())
            .build();
    }
}