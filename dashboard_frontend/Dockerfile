# 빌드 단계
FROM node:18-alpine as build

WORKDIR /app

# package.json과 package-lock.json 복사
COPY package*.json ./

# 의존성 설치
RUN npm ci --only=production

# 소스 코드 복사
COPY . .

# React 앱 빌드
RUN npm run build

# 실행 단계 - Alpine 기반 Nginx
FROM alpine:3.18

# Nginx 설치
RUN apk add --no-cache nginx && \
    mkdir -p /var/log/nginx && \
    mkdir -p /var/lib/nginx/tmp && \
    chown -R nginx:nginx /var/lib/nginx

# 빌드된 React 앱을 Nginx 웹 루트로 복사
COPY --from=build /app/dist /usr/share/nginx/html

# Nginx 설정 (React Router SPA 지원)
RUN echo 'server {' > /etc/nginx/http.d/default.conf && \
    echo '    listen 80;' >> /etc/nginx/http.d/default.conf && \
    echo '    server_name _;' >> /etc/nginx/http.d/default.conf && \
    echo '    root /usr/share/nginx/html;' >> /etc/nginx/http.d/default.conf && \
    echo '    index index.html index.htm;' >> /etc/nginx/http.d/default.conf && \
    echo '    location / {' >> /etc/nginx/http.d/default.conf && \
    echo '        try_files $uri $uri/ /index.html;' >> /etc/nginx/http.d/default.conf && \
    echo '    }' >> /etc/nginx/http.d/default.conf && \
    echo '}' >> /etc/nginx/http.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]