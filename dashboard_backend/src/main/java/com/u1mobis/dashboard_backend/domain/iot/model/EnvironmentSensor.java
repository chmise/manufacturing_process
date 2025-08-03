package com.u1mobis.dashboard_backend.domain.iot.model;

import com.u1mobis.dashboard_backend.domain.company.model.CompanyId;
import com.u1mobis.dashboard_backend.domain.manufacturing.model.ProductionLineId;
import com.u1mobis.dashboard_backend.shared.kernel.AggregateRoot;
import com.u1mobis.dashboard_backend.domain.iot.event.SensorDataReceivedEvent;
import com.u1mobis.dashboard_backend.domain.iot.event.SensorThresholdExceededEvent;
import com.u1mobis.dashboard_backend.domain.iot.event.SensorOfflineEvent;
import com.u1mobis.dashboard_backend.domain.iot.event.SensorCalibratedEvent;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EnvironmentSensor extends AggregateRoot<SensorId> {
    private SensorId id;
    private String name;
    private CompanyId companyId;
    private ProductionLineId lineId;
    private SensorType type;
    private String location;
    private SensorValue currentValue;
    private List<SensorReading> readings;
    private SensorThreshold threshold;
    private CalibrationInfo calibration;
    private LocalDateTime lastCommunication;
    private LocalDateTime installationDate;
    private LocalDateTime lastMaintenanceDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    protected EnvironmentSensor() {
        this.readings = new ArrayList<>();
    }
    
    public EnvironmentSensor(SensorId id, String name, CompanyId companyId, ProductionLineId lineId,
                           SensorType type, String location, SensorThreshold threshold) {
        this.id = id;
        this.name = name;
        this.companyId = companyId;
        this.lineId = lineId;
        this.type = type;
        this.location = location;
        this.threshold = threshold;
        this.readings = new ArrayList<>();
        this.calibration = CalibrationInfo.createDefault();
        this.installationDate = LocalDateTime.now();
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void recordReading(double value) {
        LocalDateTime now = LocalDateTime.now();
        
        // 보정 적용
        double calibratedValue = calibration.applyCalibration(value);
        
        // 센서 상태 결정
        SensorStatus status = threshold.determineStatus(calibratedValue);
        
        // 새로운 센서 값 생성
        SensorValue newValue = new SensorValue(calibratedValue, type.getUnit(), now, status);
        SensorValue previousValue = this.currentValue;
        
        this.currentValue = newValue;
        this.lastCommunication = now;
        this.updatedAt = now;
        
        // 읽기 데이터 저장 (최근 1000개만 유지)
        SensorReading reading = new SensorReading(calibratedValue, now, status, "Automatic");
        this.readings.add(reading);
        if (readings.size() > 1000) {
            readings.remove(0);
        }
        
        // 도메인 이벤트 발생
        raiseEvent(new SensorDataReceivedEvent(
            now, id.getValue(), String.valueOf(companyId.getValue()), type,
            calibratedValue, status, location
        ));
        
        // 임계값 초과 체크
        if (status == SensorStatus.WARNING || status == SensorStatus.CRITICAL || status == SensorStatus.OUT_OF_RANGE) {
            String thresholdType = determineThresholdType(calibratedValue, status);
            double thresholdValue = getThresholdValue(calibratedValue, thresholdType);
            
            raiseEvent(new SensorThresholdExceededEvent(
                now, id.getValue(), String.valueOf(companyId.getValue()), type,
                calibratedValue, thresholdValue, thresholdType, status, location
            ));
        }
    }
    
    public void updateThreshold(double minValue, double maxValue, double warningMin, double warningMax,
                              double criticalMin, double criticalMax, String userId) {
        // 이전 임계값 대비 변경사항 추적을 위해 기록
        this.threshold = new SensorThreshold(minValue, maxValue, warningMin, warningMax, 
                                           criticalMin, criticalMax, type.getUnit(), userId);
        this.updatedAt = LocalDateTime.now();
        
        // 현재 값이 새로운 임계값을 위반하는지 체크
        if (currentValue != null && currentValue.isValid()) {
            SensorStatus newStatus = threshold.determineStatus(currentValue.getValue());
            if (newStatus != currentValue.getStatus()) {
                recordReading(currentValue.getValue());
            }
        }
    }
    
    public void calibrate(double offset, double multiplier, String calibratedBy, String notes) {
        CalibrationInfo previousCalibration = this.calibration;
        this.calibration = CalibrationInfo.createManualCalibration(offset, multiplier, calibratedBy, notes);
        this.updatedAt = LocalDateTime.now();
        
        // 보정 완료 이벤트 발생
        raiseEvent(new SensorCalibratedEvent(
            LocalDateTime.now(), id.getValue(), String.valueOf(companyId.getValue()), type, location,
            "MANUAL", previousCalibration.getOffset(), offset,
            previousCalibration.getMultiplier(), multiplier, calibratedBy, notes
        ));
    }
    
    public void performMaintenance() {
        this.lastMaintenanceDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    public void markOffline() {
        if (currentValue != null) {
            this.currentValue = new SensorValue(
                currentValue.getValue(), currentValue.getUnit(),
                currentValue.getTimestamp(), SensorStatus.OFFLINE
            );
        }
        
        raiseEvent(new SensorOfflineEvent(
            LocalDateTime.now(), id.getValue(), String.valueOf(companyId.getValue()),
            type, location, lastCommunication, "Communication timeout"
        ));
        
        this.updatedAt = LocalDateTime.now();
    }
    
    private String determineThresholdType(double value, SensorStatus status) {
        if (status == SensorStatus.CRITICAL) {
            return value < threshold.getCriticalMin() ? "CRITICAL_MIN" : "CRITICAL_MAX";
        } else if (status == SensorStatus.WARNING) {
            return value < threshold.getWarningMin() ? "WARNING_MIN" : "WARNING_MAX";
        } else if (status == SensorStatus.OUT_OF_RANGE) {
            return value < threshold.getMinValue() ? "MIN" : "MAX";
        }
        return "NORMAL";
    }
    
    private double getThresholdValue(double value, String thresholdType) {
        return switch (thresholdType) {
            case "CRITICAL_MIN" -> threshold.getCriticalMin();
            case "CRITICAL_MAX" -> threshold.getCriticalMax();
            case "WARNING_MIN" -> threshold.getWarningMin();
            case "WARNING_MAX" -> threshold.getWarningMax();
            case "MIN" -> threshold.getMinValue();
            case "MAX" -> threshold.getMaxValue();
            default -> value;
        };
    }
    
    public boolean isOnline() {
        if (lastCommunication == null) {
            return false;
        }
        return lastCommunication.isAfter(LocalDateTime.now().minusMinutes(5));
    }
    
    public boolean needsMaintenance() {
        if (lastMaintenanceDate == null) {
            return installationDate.isBefore(LocalDateTime.now().minusMonths(6));
        }
        return lastMaintenanceDate.isBefore(LocalDateTime.now().minusMonths(3));
    }
    
    public boolean isWithinThreshold() {
        return currentValue != null && currentValue.getStatus() == SensorStatus.NORMAL;
    }
    
    public long getOfflineDuration() {
        if (lastCommunication == null) {
            return 0;
        }
        return java.time.Duration.between(lastCommunication, LocalDateTime.now()).toMinutes();
    }
    
    public List<SensorReading> getRecentReadings(int count) {
        if (readings.isEmpty()) {
            return new ArrayList<>();
        }
        
        int startIndex = Math.max(0, readings.size() - count);
        return new ArrayList<>(readings.subList(startIndex, readings.size()));
    }
    
    public double getAverageValue(int lastNReadings) {
        List<SensorReading> recent = getRecentReadings(lastNReadings);
        return recent.stream()
            .filter(r -> r.getStatus() != SensorStatus.ERROR)
            .mapToDouble(SensorReading::getValue)
            .average()
            .orElse(0.0);
    }
    
    @Override
    public SensorId getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public CompanyId getCompanyId() {
        return companyId;
    }
    
    public ProductionLineId getLineId() {
        return lineId;
    }
    
    public SensorType getType() {
        return type;
    }
    
    public String getLocation() {
        return location;
    }
    
    public SensorValue getCurrentValue() {
        return currentValue;
    }
    
    public List<SensorReading> getReadings() {
        return new ArrayList<>(readings);
    }
    
    public SensorThreshold getThreshold() {
        return threshold;
    }
    
    public CalibrationInfo getCalibration() {
        return calibration;
    }
    
    public LocalDateTime getLastCommunication() {
        return lastCommunication;
    }
    
    public LocalDateTime getInstallationDate() {
        return installationDate;
    }
    
    public LocalDateTime getLastMaintenanceDate() {
        return lastMaintenanceDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}