package com.u1mobis.dashboard_backend.domain.iot.model;

public enum SensorType {
    TEMPERATURE("Temperature Sensor", "Measures temperature", "°C"),
    HUMIDITY("Humidity Sensor", "Measures humidity", "%"),
    PRESSURE("Pressure Sensor", "Measures pressure", "Pa"),
    VIBRATION("Vibration Sensor", "Measures vibration", "Hz"),
    SOUND("Sound Sensor", "Measures sound level", "dB"),
    AIR_QUALITY("Air Quality Sensor", "Measures air quality index", "AQI"),
    LIGHT("Light Sensor", "Measures light intensity", "lux"),
    MOTION("Motion Sensor", "Detects motion", "boolean"),
    PROXIMITY("Proximity Sensor", "Measures distance", "cm"),
    CURRENT("Current Sensor", "Measures electrical current", "A"),
    VOLTAGE("Voltage Sensor", "Measures electrical voltage", "V"),
    POWER("Power Sensor", "Measures power consumption", "W");
    
    private final String displayName;
    private final String description;
    private final String unit;
    
    SensorType(String displayName, String description, String unit) {
        this.displayName = displayName;
        this.description = description;
        this.unit = unit;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public String getUnit() {
        return unit;
    }
    
    public boolean isEnvironmental() {
        return this == TEMPERATURE || this == HUMIDITY || this == AIR_QUALITY || this == LIGHT;
    }
    
    public boolean isElectrical() {
        return this == CURRENT || this == VOLTAGE || this == POWER;
    }
    
    public boolean isMechanical() {
        return this == VIBRATION || this == SOUND || this == PRESSURE;
    }
    
    public boolean requiresCalibration() {
        return this == TEMPERATURE || this == PRESSURE || this == VIBRATION || 
               this == CURRENT || this == VOLTAGE || this == POWER;
    }
}