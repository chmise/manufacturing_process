# ===================================
# 통합 Dockerfile - Frontend & Backend
# ===================================

# ===================================
# Backend Build Stage
# ===================================
FROM gradle:8.1.1-jdk17 AS backend-build

WORKDIR /app/backend

# Copy backend source
COPY dashboard_backend/ .

# Build Spring Boot application
RUN gradle clean build -x test

# ===================================
# Frontend Build Stage  
# ===================================
FROM node:18 AS frontend-build

WORKDIR /app/frontend

# Copy frontend source
COPY dashboard_frontend/package*.json ./
RUN npm ci

COPY dashboard_frontend/ .
RUN npm run build

# ===================================
# Backend Runtime Image (JDK 이미 포함!)
# ===================================
FROM openjdk:17 AS backend

WORKDIR /app

# Copy built JAR from backend-build stage
COPY --from=backend-build /app/backend/build/libs/*.jar app.jar

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=production

# Install useful tools (JDK에 이미 포함되어 있지만 추가 도구 설치 가능)
RUN apt-get update && apt-get install -y \
    curl \
    wget \
    net-tools \
    && rm -rf /var/lib/apt/lists/*

# Expose port
EXPOSE 8080

# Start application
CMD ["java", "-jar", "app.jar"]

# ===================================
# Frontend Runtime Image (Ubuntu 기반)
# ===================================
FROM ubuntu:22.04 AS frontend

# Install nginx and other tools
RUN apt-get update && apt-get install -y \
    nginx \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Remove default nginx config
RUN rm -rf /usr/share/nginx/html/*

# Copy built frontend from frontend-build stage
COPY --from=frontend-build /app/frontend/dist /usr/share/nginx/html

# Copy nginx configuration
COPY nginx.conf /etc/nginx/nginx.conf

# Create nginx PID directory
RUN mkdir -p /var/run/nginx

# Expose port
EXPOSE 80

# Start nginx
CMD ["nginx", "-g", "daemon off;"]