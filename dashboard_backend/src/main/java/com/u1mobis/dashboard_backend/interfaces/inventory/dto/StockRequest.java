package com.u1mobis.dashboard_backend.interfaces.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockRequest {
    private String materialName;
    private String type;
    private int initialQuantity;
    private int minimumThreshold;
    private int maximumCapacity;
    private String unit;
    private String location;
    private double unitCost;
    private String supplier;
}