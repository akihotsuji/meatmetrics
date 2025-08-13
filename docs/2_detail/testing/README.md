# テストの進め方（MVP）

## 目的

- 最小の工数で品質を担保し、拡張に備える

## レイヤ別の実施内容

- ユニット: ドメイン計算（net_carbs、合計、達成率）
- API/統合: 認証、foods 検索、meals 作成/更新、summary、goals
- E2E: 後日導入

## タイミング

- 新機能追加時: 先に失敗するテスト → 実装 → リファクタ
- バグ修正時: 再現テストを先に追加 → 修正
- CI: PR 時にユニット/統合を実行

## 推奨ツール

- Backend: JUnit 5, Spring Boot Test, Testcontainers（PostgreSQL）
- Frontend: Vitest, Testing Library, MSW（API モック）

## データ/フィクスチャ

- サンプル食品（牛/豚/鶏/卵/乳 各 5〜10 件）
- タグ例: 乳製品/低糖質/低価格

参照: docs/2_detail/09_test_strategy.md
