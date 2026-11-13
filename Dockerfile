# 멀티 스테이지 빌드. docker-compose.yml에서 target으로 backend / frontend 이미지를 각각 만든다.
#   docker build --target backend  -t factory-backend .
#   docker build --target frontend -t factory-frontend .

# ===================================
# 1) 백엔드 빌드 (Gradle 래퍼 버전 그대로 사용)
# ===================================
FROM eclipse-temurin:17-jdk AS backend-build

WORKDIR /app

# 의존성 캐시를 위해 빌드 설정을 먼저 복사
COPY dashboard_backend/gradlew dashboard_backend/settings.gradle dashboard_backend/build.gradle ./
COPY dashboard_backend/gradle ./gradle
RUN chmod +x gradlew && ./gradlew dependencies --no-daemon > /dev/null

COPY dashboard_backend/src ./src
# 테스트는 CI(.github/workflows/ci.yml)에서 따로 돌린다
RUN ./gradlew bootJar --no-daemon

# ===================================
# 2) 프론트엔드 빌드
# ===================================
FROM node:22-alpine AS frontend-build

WORKDIR /app

COPY dashboard_frontend/package*.json ./
RUN npm ci

COPY dashboard_frontend/ .

# 비워두면 같은 주소의 /api, /ws를 쓴다 (nginx가 백엔드로 넘김)
ARG VITE_API_URL=""
ARG VITE_OPENWEATHER_API_KEY=""
ENV VITE_API_URL=$VITE_API_URL \
    VITE_OPENWEATHER_API_KEY=$VITE_OPENWEATHER_API_KEY
RUN npm run build

# ===================================
# 3) 백엔드 실행 이미지
# ===================================
FROM eclipse-temurin:17-jre-alpine AS backend

RUN apk add --no-cache tzdata

WORKDIR /app

COPY --from=backend-build /app/build/libs/*.jar app.jar

ENV TZ=Asia/Seoul
# 메모리가 작은 인스턴스(t2.micro 등)에 맞춘 JVM 설정. 필요하면 JAVA_OPTS 환경변수로 덮어쓴다
ENV JAVA_OPTS="-Xms200m -Xmx400m -XX:MaxMetaspaceSize=128m -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+UseStringDeduplication"

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]

# ===================================
# 4) 프론트엔드 실행 이미지 (nginx가 정적 파일 제공 + /api, /ws를 백엔드로 전달)
# ===================================
FROM nginx:alpine AS frontend

COPY nginx.conf /etc/nginx/nginx.conf
COPY --from=frontend-build /app/dist /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
