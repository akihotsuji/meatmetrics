# Backend Tests

このディレクトリ配下のテストは、PostgreSQL + Flyway を前提にしています。H2 は使用しません。

## 実行モード

- デフォルト: Testcontainers で PostgreSQL コンテナを起動して実行（本番同等）
- 既存 DB 直結: docker-compose の `postgres` サービスに接続して実行（高速・安定）

環境変数 `TEST_DB_MODE` で切替できます。

```bash
# Testcontainers（デフォルト）
docker compose exec backend mvn test

# 既存DB直結（docker-compose の postgres を使用）
docker compose exec backend bash -lc 'TEST_DB_MODE=compose mvn test'
```

## 事前準備

```bash
cd infrastructure/docker/dev
docker compose up -d  # postgres / backend / frontend を起動
```

## よく使うコマンド

```bash
# 単体テスト（DB不要）
docker compose exec backend mvn test -Dtest="JwtTokenServiceTest"

# サービス統合テストのみ
docker compose exec backend mvn test -Dtest="*ServiceTest"

# 既存DB直結でサービス統合テスト
docker compose exec backend bash -lc 'TEST_DB_MODE=compose mvn test -Dtest="*ServiceTest"'
```

## 注意

- CI/CD では Testcontainers を使用し、PostgreSQL コンテナを起動して実行します。
- ローカル開発では `TEST_DB_MODE=compose` により Docker-in-Docker 設定なしで高速に回せます。
- Flyway マイグレーションはテスト起動時に自動適用されます。
