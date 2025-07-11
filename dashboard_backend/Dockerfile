# Alpine Linux 3.18 기반 OpenJDK 17 사용
FROM alpine:3.18

# 패키지 업데이트 및 OpenJDK 17 설치
RUN apk add --no-cache openjdk17-jdk

# 작업 디렉토리 설정
WORKDIR /app

# Gradle 관련 파일들 복사
COPY gradle gradle
COPY gradlew .
COPY build.gradle .
COPY settings.gradle .

# Gradle wrapper 실행 권한 부여
RUN chmod +x ./gradlew

# 소스 코드 복사 (application.properties 포함)
COPY src src

# 애플리케이션 빌드
RUN ./gradlew bootJar --no-daemon

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
CMD ["java", "-jar", "build/libs/dashboard_backend-0.0.1-SNAPSHOT.jar"]