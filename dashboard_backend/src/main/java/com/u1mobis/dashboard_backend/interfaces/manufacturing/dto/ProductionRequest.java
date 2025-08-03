package com.u1mobis.dashboard_backend.interfaces.manufacturing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionRequest {
    
    @NotBlank(message = "Product name is required")
    @Size(min = 2, max = 100, message = "Product name must be between 2 and 100 characters")
    private String productName;
    
    @NotNull(message = "Target quantity is required")
    @Min(value = 1, message = "Target quantity must be at least 1")
    @Max(value = 10000, message = "Target quantity cannot exceed 10000")
    private Integer targetQuantity;
    
    @NotNull(message = "Due date is required")
    @Future(message = "Due date must be in the future")
    private LocalDateTime dueDate;
    
    @Pattern(regexp = "^(LOW|MEDIUM|HIGH|URGENT)$", message = "Priority must be one of: LOW, MEDIUM, HIGH, URGENT")
    private String priority;
    
    @Size(max = 50, message = "Work order ID cannot exceed 50 characters")
    private String workOrderId;
    
    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;
}