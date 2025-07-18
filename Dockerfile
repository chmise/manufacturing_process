# ===================================
# Backend Build Stage
# ===================================
FROM gradle:8.1.1-jdk17 AS backend-build

WORKDIR /app

# Copy backend source
COPY dashboard_backend/ .

# gradlew 대신 시스템 gradle 사용
RUN gradle clean build -x test --no-daemon

# ===================================
# Frontend Build Stage  
# ===================================
FROM node:18 AS frontend-build

WORKDIR /app

# Copy and build frontend
COPY dashboard_frontend/package*.json ./
RUN npm ci

COPY dashboard_frontend/ .
RUN npm run build

# ===================================
# Backend Runtime
# ===================================
FROM openjdk:17-jdk-slim AS backend


WORKDIR /app

# Copy JAR file
COPY --from=backend-build /app/build/libs/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=production

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]

# ===================================
# Frontend Runtime
# ===================================
FROM nginx:latest AS frontend

# Copy built frontend
COPY --from=frontend-build /app/dist /usr/share/nginx/html

# Copy nginx config
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 3000

CMD ["nginx", "-g", "daemon off;"]