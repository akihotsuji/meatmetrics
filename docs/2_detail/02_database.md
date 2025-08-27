# 02. Database Design（MVP）

MVP で必要な最小スキーマと方針。詳細は `database_design.md` を参照。本書は MVP 差分にフォーカスする。

## 1. 方針

- 計算はアプリ層で実行、DB トリガーでキャッシュ更新
- 正規化アプローチを採用（meals + meal_items）
- 栄養集計はキャッシュとして保存、トリガーで自動更新
- 検索性のため foods.tags TEXT[] + GIN を採用

## 2. 主要テーブル（要約）

```sql
-- users（実装版: 基本情報のみ）
CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  username VARCHAR(100) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- user_goals（実装版: 栄養目標を分離）
CREATE TABLE IF NOT EXISTS user_goals (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  daily_calorie_goal INTEGER CHECK (daily_calorie_goal BETWEEN 800 AND 5000),
  protein_goal_g DECIMAL(6,2) CHECK (protein_goal_g BETWEEN 50 AND 500),
  fat_goal_g DECIMAL(6,2) CHECK (fat_goal_g BETWEEN 30 AND 400),
  net_carbs_goal_g DECIMAL(6,2) CHECK (net_carbs_goal_g BETWEEN 0 AND 150),
  effective_date DATE DEFAULT CURRENT_DATE,
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(user_id, effective_date)
);

-- foods（実装版: 栄養成分とタグ）
CREATE TABLE IF NOT EXISTS foods (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  category_id BIGINT, -- categories.idへの外部キー
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  -- 栄養成分（100gあたり）
  calories_per_100g INTEGER NOT NULL,
  protein_g_per_100g DECIMAL(6,2) NOT NULL,
  fat_g_per_100g DECIMAL(6,2) NOT NULL,
  carbohydrates_g_per_100g DECIMAL(6,2) NOT NULL,
  fiber_g_per_100g DECIMAL(6,2) DEFAULT 0.00,
  -- タグ管理
  tags TEXT[] DEFAULT '{}',
  description TEXT,
  is_active BOOLEAN DEFAULT true
);
CREATE INDEX IF NOT EXISTS idx_foods_tags ON foods USING gin(tags);

-- categories（実装版: 階層構造）
CREATE TABLE IF NOT EXISTS categories (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  parent_id BIGINT REFERENCES categories(id),
  level INTEGER NOT NULL DEFAULT 1,
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  display_name VARCHAR(100) NOT NULL,
  description TEXT,
  sort_order INTEGER DEFAULT 0,
  is_active BOOLEAN DEFAULT true,
  icon_name VARCHAR(50)
);

-- meals（実装版: 正規化アプローチ）
CREATE TABLE IF NOT EXISTS meals (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  meal_date DATE NOT NULL,
  meal_type VARCHAR(20) NOT NULL, -- BREAKFAST, LUNCH, DINNER, SNACK
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  -- 栄養集計（キャッシュ値、トリガーで自動更新）
  total_calories INTEGER DEFAULT 0,
  total_protein_g DECIMAL(8,2) DEFAULT 0.00,
  total_fat_g DECIMAL(8,2) DEFAULT 0.00,
  total_net_carbs_g DECIMAL(8,2) DEFAULT 0.00,
  notes TEXT,
  is_deleted BOOLEAN DEFAULT false,
  UNIQUE(user_id, meal_date, meal_type)
);

-- meal_items（実装版: 食事詳細の正規化）
CREATE TABLE IF NOT EXISTS meal_items (
  id BIGSERIAL PRIMARY KEY,
  meal_id BIGINT NOT NULL REFERENCES meals(id) ON DELETE CASCADE,
  food_id BIGINT NOT NULL REFERENCES foods(id) ON DELETE RESTRICT,
  quantity_g DECIMAL(8,2) NOT NULL, -- 摂取量（グラム）
  created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  -- 栄養成分キャッシュ（トリガーで自動計算）
  item_calories INTEGER DEFAULT 0,
  item_protein_g DECIMAL(8,2) DEFAULT 0.00,
  item_fat_g DECIMAL(8,2) DEFAULT 0.00,
  item_net_carbs_g DECIMAL(8,2) DEFAULT 0.00,
  notes TEXT,
  is_deleted BOOLEAN DEFAULT false,
  UNIQUE(meal_id, food_id)
);

-- 主要インデックス
CREATE INDEX IF NOT EXISTS idx_meals_user_date_type ON meals(user_id, meal_date, meal_type);
CREATE INDEX IF NOT EXISTS idx_meal_items_meal_id ON meal_items(meal_id) WHERE is_deleted = false;
CREATE INDEX IF NOT EXISTS idx_foods_category_id ON foods(category_id);
CREATE INDEX IF NOT EXISTS idx_categories_parent_id ON categories(parent_id);
```

## 3. 栄養計算ロジック（DB 観点）

- **自動計算トリガー**: meal_items INSERT/UPDATE 時に食材 × 摂取量から栄養成分を自動計算
- **集計更新トリガー**: meal*items 変更時に meals.total*\*を自動更新
- **net_carbs 計算**: carbohydrates - fiber（トリガー内で実行）
- **キャッシュ戦略**: 計算結果を DB に保存、整合性チェック機能付き

### トリガー機能

```sql
-- 栄養成分自動計算（meal_items）
item_calories := (food.calories_per_100g * quantity_g / 100.0)
item_protein_g := (food.protein_g_per_100g * quantity_g / 100.0)
item_net_carbs_g := ((food.carbohydrates_g_per_100g - food.fiber_g_per_100g) * quantity_g / 100.0)

-- 食事合計の自動更新（meals）
total_calories := SUM(meal_items.item_calories)
total_protein_g := SUM(meal_items.item_protein_g)
total_net_carbs_g := SUM(meal_items.item_net_carbs_g)
```

## 4. データ投入（初期）

### 実装済み初期データ

**カテゴリデータ（V005-V006）**

- 3 階層のカテゴリ構造（肉類 → 牛肉 → ステーキ用など）
- 肉類、魚介類、卵類、乳製品、調味料、内臓の主要カテゴリ
- 各カテゴリに適切な表示名、アイコン、ソート順序

**食材データ（V003-V004）**

- カーニボア向け食材約 38 品目
- 牛肉（サーロイン、ヒレ、リブロース等）、豚肉、鶏肉
- 魚類（サケ、マグロ、サバ等）、卵・乳製品
- 正確な栄養成分データ（文部科学省標準栄養成分表ベース）
- タグ: 肉類、高タンパク、低糖質、乳製品等

**ユーザーデータ（V001-V002）**

- 3 つのテストユーザー（カーニボア向け目標設定）
- パスワード: `password123`（全ユーザー共通）

**食事記録データ（V010）**

- user_id=1 の過去 1 週間分の詳細な食事記録
- 朝昼夕＋間食の実際的なカーニボア食事パターン
- 食事詳細（meal_items）のサンプルデータ

## 5. 将来拡張

### データ構造の拡張

- タグの正規化（tags, food_tags, food_tag_assignments）
- 週/月集計テーブルの導入
- 栄養詳細（ビタミン/ミネラル）の再正規化
- user_goals テーブルの分離（時系列での目標変更履歴）

### パフォーマンス最適化

- 大量データ向けパーティショニング（meal_date 基準）
- 集計処理の並列化
- インデックス最適化（使用パターン分析後）

### 機能拡張

- 食事テンプレート機能
- 食材の栄養成分変更履歴
- バルク操作最適化（複数食材同時登録）

---

参照: `database_design.md`, `01_system_architecture.md`, `03_api.md`
