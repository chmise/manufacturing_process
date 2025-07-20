package com.u1mobis.dashboard_backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.u1mobis.dashboard_backend.entity.EnvironmentSensor;

@Repository
public interface EnvironmentSensorRepository extends JpaRepository<EnvironmentSensor, Long> {
    
    // 최신 환경 데이터 조회
    Optional<EnvironmentSensor> findTopByOrderByTimestampDesc();
    
    // 특정 기간 환경 데이터 조회
    List<EnvironmentSensor> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime startTime, LocalDateTime endTime);
    
    // 오늘 평균 온도 조회 (수정됨)
    @Query("SELECT AVG(es.temperature) FROM EnvironmentSensor es WHERE CAST(es.timestamp AS DATE) = CURRENT_DATE")
    Double getAverageTemperatureToday();
    
    // 오늘 평균 습도 조회 (수정됨)
    @Query("SELECT AVG(es.humidity) FROM EnvironmentSensor es WHERE CAST(es.timestamp AS DATE) = CURRENT_DATE")
    Double getAverageHumidityToday();
    
    // 온도 범위별 조회
    List<EnvironmentSensor> findByTemperatureBetween(Double minTemp, Double maxTemp);
    
    // 공기질 경고 레벨 조회
    @Query("SELECT es FROM EnvironmentSensor es WHERE es.airQuality >= 300 AND es.timestamp BETWEEN :startTime AND :endTime")
    List<EnvironmentSensor> findHighAirQualityAlerts(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);
    
    // 시간별 환경 데이터 평균 (수정됨)
    @Query("SELECT HOUR(es.timestamp) as hour, " +
           "AVG(es.temperature) as avgTemp, " +
           "AVG(es.humidity) as avgHumidity, " +
           "AVG(es.airQuality) as avgAirQuality " +
           "FROM EnvironmentSensor es WHERE CAST(es.timestamp AS DATE) = CURRENT_DATE " +
           "GROUP BY HOUR(es.timestamp) ORDER BY hour")
    List<Object[]> getHourlyEnvironmentAverages();
}