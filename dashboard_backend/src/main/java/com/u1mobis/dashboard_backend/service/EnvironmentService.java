package com.u1mobis.dashboard_backend.service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.u1mobis.dashboard_backend.entity.EnvironmentSensor;
import com.u1mobis.dashboard_backend.repository.EnvironmentSensorRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EnvironmentService {

    private final EnvironmentSensorRepository environmentSensorRepository;

    // 환경 데이터 저장 (MQTT에서 호출)
    public EnvironmentSensor saveEnvironmentData(Double temperature, Double humidity, Integer airQuality) {
        EnvironmentSensor sensor = EnvironmentSensor.builder()
                .timestamp(LocalDateTime.now())
                .temperature(temperature)
                .humidity(humidity)
                .airQuality(airQuality)
                .build();

        // 알람 체크
        checkEnvironmentAlarms(sensor);

        return environmentSensorRepository.save(sensor);
    }

    // 환경 알람 체크
    private void checkEnvironmentAlarms(EnvironmentSensor sensor) {
        if (sensor.getTemperature() > 35.0) {
            log.warn("고온 경보: {}°C", sensor.getTemperature());
        }
        if (sensor.getHumidity() > 80.0) {
            log.warn("고습도 경보: {}%", sensor.getHumidity());
        }
        if (sensor.getAirQuality() > 300) {
            log.warn("공기질 경보: {}", sensor.getAirQuality());
        }
    }

    // 최신 환경 데이터 조회
    public Map<String, Object> getCurrentEnvironment() {
        Optional<EnvironmentSensor> latest = environmentSensorRepository.findTopByOrderByTimestampDesc();

        if (latest.isPresent()) {
            EnvironmentSensor env = latest.get();
            return Map.of(
                    "temperature", env.getTemperature(),
                    "humidity", env.getHumidity(),
                    "air_quality", env.getAirQuality(),
                    "timestamp", env.getTimestamp().toString(), // 문자열로 변환
                    "status", getEnvironmentStatus(env));
        }

        return Map.of("status", "NO_DATA");
    }

    private String getEnvironmentStatus(EnvironmentSensor env) {
        if (env.getTemperature() > 35 || env.getHumidity() > 80 || env.getAirQuality() > 300) {
            return "WARNING";
        }
        return "NORMAL";
    }
}