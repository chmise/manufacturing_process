package com.u1mobis.dashboard_backend.domain.equipment.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.util.Objects;

public class RobotConfiguration extends ValueObject {
    private final double cycleTime;
    private final int motorSpeed;
    private final boolean ledEnabled;
    
    public RobotConfiguration() {
        this(10.0, 100, true); // 기본값
    }
    
    public RobotConfiguration(double cycleTime, int motorSpeed, boolean ledEnabled) {
        if (cycleTime <= 0) {
            throw new IllegalArgumentException("Cycle time must be positive");
        }
        if (motorSpeed < 0 || motorSpeed > 255) {
            throw new IllegalArgumentException("Motor speed must be between 0 and 255");
        }
        
        this.cycleTime = cycleTime;
        this.motorSpeed = motorSpeed;
        this.ledEnabled = ledEnabled;
    }
    
    public double getCycleTime() {
        return cycleTime;
    }
    
    public int getMotorSpeed() {
        return motorSpeed;
    }
    
    public boolean isLedEnabled() {
        return ledEnabled;
    }
    
    public RobotConfiguration withCycleTime(double newCycleTime) {
        return new RobotConfiguration(newCycleTime, motorSpeed, ledEnabled);
    }
    
    public RobotConfiguration withMotorSpeed(int newMotorSpeed) {
        return new RobotConfiguration(cycleTime, newMotorSpeed, ledEnabled);
    }
    
    public RobotConfiguration withLedEnabled(boolean newLedEnabled) {
        return new RobotConfiguration(cycleTime, motorSpeed, newLedEnabled);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        RobotConfiguration that = (RobotConfiguration) obj;
        return Double.compare(that.cycleTime, cycleTime) == 0 &&
               motorSpeed == that.motorSpeed &&
               ledEnabled == that.ledEnabled;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(cycleTime, motorSpeed, ledEnabled);
    }
    
    @Override
    public String toString() {
        return "RobotConfiguration{" +
                "cycleTime=" + cycleTime +
                ", motorSpeed=" + motorSpeed +
                ", ledEnabled=" + ledEnabled +
                '}';
    }
}