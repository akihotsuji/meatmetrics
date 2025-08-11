# Docker 環境デプロイ・運用手順 📦

## 概要

MeatMetrics プロジェクトの Docker 環境を本番環境にデプロイし、運用管理を行うための詳細な手順書です。

## デプロイ戦略

### 1. デプロイ方式

- **Blue-Green Deployment**: ダウンタイムなしでのデプロイ
- **Rolling Update**: 段階的な更新によるリスク分散
- **Canary Deployment**: 一部ユーザーへの段階的リリース

### 2. 環境構成

```
開発環境 (Development)
├── ローカル開発環境
├── 開発サーバー環境
└── ステージング環境

本番環境 (Production)
├── 本番サーバー環境
├── バックアップ環境
└── ディザスタリカバリ環境
```

## 1. 本番環境デプロイ手順

### 1.1 事前準備

#### サーバー要件確認

```bash
# システム要件チェック
docker --version
docker-compose --version
df -h
free -h
nproc
```

#### セキュリティ設定

```bash
# ファイアウォール設定
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 22/tcp

# Dockerセキュリティ設定
sudo usermod -aG docker $USER
sudo systemctl enable docker
sudo systemctl start docker
```

### 1.2 環境変数ファイルの作成

```bash
# .env.prod ファイルの作成
cat > .env.prod << EOF
# データベース設定
POSTGRES_PASSWORD=your_secure_password_here
POSTGRES_USER=meatmetrics
POSTGRES_DB=meatmetrics

# アプリケーション設定
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=your_jwt_secret_here
API_KEY=your_api_key_here

# 外部サービス設定
AWS_ACCESS_KEY_ID=your_aws_key
AWS_SECRET_ACCESS_KEY=your_aws_secret
AWS_REGION=ap-northeast-1

# 監視設定
LOG_LEVEL=INFO
METRICS_ENABLED=true
EOF

# 権限設定
chmod 600 .env.prod
```

### 1.3 SSL 証明書の準備

```bash
# Let's Encrypt証明書の取得
sudo apt-get install certbot
sudo certbot certonly --standalone -d yourdomain.com

# 証明書の配置
sudo mkdir -p /etc/nginx/ssl
sudo cp /etc/letsencrypt/live/yourdomain.com/fullchain.pem /etc/nginx/ssl/
sudo cp /etc/letsencrypt/live/yourdomain.com/privkey.pem /etc/nginx/ssl/
sudo chmod 600 /etc/nginx/ssl/*
```

### 1.4 本番環境の起動

```bash
# 1. ディレクトリ移動
cd infrastructure/docker/prod

# 2. 本番環境の起動
docker compose --env-file .env.prod up -d --build

# 3. 起動確認
docker compose ps

# 4. ログ確認
docker compose logs -f
```

## 2. 継続的デプロイメント (CD)

### 2.1 GitHub Actions 設定

```yaml
# .github/workflows/deploy.yml
name: Deploy to Production

on:
  push:
    branches: [main]
  workflow_dispatch:

jobs:
  deploy:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3

      - name: Deploy to server
        uses: appleboy/ssh-action@v0.1.5
        with:
          host: ${{ secrets.HOST }}
          username: ${{ secrets.USERNAME }}
          key: ${{ secrets.KEY }}
          script: |
            cd /opt/meatmetrics/infrastructure/docker/prod
            git -C ../../.. pull origin main
            docker compose --env-file .env.prod down
            docker compose --env-file .env.prod up -d --build
            docker system prune -f
```

### 2.2 デプロイスクリプト

```bash
#!/bin/bash
# deploy.sh

set -e

echo "🚀 デプロイ開始: $(date)"

# 環境変数の読み込み
source .env.prod

# 最新コードの取得
git pull origin main

# 既存コンテナの停止
docker compose down

# イメージの再ビルド
docker compose build --no-cache

# 新コンテナの起動
docker compose up -d

# ヘルスチェック
echo "⏳ ヘルスチェック中..."
sleep 30

# アプリケーションの動作確認
if curl -f http://localhost/api/health; then
    echo "✅ デプロイ成功"
else
    echo "❌ デプロイ失敗 - ロールバック実行"
    docker compose down
    docker compose up -d
    exit 1
fi

# 古いイメージの削除
docker system prune -f

echo "🎉 デプロイ完了: $(date)"
```

## 3. 監視・ログ管理

### 3.1 ログ収集設定

```yaml
# docker-compose.prod.yml に追加
services:
  backend:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

  frontend:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"

  nginx:
    logging:
      driver: "json-file"
      options:
        max-size: "10m"
        max-file: "3"
```

### 3.2 ログローテーション設定

```bash
# /etc/logrotate.d/docker
/var/lib/docker/containers/*/*.log {
    rotate 7
    daily
    compress
    size=1M
    missingok
    delaycompress
    copytruncate
}
```

### 3.3 監視スクリプト

```bash
#!/bin/bash
# health_check.sh

# ヘルスチェック関数
check_service() {
    local service=$1
    local url=$2

    if curl -f -s "$url" > /dev/null; then
        echo "✅ $service: 正常"
        return 0
    else
        echo "❌ $service: 異常"
        return 1
    fi
}

# 各サービスのチェック
check_service "Frontend" "http://localhost"
check_service "Backend API" "http://localhost/api/health"
check_service "Database" "http://localhost:15432"

# リソース使用量チェック
echo "📊 リソース使用量:"
docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}"
```

## 4. バックアップ・復旧

### 4.1 データベースバックアップ

```bash
#!/bin/bash
# backup_db.sh

BACKUP_DIR="/opt/backups"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="meatmetrics"

# バックアップディレクトリの作成
mkdir -p $BACKUP_DIR

# PostgreSQLバックアップ
docker exec meatmetrics-postgres-prod pg_dump -U meatmetrics $DB_NAME > $BACKUP_DIR/db_backup_$DATE.sql

# 圧縮
gzip $BACKUP_DIR/db_backup_$DATE.sql

# 古いバックアップの削除（30日以上古いもの）
find $BACKUP_DIR -name "db_backup_*.sql.gz" -mtime +30 -delete

echo "バックアップ完了: db_backup_$DATE.sql.gz"
```

### 4.2 ボリュームバックアップ

```bash
#!/bin/bash
# backup_volumes.sh

BACKUP_DIR="/opt/backups/volumes"
DATE=$(date +%Y%m%d_%H%M%S)

mkdir -p $BACKUP_DIR

# PostgreSQLボリュームのバックアップ
docker run --rm -v postgres16_data:/data -v $BACKUP_DIR:/backup alpine tar czf /backup/postgres_data_$DATE.tar.gz -C /data .

# アプリケーションログのバックアップ
docker run --rm -v backend_logs:/data -v $BACKUP_DIR:/backup alpine tar czf /backup/backend_logs_$DATE.tar.gz -C /data .

echo "ボリュームバックアップ完了: $DATE"
```

### 4.3 復旧手順

```bash
#!/bin/bash
# restore_db.sh

BACKUP_FILE=$1
DB_NAME="meatmetrics"

if [ -z "$BACKUP_FILE" ]; then
    echo "使用方法: $0 <バックアップファイル>"
    exit 1
fi

# データベースの復旧
docker exec -i meatmetrics-postgres-prod psql -U meatmetrics $DB_NAME < $BACKUP_FILE

echo "データベース復旧完了: $BACKUP_FILE"
```

## 5. スケーリング・負荷分散

### 5.1 水平スケーリング

```yaml
# docker-compose.prod.yml に追加
services:
  backend:
    deploy:
      replicas: 3
      resources:
        limits:
          memory: 1G
          cpus: "0.5"
        reservations:
          memory: 512M
          cpus: "0.25"
      restart_policy:
        condition: on-failure
        delay: 5s
        max_attempts: 3
        window: 120s

  frontend:
    deploy:
      replicas: 2
      resources:
        limits:
          memory: 512M
          cpus: "0.25"
```

### 5.2 ロードバランサー設定

```nginx
# nginx/nginx.conf (スケーリング対応版)
upstream backend {
    least_conn;  # 最小接続数方式
    server backend:8080 max_fails=3 fail_timeout=30s;
    server backend:8081 max_fails=3 fail_timeout=30s;
    server backend:8082 max_fails=3 fail_timeout=30s;
}

upstream frontend {
    ip_hash;  # セッション保持
    server frontend:3000 max_fails=3 fail_timeout=30s;
    server frontend:3001 max_fails=3 fail_timeout=30s;
}
```

## 6. セキュリティ強化

### 6.1 コンテナセキュリティ

```yaml
# docker-compose.prod.yml に追加
services:
  backend:
    security_opt:
      - no-new-privileges:true
    read_only: true
    tmpfs:
      - /tmp
      - /var/tmp
    user: "1000:1000"

  frontend:
    security_opt:
      - no-new-privileges:true
    read_only: true
    tmpfs:
      - /tmp
      - /var/tmp
    user: "1000:1000"
```

### 6.2 ネットワークセキュリティ

```yaml
# docker-compose.prod.yml に追加
networks:
  meatmetrics-network:
    driver: bridge
    internal: true # 外部からの直接アクセスを制限
    ipam:
      config:
        - subnet: 172.20.0.0/16
          gateway: 172.20.0.1
```

## 7. パフォーマンス最適化

### 7.1 キャッシュ戦略

```nginx
# nginx/nginx.conf に追加
# 静的ファイルのキャッシュ
location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
    expires 1y;
    add_header Cache-Control "public, immutable";
    add_header Vary Accept-Encoding;
}

# APIレスポンスのキャッシュ
location /api/ {
    proxy_cache_valid 200 1m;
    proxy_cache_valid 404 1m;
    add_header X-Cache-Status $upstream_cache_status;
}
```

### 7.2 データベース最適化

```yaml
# docker-compose.prod.yml に追加
services:
  postgres:
    environment:
      POSTGRES_PASSWORD: ${POSTGRES_PASSWORD}
      POSTGRES_USER: meatmetrics
      POSTGRES_DB: meatmetrics
      # パフォーマンス設定
      POSTGRES_SHARED_BUFFERS: 256MB
      POSTGRES_EFFECTIVE_CACHE_SIZE: 1GB
      POSTGRES_WORK_MEM: 4MB
      POSTGRES_MAINTENANCE_WORK_MEM: 64MB
```

## 8. 障害対応・運用

### 8.1 障害検知スクリプト

```bash
#!/bin/bash
# alert_monitor.sh

# アラート設定
WEBHOOK_URL="https://hooks.slack.com/services/YOUR/WEBHOOK/URL"

send_alert() {
    local message="$1"
    curl -X POST -H 'Content-type: application/json' \
        --data "{\"text\":\"🚨 $message\"}" \
        $WEBHOOK_URL
}

# サービス状態チェック
if ! curl -f -s "http://localhost/api/health" > /dev/null; then
    send_alert "バックエンドサービスが停止しています"
fi

if ! curl -f -s "http://localhost" > /dev/null; then
    send_alert "フロントエンドサービスが停止しています"
fi

# リソース使用量チェック
MEMORY_USAGE=$(docker stats --no-stream --format "{{.MemPerc}}" | grep -o '[0-9.]*')
if (( $(echo "$MEMORY_USAGE > 80" | bc -l) )); then
    send_alert "メモリ使用量が80%を超えています: ${MEMORY_USAGE}%"
fi
```

### 8.2 自動復旧スクリプト

```bash
#!/bin/bash
# auto_recovery.sh

# 自動復旧処理
recover_service() {
    local service=$1

    echo "🔄 $service の自動復旧を開始..."

    # コンテナの再起動
    docker-compose -f docker-compose.prod.yml restart $service

    # 復旧確認
    sleep 30
    if docker-compose -f docker-compose.prod.yml ps $service | grep -q "Up"; then
        echo "✅ $service の復旧完了"
    else
        echo "❌ $service の復旧失敗"
        # 管理者に通知
        send_alert "$service の自動復旧に失敗しました"
    fi
}

# 各サービスの状態チェックと復旧
for service in backend frontend nginx; do
    if ! docker-compose -f docker-compose.prod.yml ps $service | grep -q "Up"; then
        recover_service $service
    fi
done
```

## 9. 運用管理ツール

### 9.1 管理ダッシュボード

```yaml
# docker-compose.prod.yml に追加
services:
  portainer:
    image: portainer/portainer-ce:latest
    container_name: meatmetrics-portainer
    ports:
      - "9000:9000"
    volumes:
      - /var/run/docker.sock:/var/run/docker.sock
      - portainer_data:/data
    networks:
      - meatmetrics-network
    restart: unless-stopped

volumes:
  portainer_data:
```

### 9.2 ログ集約

```yaml
# docker-compose.prod.yml に追加
services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.8.0
    container_name: meatmetrics-elasticsearch
    environment:
      - discovery.type=single-node
      - xpack.security.enabled=false
    volumes:
      - elasticsearch_data:/usr/share/elasticsearch/data
    networks:
      - meatmetrics-network

  kibana:
    image: docker.elastic.co/kibana/kibana:8.8.0
    container_name: meatmetrics-kibana
    ports:
      - "5601:5601"
    environment:
      - ELASTICSEARCH_HOSTS=http://elasticsearch:9200
    networks:
      - meatmetrics-network
    depends_on:
      - elasticsearch

volumes:
  elasticsearch_data:
```

---

**最終更新**: 2025 年 8 月 10 日  
**更新者**: 開発チーム  
**次回更新予定**: 環境構築完了後
