#!/bin/bash

# MeatMetrics 本番環境デプロイスクリプト
# 使用方法: ./scripts/deploy/prod-deploy.sh

set -e  # エラー時に停止

echo "🚀 MeatMetrics 本番環境デプロイを開始します..."

# 1. .envファイルの存在確認
if [ ! -f ".env" ]; then
    echo "❌ .envファイルが見つかりません"
    echo "📝 .env.exampleをコピーして.envファイルを作成してください:"
    echo "   cp .env.example .env"
    echo "   vim .env  # 本番環境用の値を設定"
    exit 1
fi

# 2. 必須環境変数のチェック
echo "🔍 必須環境変数をチェック中..."
source .env

if [ -z "$JWT_SECRET_KEY" ]; then
    echo "❌ JWT_SECRET_KEY が設定されていません"
    echo "💡 以下のコマンドで安全な鍵を生成できます:"
    echo "   openssl rand -base64 32"
    exit 1
fi

if [ ${#JWT_SECRET_KEY} -lt 32 ]; then
    echo "⚠️  警告: JWT_SECRET_KEYが短すぎます（最低32文字推奨）"
fi

echo "✅ 環境変数チェック完了"

# 3. Dockerイメージのビルド
echo "🔨 Dockerイメージをビルド中..."
docker-compose -f infrastructure/docker/prod/docker-compose.yml build --no-cache

# 4. データベースのバックアップ（既存環境の場合）
if docker ps | grep -q "meatmetrics-postgres"; then
    echo "💾 データベースをバックアップ中..."
    BACKUP_FILE="backup_$(date +%Y%m%d_%H%M%S).sql"
    docker exec meatmetrics-postgres pg_dump -U ${POSTGRES_USER:-meatmetrics} ${POSTGRES_DB:-meatmetrics} > "backups/$BACKUP_FILE"
    echo "✅ バックアップ完了: backups/$BACKUP_FILE"
fi

# 5. アプリケーションのデプロイ
echo "🚀 アプリケーションをデプロイ中..."
docker-compose -f infrastructure/docker/prod/docker-compose.yml up -d

# 6. ヘルスチェック
echo "🔍 ヘルスチェック中..."
sleep 30  # アプリケーション起動待機

HEALTH_URL="http://localhost/api/health"
RETRY_COUNT=0
MAX_RETRIES=30

while [ $RETRY_COUNT -lt $MAX_RETRIES ]; do
    if curl -f $HEALTH_URL > /dev/null 2>&1; then
        echo "✅ アプリケーションが正常に起動しました"
        break
    else
        echo "⏳ アプリケーション起動中... ($((RETRY_COUNT + 1))/$MAX_RETRIES)"
        sleep 10
        RETRY_COUNT=$((RETRY_COUNT + 1))
    fi
done

if [ $RETRY_COUNT -eq $MAX_RETRIES ]; then
    echo "❌ アプリケーションの起動に失敗しました"
    echo "📝 ログを確認してください:"
    echo "   docker-compose -f infrastructure/docker/prod/docker-compose.yml logs"
    exit 1
fi

echo "🎉 デプロイ完了!"
echo "🌐 アプリケーション: http://localhost"
echo "📊 ヘルスチェック: http://localhost/api/health"

# 7. 後片付け
echo "🧹 未使用のDockerイメージを削除中..."
docker image prune -f

echo "✅ 全ての処理が完了しました"
