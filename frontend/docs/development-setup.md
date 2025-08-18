# 開発環境セットアップガイド

## 概要

このドキュメントでは、MeatMetrics フロントエンド開発環境のセットアップ手順を説明します。

## 前提条件

- Docker および Docker Compose がインストールされていること
- Node.js 18+ がインストールされていること（開発用）
- Git がインストールされていること

## セットアップ手順

### 1. リポジトリのクローン

```bash
git clone <repository-url>
cd meatmetrics/frontend
```

### 2. 環境変数の設定

開発環境用の環境変数ファイルをコピーして設定：

```bash
cp .env.development .env
```

必要に応じて `.env` ファイルを編集してください。

### 3. 依存関係のインストール

```bash
npm install
```

### 4. 開発サーバーの起動（Docker使用）

```bash
# プロジェクトルートから
docker-compose -f infrastructure/docker/dev/docker-compose.yml up frontend
```

### 5. 開発サーバーの起動（ローカル）

```bash
npm run dev
```

## 開発ツール

### コード品質管理

#### ESLint
```bash
# リント実行
npm run lint

# 自動修正
npm run lint:fix
```

#### Prettier
```bash
# フォーマット確認
npm run format:check

# フォーマット実行
npm run format
```

#### TypeScript型チェック
```bash
# 型チェック実行
npm run type-check
```

### 利用可能なスクリプト

- `npm run dev` - 開発サーバー起動
- `npm run build` - プロダクションビルド
- `npm run preview` - ビルド結果のプレビュー
- `npm run lint` - ESLintによる静的解析
- `npm run lint:fix` - ESLint自動修正
- `npm run format` - Prettierによるコードフォーマット
- `npm run format:check` - フォーマットチェック
- `npm run type-check` - TypeScript型チェック

## 設定ファイル

### ESLint（`.eslint.config.js`）
- TypeScript対応
- React Hooks対応
- DDD命名規則対応
- Import順序管理

### Prettier（`.prettierrc`）
- Tailwind CSS対応
- 一貫したコードスタイル
- 自動フォーマット

### TypeScript（`tsconfig.json`）
- 厳格な型チェック
- パスマッピング（`@/*`）
- 最新ECMAScript対応

## 型定義

プロジェクトでは以下の型定義ファイルを使用：

### API型（`src/shared/types/api.ts`）
- RESTエンドポイント用型定義
- 設計書に基づくスキーマ

### ドメイン型（`src/shared/types/domain.ts`）
- DDDアーキテクチャに基づく型定義
- エンティティ・値オブジェクト・集約

### UI型（`src/shared/types/ui.ts`）
- コンポーネント用型定義
- フォーム・テーブル・モーダル等

### 環境変数型（`src/shared/types/env.ts`）
- 環境変数の型安全な利用
- 機能フラグ管理

## 環境変数

### 開発環境（`.env.development`）
- ローカル開発用設定
- デバッグモード有効
- 詳細ログ出力

### 本番環境（`.env.production`）
- 本番環境用設定
- 最適化済み設定
- エラーログのみ

### 機能フラグ

MVP期間中の機能フラグ：
- `VITE_FEATURE_FOOD_CATALOG=true` - 食材カタログ
- `VITE_FEATURE_MEAL_TRACKING=true` - 食事記録
- `VITE_FEATURE_DAILY_SUMMARY=true` - 日別サマリー  
- `VITE_FEATURE_GOAL_SETTING=true` - 目標設定
- `VITE_FEATURE_DATA_ANALYSIS=false` - データ分析（拡張で追加）

## トラブルシューティング

### 型エラーが発生する場合
```bash
# 型定義の再生成
npm run type-check

# node_modules の再インストール
rm -rf node_modules package-lock.json
npm install
```

### ESLintエラーが発生する場合
```bash
# 自動修正を試行
npm run lint:fix

# 設定ファイルの確認
npx eslint --print-config src/App.tsx
```

### Prettierとの競合が発生する場合
```bash
# Prettierでフォーマット後にESLint実行
npm run format
npm run lint:fix
```

## 開発ワークフロー

1. 機能ブランチの作成
2. 環境変数の確認
3. 型定義の確認・更新
4. 開発・テスト
5. コード品質チェック（`npm run lint` / `npm run format:check`）
6. 型チェック（`npm run type-check`）
7. プルリクエスト作成

## 参考資料

- [設計書](../../docs/2_detail/)
- [API仕様](../../docs/2_detail/api/)
- [DDD実装ルール](../../.cursor/rules/ddd.mdc)
- [技術スタック](../../technologystack.md)
