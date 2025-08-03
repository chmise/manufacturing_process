package com.u1mobis.dashboard_backend.interfaces.inventory.dto;

import com.u1mobis.dashboard_backend.domain.inventory.model.KeyMaterial;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KeyMaterialResponse {
    private String id;
    private String companyName;
    private String materialName;
    private String materialType;
    private Integer quantity;
    private Integer minimumStock;
    private String priority;
    private String supplier;
    private Double unitPrice;
    private String status;
    private Boolean critical;
    
    public static KeyMaterialResponse from(KeyMaterial keyMaterial) {
        return KeyMaterialResponse.builder()
            .id(keyMaterial.getId().getValue())
            .materialName(keyMaterial.getName().getValue())
            .materialType(keyMaterial.getType().name())
            .quantity(keyMaterial.getTotalQuantity().getValue())
            .minimumStock(keyMaterial.getCriticalThreshold().getValue())
            .priority(keyMaterial.getPriority().name())
            .unitPrice(keyMaterial.getUnitPrice())
            .status(keyMaterial.getQualityStatus().name())
            .critical(false) // TODO: Implement isCritical() method in KeyMaterial
            .build();
    }
}