package com.u1mobis.dashboard_backend.interfaces.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KeyMaterialRequest {
    private String companyName;
    private String materialName;
    private String materialType;
    private String type; // For MaterialType compatibility
    private Integer quantity;
    private Integer totalQuantity; // Alternative naming
    private Integer minimumStock;
    private Integer criticalThreshold; // Alternative naming  
    private String priority;
    private String supplier;
    private String storageLocation;
    private Double unitPrice;
    private String unit;
}