package com.u1mobis.dashboard_backend.shared.exception;

public abstract class BusinessException extends RuntimeException {
    
    protected BusinessException(String message) {
        super(message);
    }
    
    protected BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public abstract String getErrorCode();
}