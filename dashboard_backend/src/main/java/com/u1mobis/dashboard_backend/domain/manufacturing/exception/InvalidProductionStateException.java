package com.u1mobis.dashboard_backend.domain.manufacturing.exception;

import com.u1mobis.dashboard_backend.shared.exception.BusinessException;

public class InvalidProductionStateException extends BusinessException {
    
    private static final String ERROR_CODE = "INVALID_PRODUCTION_STATE";
    
    public InvalidProductionStateException(String currentState, String attemptedAction) {
        super(String.format("Cannot perform '%s' action. Current production state: %s", attemptedAction, currentState));
    }
    
    public InvalidProductionStateException(String message) {
        super(message);
    }
    
    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
}