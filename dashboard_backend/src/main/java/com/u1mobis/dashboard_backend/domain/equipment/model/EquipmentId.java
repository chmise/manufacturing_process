package com.u1mobis.dashboard_backend.domain.equipment.model;

import com.u1mobis.dashboard_backend.shared.kernel.ValueObject;

import java.util.Objects;
import java.util.UUID;

public class EquipmentId extends ValueObject {
    private final String value;
    
    public EquipmentId(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Equipment ID cannot be empty");
        }
        this.value = value.trim();
    }
    
    public String getValue() {
        return value;
    }
    
    public static EquipmentId generate() {
        return new EquipmentId(UUID.randomUUID().toString());
    }
    
    public static EquipmentId robotId(String id) {
        return new EquipmentId("ROBOT_" + id);
    }
    
    public static EquipmentId conveyorId(String id) {
        return new EquipmentId("CONVEYOR_" + id);
    }
    
    public static EquipmentId workStationId(String id) {
        return new EquipmentId("STATION_" + id);
    }
    
    public boolean isRobot() {
        return value.startsWith("ROBOT_");
    }
    
    public boolean isConveyor() {
        return value.startsWith("CONVEYOR_");
    }
    
    public boolean isWorkStation() {
        return value.startsWith("STATION_");
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        EquipmentId that = (EquipmentId) obj;
        return Objects.equals(value, that.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}