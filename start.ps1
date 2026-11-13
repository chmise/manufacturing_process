# 디지털 트윈 시스템 개발용 실행 - PowerShell 버전
# (전체를 Docker로 띄우려면 이 스크립트 대신 `docker compose up -d --build`를 쓰세요)
# UTF-8 인코딩 설정
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
chcp 65001 | Out-Null

Write-Host "🏭 디지털 트윈 시스템 시작 (개발 모드)" -ForegroundColor Green
Write-Host "==================" -ForegroundColor Green

# 현재 디렉토리 저장
$rootPath = Get-Location

# 백엔드는 저장소 루트의 .env에서 DB 접속 정보와 JWT_SECRET을 읽는다
if (-not (Test-Path (Join-Path $rootPath ".env"))) {
    Write-Host "❌ .env 파일이 없습니다. 먼저 .env.example을 .env로 복사한 뒤 값을 채워주세요." -ForegroundColor Red
    exit 1
}
Write-Host "ℹ️  PostgreSQL이 떠 있어야 합니다. 없다면: docker compose up -d postgres" -ForegroundColor Cyan

# Mosquitto 설치 경로 (다르면 MOSQUITTO_EXE 환경변수로 지정)
$mosquittoExe = if ($env:MOSQUITTO_EXE) { $env:MOSQUITTO_EXE } else { "C:\Program Files\mosquitto\mosquitto.exe" }

Write-Host "`n1️⃣ Spring Boot 백엔드 시작 중..." -ForegroundColor Yellow
Start-Job -Name "Backend" -ScriptBlock { 
    param($path)
    Set-Location "$path\dashboard_backend"
    .\gradlew.bat bootRun 
} -ArgumentList $rootPath

Write-Host "2️⃣ Mosquitto MQTT 브로커 시작 중..." -ForegroundColor Yellow  
Start-Job -Name "Mosquitto" -ScriptBlock { 
    param($exe)
    & $exe -v 
} -ArgumentList $mosquittoExe

Write-Host "3️⃣ React 프론트엔드 시작 중..." -ForegroundColor Yellow
Start-Job -Name "Frontend" -ScriptBlock { 
    param($path)
    Set-Location "$path\dashboard_frontend"
    npm run dev 
} -ArgumentList $rootPath

Write-Host "`n⏳ 서비스 시작 대기 중..." -ForegroundColor Cyan
Start-Sleep -Seconds 10

# 작업 상태 확인
Write-Host "`n📊 실행 중인 서비스:" -ForegroundColor Green
Get-Job | Format-Table Name, State, HasMoreData

Write-Host "`n🌐 접속 주소:" -ForegroundColor Green
Write-Host "  • 대시보드: http://localhost:5173" -ForegroundColor White
Write-Host "  • 백엔드 API: http://localhost:8080" -ForegroundColor White

Write-Host "`n📝 유용한 명령어:" -ForegroundColor Green
Write-Host "  • 상태 확인: Get-Job" -ForegroundColor White
Write-Host "  • 로그 보기: Receive-Job -Name 'Backend'" -ForegroundColor White
Write-Host "  • 서비스 종료: Stop-Job -Name 'Backend'" -ForegroundColor White
Write-Host "  • 모든 종료: Get-Job | Stop-Job; Get-Job | Remove-Job" -ForegroundColor White

Write-Host "`n🛑 종료하려면 아무 키나 누르세요..." -ForegroundColor Red
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")

Write-Host "`n🛑 모든 서비스 종료 중..." -ForegroundColor Red
Get-Job | Stop-Job
Get-Job | Remove-Job
Write-Host "✓ 모든 프로세스 종료 완료" -ForegroundColor Green