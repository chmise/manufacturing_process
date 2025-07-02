#!/usr/bin/env python3
"""
현대차 조립라인 MQTT 시뮬레이션 실행 스크립트
RFID 추적 및 실시간 센서 데이터 시뮬레이션
"""

import sys
import os

# 프로젝트 루트를 Python 경로에 추가
current_dir = os.path.dirname(os.path.abspath(__file__))
project_root = os.path.dirname(current_dir)
sys.path.append(project_root)

# from mosquitto_MQTT.assembly.assembly_simulator import AssemblyLineSimulator  # 수동 모드에서는 사용 안함

def main():
    print("🏭 현대차 조립라인 MQTT 시뮬레이터 (수동 모드)")
    print("=" * 50)
    print("🚗 2D Digital Twin에서 차량 발주 버튼으로 제어")
    print("📍 스테이션: A01(도어탈거) → A02(배선) → ... → D03(수밀검사)")
    print("📡 MQTT 자동 발행: 비활성화")
    print("🔧 현장 데이터 시뮬레이션: React Frontend에서 구현")
    print("⚙️  실시간 차량 추적: 2D Twin 캔버스에서 시각화")
    print()
    print("ℹ️  이제 React Dashboard(http://localhost:5173)에서")
    print("   Factory2D Twin 페이지로 이동하여 차량 발주 버튼을 사용하세요!")
    print()
    
    # MQTT 자동 발행 비활성화 - 대기 모드
    print("🔄 대기 모드 - React Frontend에서 제어 중...")
    print("   (Ctrl+C로 종료)")
    
    try:
        # 무한 대기 (MQTT 자동 발행 없음)
        import time
        while True:
            time.sleep(1)
    except KeyboardInterrupt:
        print("\n👋 시뮬레이션을 종료합니다.")
    except Exception as e:
        print(f"\n❌ 오류 발생: {e}")

if __name__ == "__main__":
    main()