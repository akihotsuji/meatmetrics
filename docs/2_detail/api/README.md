# API 設計ドキュメント（MVP）

本フォルダは API ごとの個別設計書を格納する。フロントエンド/バックエンドの両方が、このドキュメントだけで実装・疎通確認ができることを目的とする。

## ガイドライン

- ベースパス: `/api`
- 認証: `Authorization: Bearer <JWT>`（/auth/\* を除く）
- フォーマット: JSON（snake_case）
- 日付: `YYYY-MM-DD`
- 数値: 小数は `number`（TS）/ `BigDecimal`（Java）
- エラー: 共通エラーモデル

```json
{
  "code": "VALIDATION_ERROR|UNAUTHORIZED|NOT_FOUND|CONFLICT|INTERNAL_ERROR",
  "message": "...",
  "details": {}
}
```

## 各ドキュメントの構成

- 概要 / エンドポイント
- 認証要件
- Request/Response スキーマ（検証ルール・境界値）
- エラーコード
- 例（curl / TypeScript fetch / Java Controller スケッチ）
- 実装メモ（キャッシュ/注意点 など）

## 一覧（MVP）

- `auth.md`: ユーザー登録/ログイン/ログアウト/トークン更新
- `foods.md`: 食材検索・詳細
- `meals.md`: 食事記録 CRUD
- `summary.md`: 日別サマリー
- `users.md`: 目標取得/更新・パスワード変更

---

参照: `../03_api.md`, `../01_system_architecture.md`
