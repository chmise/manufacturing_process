# 빌드 단계
FROM node:18-alpine as build

WORKDIR /app

# package.json과 package-lock.json 복사
COPY package*.json ./

# 의존성 설치
RUN npm ci

# 소스 코드 복사
COPY . .

# Vite 빌드 (dist 폴더에 생성됨)
RUN npm run build

# 실행 단계 - Alpine 기반 Nginx
FROM nginx:alpine

# 기본 nginx 설정 제거
RUN rm -rf /usr/share/nginx/html/*

# Vite 빌드 결과물(dist)을 Nginx 웹 루트로 복사
COPY --from=build /app/dist /usr/share/nginx/html

# 완전한 nginx 설정으로 교체
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]