#!/bin/bash
# 디지털 트윈 시스템 개발용 실행 - macOS/Linux 버전
# (전체를 Docker로 띄우려면 이 스크립트 대신 `docker compose up -d --build`를 쓰세요)

echo "🏭 디지털 트윈 시스템 시작 (개발 모드)"
echo "=================="

# 현재 디렉토리 저장
ROOT_PATH=$(pwd)

# 백엔드는 저장소 루트의 .env에서 DB 접속 정보와 JWT_SECRET을 읽는다
if [ ! -f "$ROOT_PATH/.env" ]; then
  echo "❌ .env 파일이 없습니다. 먼저 'cp .env.example .env' 후 값을 채워주세요."
  exit 1
fi
echo "ℹ️  PostgreSQL이 떠 있어야 합니다. 없다면: docker compose up -d postgres"

echo
echo "1️⃣ Spring Boot 백엔드 시작 중..."
cd "$ROOT_PATH/dashboard_backend"
./gradlew bootRun &
BACKEND_PID=$!

echo "2️⃣ Mosquitto MQTT 브로커 시작 중..."
mosquitto -v &
MQTT_PID=$!

echo "3️⃣ React 프론트엔드 시작 중..."
cd "$ROOT_PATH/dashboard_frontend"
npm run dev &
FRONTEND_PID=$!

cd "$ROOT_PATH"

echo
echo "⏳ 서비스 시작 대기 중..."
sleep 10

echo
echo "📊 실행 중인 서비스:"
echo "  • 백엔드 (PID: $BACKEND_PID)"
echo "  • MQTT 브로커 (PID: $MQTT_PID)" 
echo "  • 프론트엔드 (PID: $FRONTEND_PID)"

echo
echo "🌐 접속 주소:"
echo "  • 대시보드: http://localhost:5173"
echo "  • 백엔드 API: http://localhost:8080"

echo
echo "📝 유용한 명령어:"
echo "  • 프로세스 확인: ps aux | grep -E '(gradle|mosquitto|npm)'"
echo "  • 백엔드 종료: kill $BACKEND_PID"
echo "  • MQTT 종료: kill $MQTT_PID"
echo "  • 프론트엔드 종료: kill $FRONTEND_PID"

echo
echo "🛑 종료하려면 아무 키나 누르세요..."
read -n 1 -s

echo
echo "🛑 모든 서비스 종료 중..."
kill $BACKEND_PID $MQTT_PID $FRONTEND_PID 2>/dev/null
wait $BACKEND_PID $MQTT_PID $FRONTEND_PID 2>/dev/null

echo "✓ 모든 프로세스 종료 완료"