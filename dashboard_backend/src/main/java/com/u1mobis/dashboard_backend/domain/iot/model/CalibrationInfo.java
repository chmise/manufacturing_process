package com.u1mobis.dashboard_backend.domain.iot.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class CalibrationInfo extends ValueObject {
    private final double offset;
    private final double multiplier;
    private final String calibrationType;
    private final LocalDateTime calibratedAt;
    private final String calibratedBy;
    private final LocalDateTime nextCalibrationDue;
    private final String notes;
    private final CalibrationStatus status;
    
    public CalibrationInfo(double offset, double multiplier, String calibrationType, 
                          String calibratedBy, LocalDateTime nextCalibrationDue, String notes) {
        this.offset = offset;
        this.multiplier = multiplier != 0 ? multiplier : 1.0;
        this.calibrationType = calibrationType != null ? calibrationType.trim() : "MANUAL";
        this.calibratedAt = LocalDateTime.now();
        this.calibratedBy = calibratedBy != null ? calibratedBy.trim() : "SYSTEM";
        this.nextCalibrationDue = nextCalibrationDue != null ? nextCalibrationDue : 
                                 LocalDateTime.now().plus(6, ChronoUnit.MONTHS);
        this.notes = notes != null ? notes.trim() : "";
        this.status = CalibrationStatus.ACTIVE;
    }
    
    public static CalibrationInfo createDefault() {
        return new CalibrationInfo(0.0, 1.0, "FACTORY_DEFAULT", "SYSTEM", 
                                 LocalDateTime.now().plus(6, ChronoUnit.MONTHS), 
                                 "Factory default calibration");
    }
    
    public static CalibrationInfo createManualCalibration(double offset, double multiplier, 
                                                        String calibratedBy, String notes) {
        return new CalibrationInfo(offset, multiplier, "MANUAL", calibratedBy, 
                                 LocalDateTime.now().plus(3, ChronoUnit.MONTHS), notes);
    }
    
    public static CalibrationInfo createAutoCalibration(double offset, double multiplier, String notes) {
        return new CalibrationInfo(offset, multiplier, "AUTOMATIC", "SYSTEM", 
                                 LocalDateTime.now().plus(6, ChronoUnit.MONTHS), notes);
    }
    
    public double applyCalibration(double rawValue) {
        return (rawValue + offset) * multiplier;
    }
    
    public double reverseCalibration(double calibratedValue) {
        return (calibratedValue / multiplier) - offset;
    }
    
    public boolean isCalibrationDue() {
        return LocalDateTime.now().isAfter(nextCalibrationDue);
    }
    
    public boolean isCalibrationOverdue() {
        return LocalDateTime.now().isAfter(nextCalibrationDue.plus(1, ChronoUnit.MONTHS));
    }
    
    public long getDaysSinceCalibration() {
        return ChronoUnit.DAYS.between(calibratedAt, LocalDateTime.now());
    }
    
    public long getDaysUntilCalibrationDue() {
        return ChronoUnit.DAYS.between(LocalDateTime.now(), nextCalibrationDue);
    }
    
    public boolean requiresCalibration() {
        return isCalibrationDue() || status == CalibrationStatus.EXPIRED;
    }
    
    public boolean isSignificantCalibration() {
        return Math.abs(offset) > 0.1 || Math.abs(multiplier - 1.0) > 0.05;
    }
    
    public double getOffset() {
        return offset;
    }
    
    public double getMultiplier() {
        return multiplier;
    }
    
    public String getCalibrationType() {
        return calibrationType;
    }
    
    public LocalDateTime getCalibratedAt() {
        return calibratedAt;
    }
    
    public String getCalibratedBy() {
        return calibratedBy;
    }
    
    public LocalDateTime getNextCalibrationDue() {
        return nextCalibrationDue;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public CalibrationStatus getStatus() {
        return status;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CalibrationInfo that = (CalibrationInfo) obj;
        return Double.compare(that.offset, offset) == 0 &&
               Double.compare(that.multiplier, multiplier) == 0 &&
               Objects.equals(calibrationType, that.calibrationType) &&
               Objects.equals(calibratedAt, that.calibratedAt) &&
               Objects.equals(calibratedBy, that.calibratedBy);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(offset, multiplier, calibrationType, calibratedAt, calibratedBy);
    }
    
    @Override
    public String toString() {
        return String.format("Calibration{offset=%.3f, multiplier=%.3f, type=%s, by=%s, due=%s}",
                offset, multiplier, calibrationType, calibratedBy, nextCalibrationDue.toLocalDate());
    }
    
    public enum CalibrationStatus {
        ACTIVE("Active", "Calibration is active and valid"),
        EXPIRED("Expired", "Calibration has expired and needs renewal"),
        PENDING("Pending", "Calibration is pending validation"),
        INVALID("Invalid", "Calibration is invalid and should not be used");
        
        private final String displayName;
        private final String description;
        
        CalibrationStatus(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getDescription() {
            return description;
        }
    }
}