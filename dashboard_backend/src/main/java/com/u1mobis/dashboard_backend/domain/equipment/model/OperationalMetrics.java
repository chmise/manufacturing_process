package com.u1mobis.dashboard_backend.domain.equipment.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class OperationalMetrics extends ValueObject {
    private final int productionCount;
    private final long totalRuntimeSeconds;
    private final LocalDateTime lastOperationTime;
    private final int errorCount;
    
    public OperationalMetrics() {
        this(0, 0L, null, 0);
    }
    
    public OperationalMetrics(int productionCount, long totalRuntimeSeconds, 
                             LocalDateTime lastOperationTime, int errorCount) {
        if (productionCount < 0) {
            throw new IllegalArgumentException("Production count cannot be negative");
        }
        if (totalRuntimeSeconds < 0) {
            throw new IllegalArgumentException("Total runtime cannot be negative");
        }
        if (errorCount < 0) {
            throw new IllegalArgumentException("Error count cannot be negative");
        }
        
        this.productionCount = productionCount;
        this.totalRuntimeSeconds = totalRuntimeSeconds;
        this.lastOperationTime = lastOperationTime;
        this.errorCount = errorCount;
    }
    
    public int getProductionCount() {
        return productionCount;
    }
    
    public long getTotalRuntimeSeconds() {
        return totalRuntimeSeconds;
    }
    
    public LocalDateTime getLastOperationTime() {
        return lastOperationTime;
    }
    
    public int getErrorCount() {
        return errorCount;
    }
    
    public OperationalMetrics incrementProductionCount() {
        return new OperationalMetrics(
            productionCount + 1,
            totalRuntimeSeconds,
            LocalDateTime.now(),
            errorCount
        );
    }
    
    public OperationalMetrics incrementErrorCount() {
        return new OperationalMetrics(
            productionCount,
            totalRuntimeSeconds,
            lastOperationTime,
            errorCount + 1
        );
    }
    
    public OperationalMetrics addRuntime(Duration runtime) {
        return new OperationalMetrics(
            productionCount,
            totalRuntimeSeconds + runtime.toSeconds(),
            lastOperationTime,
            errorCount
        );
    }
    
    public double getAverageProductionRate() {
        if (totalRuntimeSeconds == 0) {
            return 0.0;
        }
        // 시간당 생산율
        return (double) productionCount / (totalRuntimeSeconds / 3600.0);
    }
    
    public double getErrorRate() {
        if (productionCount == 0) {
            return 0.0;
        }
        return (double) errorCount / productionCount * 100.0;
    }
    
    public Duration getTotalRuntime() {
        return Duration.ofSeconds(totalRuntimeSeconds);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        OperationalMetrics that = (OperationalMetrics) obj;
        return productionCount == that.productionCount &&
               totalRuntimeSeconds == that.totalRuntimeSeconds &&
               errorCount == that.errorCount &&
               Objects.equals(lastOperationTime, that.lastOperationTime);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(productionCount, totalRuntimeSeconds, lastOperationTime, errorCount);
    }
    
    @Override
    public String toString() {
        return "OperationalMetrics{" +
                "productionCount=" + productionCount +
                ", totalRuntimeSeconds=" + totalRuntimeSeconds +
                ", lastOperationTime=" + lastOperationTime +
                ", errorCount=" + errorCount +
                '}';
    }
}