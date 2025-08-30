# Docker 環境構築手順 🚀

## 概要

MeatMetrics プロジェクトの Docker 環境を構築するための詳細な手順書です。**Docker 内での完全な開発環境**を提供し、ローカルのソフトウェアインストールを最小限に抑えます。

## 前提条件

### 必要なソフトウェア

- Docker Desktop (Windows/Mac) または Docker Engine (Linux)
- Docker Compose v2.0 以上
- Git
- **※Maven、Node.js、PostgreSQL 等は不要（すべて Docker 内で実行）**

### システム要件

- メモリ: 最低 4GB、推奨 8GB 以上
- ディスク容量: 最低 10GB、推奨 20GB 以上
- CPU: 2 コア以上

## 🎯 開発環境の特徴

- **完全コンテナ化**: Maven、Node.js、PostgreSQL すべて Docker 内で実行
- **ホットリロード対応**: ソースコード変更の即座反映
- **ポート統一**: ホスト側で統一されたポート番号でアクセス
- **データ永続化**: コンテナ再起動時もデータベースデータが保持

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

## 5. 開発環境の構築・運用手順

### 5.1 初回セットアップ

```bash
# 1. プロジェクトルートに移動
cd /path/to/meatmetrics

# 2. 開発環境ディレクトリに移動
cd infrastructure/docker/dev

# 3. 初回ビルド&起動（時間がかかります）
docker compose up --build

# 4. バックグラウンド起動する場合
docker compose up -d --build
```

### 5.2 日常の開発フロー

```bash
# 開発開始
cd infrastructure/docker/dev
docker compose up -d

# 開発終了
docker compose down

# サービス状態確認
docker compose ps

# リアルタイムログ確認
docker compose logs -f

# 特定サービスのログ確認
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f postgres
```

### 5.3 アクセス URL

開発環境起動後、以下の URL でアクセスできます：

| サービス             | URL                   | 説明                           |
| -------------------- | --------------------- | ------------------------------ |
| **フロントエンド**   | http://localhost:5173 | React 開発サーバー             |
| **バックエンド API** | http://localhost:8081 | Spring Boot API                |
| **PostgreSQL**       | localhost:15432       | データベース（IDE 等から接続） |

### 5.4 リビルドが必要なケース

以下の変更を行った場合はリビルドが必要です：

```bash
# Dockerfileやpackage.json、pom.xmlを変更した場合
docker compose down
docker compose up --build

# または特定サービスのみリビルド
docker compose build backend
docker compose up -d backend

# 完全クリーンビルド（キャッシュも削除）
docker compose down
docker compose build --no-cache
docker compose up -d
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

## 6. Docker 開発環境での作業方法

### 6.1 コンテナ内でのコマンド実行

```bash
# バックエンドコンテナ内でMavenコマンドを実行
docker compose exec backend mvn test
docker compose exec backend mvn spring-boot:run

# フロントエンドコンテナ内でnpmコマンドを実行
docker compose exec frontend npm test
docker compose exec frontend npm run lint

# PostgreSQLコンテナ内でpsqlコマンドを実行
docker compose exec postgres psql -U meatmetrics -d meatmetrics
```

### 6.2 コンテナ内でのシェルアクセス

```bash
# バックエンドコンテナ内でシェルを起動
docker compose exec backend bash

# フロントエンドコンテナ内でシェルを起動
docker compose exec frontend sh

# PostgreSQLコンテナ内でシェルを起動
docker compose exec postgres bash
```

### 6.3 ファイル変更の反映

開発環境では以下のディレクトリがマウントされており、変更は即座に反映されます：

```yaml
# backend/src/ 配下の変更
- Spring Boot DevToolsによる自動再起動
- Java, properties, SQL ファイルの変更検知

# frontend/src/ 配下の変更
- Vite HMR（Hot Module Replacement）による即座更新
- TypeScript, CSS, React コンポーネントの変更検知
```

### 6.4 ポート設定の確認

現在の実際のポート設定：

```yaml
# docker-compose.yml 実際の設定
services:
  postgres:
    ports:
      - "15432:5432" # ホスト:15432 → コンテナ:5432

  backend:
    ports:
      - "8081:8080" # ホスト:8081 → コンテナ:8080
    environment:
      SERVER_PORT: 8080 # コンテナ内のSpring Bootポート

  frontend:
    ports:
      - "5173:5173" # ホスト:5173 → コンテナ:5173
```

## 7. トラブルシューティング

### 7.1 よくある問題と解決方法

#### 1. ポート競合エラー

```bash
# 使用中のポート確認（Windows）
netstat -an | findstr :8081
netstat -an | findstr :5173
netstat -an | findstr :15432

# 使用中のポート確認（Mac/Linux）
lsof -i :8081
lsof -i :5173
lsof -i :15432

# 競合するプロセスの停止（Windows）
taskkill /F /PID <PID>

# 競合するプロセスの停止（Mac/Linux）
kill -9 <PID>
```

#### 2. データベース接続エラー

```bash
# PostgreSQLコンテナの状態確認
docker compose ps postgres

# PostgreSQLのログ確認
docker compose logs postgres

# データベース接続テスト
docker compose exec postgres psql -U meatmetrics -d meatmetrics -c "SELECT version();"
```

#### 3. Spring Boot 起動失敗

```bash
# バックエンドのログ確認
docker compose logs backend

# Mavenキャッシュクリア
docker compose exec backend mvn clean

# バックエンドの再ビルド
docker compose build --no-cache backend
docker compose up -d backend
```

#### 4. ボリュームの問題

```bash
# ボリュームの詳細確認
docker volume inspect dev_postgres16_data
docker volume inspect dev_maven_cache

# ボリュームの削除（注意：データが失われます）
docker compose down
docker volume rm dev_postgres16_data

# 完全リセット
docker compose down -v  # ボリュームも削除
docker compose up --build
```

#### 5. ネットワークの問題

```bash
# ネットワークの詳細確認
docker network inspect dev_meatmetrics-network

# ネットワークの再作成
docker compose down
docker compose up -d
```

#### 6. キャッシュの問題

```bash
# Dockerイメージキャッシュクリア
docker system prune -f

# ビルドキャッシュクリア
docker builder prune -f

# 完全クリーンアップ
docker compose down -v
docker system prune -a -f
docker compose up --build
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

## 8. 開発効率を上げる Tips

### 8.1 便利なエイリアス設定

```bash
# ~/.bashrc または ~/.zshrc に追加
alias dcup='docker compose up -d'
alias dcdown='docker compose down'
alias dcbuild='docker compose build'
alias dclogs='docker compose logs -f'
alias dcps='docker compose ps'

# MeatMetrics専用エイリアス
alias mmdev='cd /path/to/meatmetrics/infrastructure/docker/dev'
alias mmup='mmdev && dcup'
alias mmdown='mmdev && dcdown'
alias mmrebuild='mmdev && dcdown && dcbuild && dcup'
```

### 8.2 IDE 設定のコツ

#### データベース接続設定

```
Host: localhost
Port: 15432
Database: meatmetrics
Username: meatmetrics
Password: meatmetrics123
```

#### デバッグポート設定

```yaml
# デバッグ用にポートを追加する場合
backend:
  ports:
    - "8081:8080"
    - "5005:5005" # デバッグポート
  environment:
    JAVA_TOOL_OPTIONS: "-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"
```

### 8.3 パフォーマンス改善

```bash
# Docker Desktop設定推奨値
- Memory: 6GB以上
- CPU: 4コア以上
- Disk Image Size: 100GB以上

# WSL2使用時（Windows）
# .wslconfig ファイル設定
[wsl2]
memory=6GB
processors=4
```

### 8.4 よく使うコマンド集

```bash
# 全体状況の確認
docker compose ps
docker compose top

# リソース使用量確認
docker stats

# ログのフィルタリング
docker compose logs backend | grep ERROR
docker compose logs frontend | grep WARN

# 特定時間のログ確認
docker compose logs --since="2024-01-01T10:00:00" backend

# コンテナ内のプロセス確認
docker compose exec backend ps aux
docker compose exec frontend ps aux

# ファイルの確認
docker compose exec backend ls -la /app
docker compose exec frontend ls -la /app

# 環境変数の確認
docker compose exec backend env
docker compose exec frontend env
```

### 8.5 開発ワークフローの例

```bash
# 1. 朝の開発開始
mmup && dclogs

# 2. フィーチャー開発中
# コードを編集（自動でホットリロード）
# 必要に応じてテスト実行
docker compose exec backend mvn test
docker compose exec frontend npm test

# 3. 依存関係追加時
docker compose exec backend mvn dependency:tree
docker compose exec frontend npm install new-package
# 必要に応じてリビルド
dcbuild backend

# 4. データベース操作
docker compose exec postgres psql -U meatmetrics -d meatmetrics

# 5. 開発終了
mmdown
```

## 9. 本番環境との差異

### 9.1 開発環境特有の設定

- **ボリュームマウント**: ソースコードの変更を即座反映
- **開発ツール有効**: Spring Boot DevTools, Vite HMR
- **デバッグログ有効**: 詳細なログ出力
- **外部ポート公開**: 各サービスへの直接アクセス

### 9.2 本番環境での変更点

- **ビルド済み成果物**: ソースコードはコンテナ内にコピー
- **最適化設定**: プロダクション用の設定値
- **Nginx 使用**: リバースプロキシ経由でのアクセス
- **セキュリティ強化**: 最小限のポートのみ公開

---

**最終更新**: 2025 年 8 月 28 日  
**更新者**: 開発チーム  
**次回更新予定**: 環境構築完了後

## 🔗 関連ドキュメント

- [Docker 環境アーキテクチャ](./architecture.md)
- [デプロイ・運用手順](./deployment.md)
- [CI/CD 設定ガイド](../2_detail/11_ci_cd.md)
