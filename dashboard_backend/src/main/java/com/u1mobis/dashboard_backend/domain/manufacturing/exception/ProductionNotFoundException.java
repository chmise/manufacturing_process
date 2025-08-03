package com.u1mobis.dashboard_backend.domain.manufacturing.exception;

import com.u1mobis.dashboard_backend.shared.exception.ResourceNotFoundException;

public class ProductionNotFoundException extends ResourceNotFoundException {
    
    public ProductionNotFoundException(String productionId) {
        super("Production", productionId);
    }
}