package com.u1mobis.dashboard_backend.shared.exception;

public class ResourceNotFoundException extends BusinessException {
    
    private static final String ERROR_CODE = "RESOURCE_NOT_FOUND";
    
    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(String.format("%s not found with id: %s", resourceType, resourceId));
    }
    
    public ResourceNotFoundException(String message) {
        super(message);
    }
    
    @Override
    public String getErrorCode() {
        return ERROR_CODE;
    }
}