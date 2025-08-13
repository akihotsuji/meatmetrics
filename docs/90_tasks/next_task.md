# 翌日タスク計画 📅

## 日付: 2025 年 8 月 14 日（木）

**優先度**: 高  
**予想作業時間**: 2 時間

---

## Phase 1: 基盤構築（優先度：高）

### プロジェクト構造構築

- [ ] バックエンド基盤
  - [ ] 基本的なコントローラー（/api/health, /api/health/db）
  - [ ] 共通エラーハンドリング
  - [ ] CORS（dev）/セキュリティヘッダー設定
- [ ] フロントエンド基盤
  - [ ] React アプリケーションの起動確認
  - [ ] 基本的なルーティング設定
    - [ ] ホームページの作成
    - [ ] ナビゲーションの実装
  - [ ] 基本的な UI コンポーネントの作成

### データベース設計・構築

- [ ] MVP スキーマ差分適用
  - [ ] foods.tags TEXT[] 追加 + GIN index
  - [ ] user_goals に net_carbs_g を追加
  - [ ] meal_records に total_nutrition{calories,protein,fat,net_carbs}

## Phase 2: 認証・ユーザー管理（優先度：高）

- [ ] 認証 API 実装（register/login/logout/refresh）
- [ ] 目標 API（GET/PUT）に net_carbs_g を含める
- [ ] パスワード変更 API（PUT /api/users/password）

## Phase 4: CI/CD（優先度：中）

- [ ] CI パイプライン雛形の作成（backend-test, frontend-test）
- [ ] Secrets の命名方針整理（DB_URL, JWT_SECRET 等）
- [ ] main への push 時 build 実行

## Phase 3: 食材管理（優先度：高）

- [ ] 食材検索 API（q, category, tags）
- [ ] カテゴリ=タグ扱いの包含ロジック
- [ ] 初期データ投入（牛/豚/鶏/卵/乳 各 5-10 件）

---

## 今日の目標 🎯

- [ ] MVP API の骨組み完成（認証/食材/食事記録/サマリーの雛形）
- [ ] DB スキーマの差分反映
- [ ] 初期データの雛形投入

---

## 注意点・前提条件 ⚠️

- PostgreSQL 16 は dev で `15432:5432` 公開、本番は非公開
- Spring Boot は dev で 8080 公開、prod は Nginx 経由
- データベース接続は Compose のサービス名 `postgres` を使用
- `infrastructure/docker` は dev/prod 分離済み（ルート Compose は撤去済み）

---

**最終更新**: 2025 年 8 月 13 日  
**更新者**: 開発チーム
