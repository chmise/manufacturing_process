#!/usr/bin/env python3
"""
KPI 운영데이터 MQTT 테스트 스크립트
- u1mobis 회사: 모든 KPI 우수 (OEE 95%+, FTY 95%+, OTD 98%+)
- clear 회사: OEE 미달(65%), FTY 보통(80%), OTD 보통(85%)
"""

import paho.mqtt.client as mqtt
import json
import time
from datetime import datetime

# MQTT 브로커 설정
MQTT_BROKER = "localhost"
MQTT_PORT = 1883

def on_connect(client, userdata, flags, rc):
    if rc == 0:
        print(f"MQTT 브로커에 연결됨: {MQTT_BROKER}:{MQTT_PORT}")
    else:
        print(f"MQTT 연결 실패: {rc}")

def on_publish(client, userdata, mid):
    print(f"메시지 발송 완료: MID {mid}")

def create_excellent_kpi_data():
    """u1mobis용 우수 KPI 데이터 생성 (모든 지표 우수)"""
    return {
        "planned_time": 480,           # 8시간 = 480분
        "downtime": 10,               # 정지시간 10분 -> Availability 97.9%
        "target_cycle_time": 30.0,    # 목표 사이클타임 30초
        "good_count": 450,            # 양품 450개
        "total_count": 450,           # 총 생산 450개 -> Quality 100%
        "first_time_pass_count": 445, # 일발통과 445개 -> FTY 98.9%
        "on_time_delivery_count": 448 # 정시납기 448개 -> OTD 99.6%
        # 예상 OEE: 97.9% × 100% × 100% = 97.9% (95% 이상 우수)
    }

def create_mixed_kpi_data():
    """clear용 혼합 KPI 데이터 생성 (1개 미달, 2개 보통)"""
    return {
        "planned_time": 480,           # 8시간 = 480분
        "downtime": 120,              # 정지시간 120분 -> Availability 75%
        "target_cycle_time": 35.0,    # 목표 사이클타임 35초
        "good_count": 290,            # 양품 290개
        "total_count": 300,           # 총 생산 300개 -> Quality 96.7%
        "first_time_pass_count": 240, # 일발통과 240개 -> FTY 80% (보통)
        "on_time_delivery_count": 255 # 정시납기 255개 -> OTD 85% (보통)
        # 예상 OEE: 75% × 87% × 96.7% = 63.2% (70% 미만 미달)
    }

def send_kpi_mqtt_messages():
    """KPI MQTT 메시지 전송"""
    client = mqtt.Client()
    client.on_connect = on_connect
    client.on_publish = on_publish
    
    try:
        # MQTT 브로커 연결
        client.connect(MQTT_BROKER, MQTT_PORT, 60)
        client.loop_start()
        
        time.sleep(2)  # 연결 대기
        
        print("\n=== KPI 운영데이터 MQTT 테스트 시작 ===")
        print(f"시간: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
        
        # 1. u1mobis 회사 - 우수 KPI 데이터 전송
        print("\n1. u1mobis 회사 - 우수 KPI 데이터 전송")
        u1mobis_topic = "factory/g2qj6jvf/1/operations"  # g2qj6jvf = u1mobis 회사코드
        u1mobis_data = create_excellent_kpi_data()
        
        print(f"토픽: {u1mobis_topic}")
        print(f"데이터: {json.dumps(u1mobis_data, indent=2, ensure_ascii=False)}")
        
        result = client.publish(u1mobis_topic, json.dumps(u1mobis_data))
        if result.rc == mqtt.MQTT_ERR_SUCCESS:
            print("✅ u1mobis KPI 데이터 전송 성공")
        else:
            print(f"❌ u1mobis KPI 데이터 전송 실패: {result.rc}")
        
        time.sleep(2)
        
        # 2. clear 회사 - 혼합 KPI 데이터 전송 (1개 미달, 2개 보통)
        print("\n2. clear 회사 - 혼합 KPI 데이터 전송")
        clear_topic = "factory/fdyeaqhl/1/operations"    # fdyeaqhl = clear 회사코드
        clear_data = create_mixed_kpi_data()
        
        print(f"토픽: {clear_topic}")
        print(f"데이터: {json.dumps(clear_data, indent=2, ensure_ascii=False)}")
        
        result = client.publish(clear_topic, json.dumps(clear_data))
        if result.rc == mqtt.MQTT_ERR_SUCCESS:
            print("✅ clear KPI 데이터 전송 성공")
        else:
            print(f"❌ clear KPI 데이터 전송 실패: {result.rc}")
        
        time.sleep(2)
        
        print("\n=== KPI 예상 계산 결과 ===")
        print("📊 u1mobis 회사:")
        print("  - OEE: ~97.9% (우수 - 95% 이상)")
        print("  - FTY: ~98.9% (우수 - 85% 이상)")
        print("  - OTD: ~99.6% (우수 - 90% 이상)")
        print("  → 모든 지표 우수 등급")
        
        print("\n📊 clear 회사:")
        print("  - OEE: ~63.2% (미달 - 70% 미만)")
        print("  - FTY: ~80.0% (보통 - 85% 미만)")
        print("  - OTD: ~85.0% (보통 - 90% 미만)")
        print("  → 1개 미달, 2개 보통 등급")
        
        print("\n=== MQTT 테스트 완료 ===")
        
    except Exception as e:
        print(f"❌ MQTT 전송 중 오류 발생: {e}")
    finally:
        client.loop_stop()
        client.disconnect()

if __name__ == "__main__":
    print("KPI 운영데이터 MQTT 테스트 스크립트")
    print("=" * 50)
    
    # 필요한 라이브러리 확인
    try:
        import paho.mqtt.client as mqtt
        print("✅ paho-mqtt 라이브러리 확인됨")
    except ImportError:
        print("❌ paho-mqtt 라이브러리가 필요합니다:")
        print("pip install paho-mqtt")
        exit(1)
    
    send_kpi_mqtt_messages()