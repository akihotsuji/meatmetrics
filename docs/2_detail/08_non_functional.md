# 08. Non-Functional Requirements（MVP）

## 1. パフォーマンス

- 食材検索: 1 秒以内（LIKE + tags GIN index）
- サマリー取得: 1 秒以内（当日分の合計）

## 2. 可用性/運用

- 開発: Docker Compose、PostgreSQL は永続ボリューム
- 本番: Nginx + Spring Boot + PostgreSQL（最小構成）
- 監視/バックアップは拡張

## 3. セキュリティ

- JWT 認証、強パスワードハッシュ
- CORS/ヘッダー設定
- 入力バリデーションと共通エラー

## 4. ログ/監査（拡張）

- 監査ログ、API アクセスログ
- アラート/レートリミット

## 5. 国際化/アクセシビリティ（拡張）

- i18n フレームワークの導入検討
- キーボード操作、配色コントラスト

---

参照: `01_system_architecture.md`, `04_security.md`
