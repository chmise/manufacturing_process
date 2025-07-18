# ===================================
# Backend Build Stage
# ===================================
FROM gradle:8.1.1-jdk17-alpine AS backend-build

WORKDIR /app

# Copy backend source
COPY dashboard_backend/ .

# Alpine에서 빌드 (메모리 제한 제거)
RUN gradle clean build -x test --no-daemon

# ===================================
# Frontend Build Stage  
# ===================================
FROM node:18-alpine AS frontend-build

WORKDIR /app

# Copy and build frontend
COPY dashboard_frontend/package*.json ./
RUN npm ci --prefer-offline

COPY dashboard_frontend/ .
RUN npm run build

# ===================================
# Backend Runtime
# ===================================
FROM openjdk:17-alpine AS backend

# Alpine에서 필요한 패키지 설치
RUN apk add --no-cache curl tzdata

WORKDIR /app

COPY --from=backend-build /app/build/libs/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=production
ENV TZ=Asia/Seoul

EXPOSE 8080

# 메모리 최적화된 JVM 설정 (t3.micro용)
CMD ["java", \
     "-Xmx400m", \
     "-Xms200m", \
     "-XX:MaxMetaspaceSize=128m", \
     "-XX:+UseG1GC", \
     "-XX:MaxGCPauseMillis=200", \
     "-XX:+UseStringDeduplication", \
     "-jar", "app.jar"]

# ===================================
# Frontend Runtime
# ===================================
FROM nginx:alpine AS frontend

# nginx 설정 복사
COPY nginx.conf /etc/nginx/nginx.conf

# 빌드된 프론트엔드 복사
COPY --from=frontend-build /app/dist /usr/share/nginx/html

# nginx 포트 변경 (ALB 3000포트 맞춤)
RUN sed -i 's/listen 80;/listen 3000;/' /etc/nginx/nginx.conf

EXPOSE 3000

CMD ["nginx", "-g", "daemon off;"]
