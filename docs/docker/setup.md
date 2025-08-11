# Docker 環境構築手順 🚀

## 概要

MeatMetrics プロジェクトの Docker 環境を構築するための詳細な手順書です。開発環境と本番環境の両方に対応しています。

## 前提条件

### 必要なソフトウェア

- Docker Desktop (Windows/Mac) または Docker Engine (Linux)
- Docker Compose v2.0 以上
- Git

### システム要件

- メモリ: 最低 4GB、推奨 8GB 以上
- ディスク容量: 最低 10GB、推奨 20GB 以上
- CPU: 2 コア以上

## ディレクトリ構造（実体）

```
meatmetrics/
└── infrastructure/
    └── docker/
        ├── dev/
        │   └── docker-compose.yml       # 開発用（Frontend/Backend/Postgres）
        ├── prod/
        │   └── docker-compose.yml       # 本番用（Nginx/Backend/Postgres）
        ├── backend/
        │   ├── Dockerfile               # Spring Boot (prod)
        │   └── Dockerfile.dev           # Spring Boot (dev)
        ├── frontend/
        │   ├── Dockerfile               # React build → Nginx 配信 (prod)
        │   └── Dockerfile.dev           # Vite dev server (dev)
        ├── nginx/
        │   └── nginx.conf               # 80 番で配信＋ /api → backend
        ├── postgres/
        │   ├── Dockerfile               # Postgres (prod)
        │   └── Dockerfile.dev           # Postgres (dev)
        └── init/                        # DB 初期化 SQL
```

## 1. バックエンドコンテナの構築

### 1.1 Spring Boot 用 Dockerfile 作成

```dockerfile
# backend/Dockerfile
FROM maven:3.9.4-openjdk-17 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM openjdk:17-jre-slim

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
CMD ["java", "-jar", "app.jar"]
```

### 1.2 マルチステージビルドの最適化

```dockerfile
# backend/Dockerfile (最適化版)
FROM maven:3.9.4-openjdk-17 AS build

WORKDIR /app

# 依存関係のキャッシュ
COPY pom.xml .
RUN mvn dependency:go-offline

# ソースコードのコピーとビルド
COPY src ./src
RUN mvn clean package -DskipTests

# 実行環境
FROM openjdk:17-jre-slim AS runtime

WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# JVM設定の最適化
ENV JAVA_OPTS="-Xmx512m -Xms256m"

EXPOSE 8080
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

## 2. フロントエンドコンテナの構築

### 2.1 React 用 Dockerfile 作成

```dockerfile
# frontend/Dockerfile
FROM node:18-alpine AS build

WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production

COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

### 2.2 開発環境用 Dockerfile

```dockerfile
# frontend/Dockerfile.dev
FROM node:18-alpine

WORKDIR /app
COPY package*.json ./
RUN npm install

COPY . .

EXPOSE 3000
CMD ["npm", "start"]
```

## 3. Nginx コンテナの構築

### 3.1 Nginx 設定ファイル

```nginx
# nginx/nginx.conf
events {
    worker_connections 1024;
}

http {
    upstream backend {
        server backend:8080;
    }

    upstream frontend {
        server frontend:3000;
    }

    server {
        listen 80;
        server_name localhost;

        # フロントエンド（React）
        location / {
            proxy_pass http://frontend;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # バックエンドAPI
        location /api/ {
            proxy_pass http://backend/;
            proxy_set_header Host $host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
        }

        # 静的ファイル
        location /static/ {
            proxy_pass http://frontend;
            expires 1y;
            add_header Cache-Control "public, immutable";
        }
    }
}
```

### 3.2 Nginx 用 Dockerfile

```dockerfile
# nginx/Dockerfile
FROM nginx:alpine

COPY nginx.conf /etc/nginx/nginx.conf
COPY conf.d/default.conf /etc/nginx/conf.d/default.conf

EXPOSE 80 443
CMD ["nginx", "-g", "daemon off;"]
```

## 4. Docker Compose 設定（実体のファイル名に更新）

### 4.1 開発環境用

```yaml
# infrastructure/docker/dev/docker-compose.yml（抜粋）
version: "3.8"

services:
  postgres:
    build:
      context: ..
      dockerfile: ./postgres/Dockerfile.dev
    container_name: meatmetrics-postgres-dev
    environment:
      POSTGRES_DB: meatmetrics
      POSTGRES_USER: meatmetrics
      POSTGRES_PASSWORD: meatmetrics123
    ports:
      - "15432:5432"
    volumes:
      - postgres16_data:/var/lib/postgresql/data
      - ../init:/docker-entrypoint-initdb.d
    networks:
      - meatmetrics-network

  backend:
    build:
      context: ../../../backend
      dockerfile: ../backend/Dockerfile.dev
    container_name: meatmetrics-backend-dev
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/meatmetrics
      SPRING_DATASOURCE_USERNAME: meatmetrics
      SPRING_DATASOURCE_PASSWORD: meatmetrics123
    volumes:
      - ../../../backend:/app
      - maven_cache:/root/.m2
    networks:
      - meatmetrics-network
    depends_on:
      - postgres

  frontend:
    build:
      context: ../../../frontend
      dockerfile: ../frontend/Dockerfile.dev
    container_name: meatmetrics-frontend-dev
    volumes:
      - ../../../frontend:/app
      - /app/node_modules
    networks:
      - meatmetrics-network

  nginx:
    build:
      context: ./nginx
      dockerfile: Dockerfile
    container_name: meatmetrics-nginx-dev
    ports:
      - "80:80"
    networks:
      - meatmetrics-network
    depends_on:
      - frontend
      - backend

volumes:
  postgres16_data:
  maven_cache:

networks:
  meatmetrics-network:
    driver: bridge
```

### 4.2 本番環境用

```yaml
# infrastructure/docker/prod/docker-compose.yml（抜粋）
version: "3.8"

services:
  postgres:
    build:
      context: ..
      dockerfile: ./postgres/Dockerfile
    container_name: meatmetrics-postgres-prod
    environment:
      POSTGRES_DB: meatmetrics
      POSTGRES_USER: meatmetrics
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
    volumes:
      - postgres16_data:/var/lib/postgresql/data
      - ../init:/docker-entrypoint-initdb.d
    networks:
      - meatmetrics-network
    restart: unless-stopped

  backend:
    build:
      context: ../../../backend
      dockerfile: ../backend/Dockerfile
    container_name: meatmetrics-backend-prod
    environment:
      SPRING_PROFILES_ACTIVE: prod
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/meatmetrics
      SPRING_DATASOURCE_USERNAME: meatmetrics
      SPRING_DATASOURCE_PASSWORD: ${POSTGRES_PASSWORD}
    networks:
      - meatmetrics-network
    depends_on:
      - postgres
    restart: unless-stopped

  frontend:
    build:
      context: ../../frontend
      dockerfile: Dockerfile
    container_name: meatmetrics-frontend-prod
    networks:
      - meatmetrics-network
    restart: unless-stopped

  nginx:
    image: meatmetrics-frontend
    build:
      context: ../../../frontend
      dockerfile: ../frontend/Dockerfile
    container_name: meatmetrics-nginx-prod
    ports:
      - "80:80"
    volumes:
      - ../nginx/nginx.conf:/etc/nginx/nginx.conf:ro
    networks:
      - meatmetrics-network
    depends_on:
      - backend
    restart: unless-stopped

volumes:
  postgres16_data:

networks:
  meatmetrics-network:
    driver: bridge
```

## 5. 環境構築手順

### 5.1 開発環境の起動

```bash
# 1. ディレクトリ移動
cd infrastructure/docker/dev

# 2. 開発環境の起動
docker compose up -d --build

# 3. ログの確認
docker compose logs -f

# 4. 各サービスの状態確認
docker compose ps
```

### 5.2 本番環境の起動

```bash
# 1. ディレクトリ移動
cd infrastructure/docker/prod

# 2. 環境変数ファイルの用意（任意）
cp ../../.env.example .env  # 例: ある場合

# 3. 本番環境の起動
docker compose up -d --build

# 4. ログの確認
docker compose logs -f
```

## 6. 動作確認

### 6.1 各サービスの確認

```bash
# PostgreSQL接続確認
docker exec -it meatmetrics-postgres-dev psql -U meatmetrics -d meatmetrics

# バックエンドAPI確認
curl http://localhost/api/health

# フロントエンド確認
curl http://localhost

# Nginx設定確認
docker exec -it meatmetrics-nginx-dev nginx -t
```

### 6.2 ログの確認

```bash
# 全サービスのログ
docker compose logs

# 特定サービスのログ
docker compose logs backend

# リアルタイムログ
docker compose logs -f nginx
```

## 7. トラブルシューティング

### 7.1 よくある問題と解決方法

#### ポート競合

```bash
# 使用中のポート確認
netstat -an | findstr :80
netstat -an | findstr :8080

# 競合するプロセスの停止
taskkill /F /PID <PID>
```

#### ボリュームの問題

```bash
# ボリュームの詳細確認
docker volume inspect postgres16_data

# ボリュームの削除（注意：データが失われます）
docker volume rm postgres16_data
```

#### ネットワークの問題

```bash
# ネットワークの詳細確認
docker network inspect meatmetrics_meatmetrics-network

# ネットワークの再作成
docker-compose -f docker-compose.dev.yml down
docker-compose -f docker-compose.dev.yml up -d
```

### 7.2 デバッグ用コマンド

```bash
# コンテナ内でのシェル実行
docker exec -it meatmetrics-backend-dev /bin/bash
docker exec -it meatmetrics-frontend-dev /bin/sh

# ファイルシステムの確認
docker exec -it meatmetrics-backend-dev ls -la /app
docker exec -it meatmetrics-frontend-dev ls -la /app

# プロセスの確認
docker exec -it meatmetrics-backend-dev ps aux
```

## 8. パフォーマンス最適化

### 8.1 リソース制限の設定

```yaml
# docker-compose.dev.yml に追加
services:
  backend:
    deploy:
      resources:
        limits:
          memory: 1G
          cpus: "0.5"
        reservations:
          memory: 512M
          cpus: "0.25"
```

### 8.2 キャッシュの最適化

```dockerfile
# backend/Dockerfile の最適化
FROM maven:3.9.4-openjdk-17 AS build

WORKDIR /app

# 依存関係のキャッシュ
COPY pom.xml .
RUN mvn dependency:go-offline -B

# ソースコードのコピーとビルド
COPY src ./src
RUN mvn clean package -DskipTests -B
```

## 9. セキュリティ設定

### 9.1 環境変数の管理

```bash
# .env ファイルの作成
POSTGRES_PASSWORD=your_secure_password
JWT_SECRET=your_jwt_secret
API_KEY=your_api_key
```

### 9.2 ネットワークの分離

```yaml
# 本番環境でのネットワーク分離（必要に応じて）
networks:
  meatmetrics-network:
    driver: bridge
    internal: true
```

---

**最終更新**: 2025 年 8 月 11 日  
**更新者**: 開発チーム  
**次回更新予定**: 環境構築完了後
