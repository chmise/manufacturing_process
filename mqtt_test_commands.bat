@echo off
chcp 65001 >nul
echo ====================================
echo KPI 운영데이터 MQTT 테스트 명령어
echo ====================================

echo.
echo 1. u1mobis 회사 - 우수 KPI 데이터 전송
echo 토픽: factory/g2qj6jvf/1/operations
echo OEE: 97.9%% (우수), FTY: 98.9%% (우수), OTD: 99.6%% (우수)
echo.
mosquitto_pub -h localhost -p 1883 -t "factory/g2qj6jvf/1/operations" -m "{\"planned_time\": 480, \"downtime\": 10, \"target_cycle_time\": 30.0, \"good_count\": 450, \"total_count\": 450, \"first_time_pass_count\": 445, \"on_time_delivery_count\": 448}"

if %errorlevel% == 0 (
    echo ✅ u1mobis KPI 데이터 전송 성공
) else (
    echo ❌ u1mobis KPI 데이터 전송 실패
)

timeout /t 3 /nobreak >nul

echo.
echo 2. clear 회사 - 혼합 KPI 데이터 전송  
echo 토픽: factory/fdyeaqhl/1/operations
echo OEE: 63.2%% (미달), FTY: 80.0%% (보통), OTD: 85.0%% (보통)
echo.
mosquitto_pub -h localhost -p 1883 -t "factory/fdyeaqhl/1/operations" -m "{\"planned_time\": 480, \"downtime\": 120, \"target_cycle_time\": 35.0, \"good_count\": 290, \"total_count\": 300, \"first_time_pass_count\": 240, \"on_time_delivery_count\": 255}"

if %errorlevel% == 0 (
    echo ✅ clear KPI 데이터 전송 성공
) else (
    echo ❌ clear KPI 데이터 전송 실패
)

echo.
echo 3. 환경 데이터 전송 테스트
echo.
mosquitto_pub -h localhost -p 1883 -t "factory/g2qj6jvf/environment" -m "{\"temperature\": 25.5, \"humidity\": 60.0, \"air_quality\": 95}"
echo ✅ 환경 데이터 전송 완료

echo.
echo 4. 컨베이어 제어 테스트
echo.
mosquitto_pub -h localhost -p 1883 -t "factory/g2qj6jvf/1/conveyor" -m "{\"command\": \"START\", \"reason\": \"Normal operation\"}"
echo ✅ 컨베이어 제어 명령 전송 완료

echo.
echo ====================================
echo MQTT 테스트 완료
echo ====================================
echo.
echo 메시지 수신 확인 (별도 창에서 실행):
echo mosquitto_sub -h localhost -p 1883 -t "factory/+/+/operations"
echo mosquitto_sub -h localhost -p 1883 -t "#"
echo.
pause