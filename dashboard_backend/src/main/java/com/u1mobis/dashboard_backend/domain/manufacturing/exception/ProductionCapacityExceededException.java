package com.u1mobis.dashboard_backend.domain.manufacturing.exception;

import com.u1mobis.dashboard_backend.shared.exception.BusinessException;

public class ProductionCapacityExceededException extends BusinessException {
    
    private static final String ERROR_CODE = "PRODUCTION_CAPACITY_EXCEEDED";
    
    public ProductionCapacityExceededException(String lineId) {
        super(String.format("Production line %s is at capacity. Cannot start new production.", lineId));
    }
    
    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
}