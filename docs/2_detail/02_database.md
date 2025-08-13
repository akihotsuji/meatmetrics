# 02. Database Design（MVP）

MVP で必要な最小スキーマと方針。詳細は `database_design.md` を参照。本書は MVP 差分にフォーカスする。

## 1. 方針

- 計算はアプリ層で実行（DB 関数/ビューは未使用）
- 柔軟性のため JSONB を活用、将来正規化も可能
- 検索性のため foods.tags TEXT[] + GIN を採用

## 2. 主要テーブル（要約）

```sql
-- users（要約）
CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- user_goals（MVP: net_carbs 対応）
CREATE TABLE IF NOT EXISTS user_goals (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  daily_calorie_goal INTEGER,
  protein_goal_g DECIMAL(6,2),
  fat_goal_g DECIMAL(6,2),
  net_carbs_goal_g DECIMAL(6,2),
  is_active BOOLEAN DEFAULT true,
  effective_date DATE DEFAULT CURRENT_DATE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(user_id, effective_date)
);

-- foods（MVP差分: tags）
ALTER TABLE foods ADD COLUMN IF NOT EXISTS tags TEXT[];
CREATE INDEX IF NOT EXISTS idx_foods_tags ON foods USING gin(tags);

-- meal_records（MVP: 合計栄養4種）
CREATE TABLE IF NOT EXISTS meal_records (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  meal_date DATE NOT NULL,
  items JSONB NOT NULL, -- [{food_id, amount_g}]
  total_nutrition JSONB NOT NULL, -- {calories, protein, fat, net_carbs}
  notes TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_meal_records_user_date ON meal_records(user_id, meal_date);
```

## 3. 栄養計算ロジック（DB 観点）

- net_carbs = carbohydrates - fiber（アプリ層で算出）
- total_nutrition は冪等な再計算が可能な構造に（食品更新時の再集計を許容）

## 4. データ投入（初期）

- 牛/豚/鶏/卵/乳 各 5〜10 件
- タグ例: 乳製品, 低糖質, 低価格, 高タンパク 等

## 5. 将来拡張

- タグの正規化（tags, food_tags, food_tag_assignments）
- 週/月集計テーブルの導入
- 栄養詳細（ビタミン/ミネラル）の再正規化

---

参照: `database_design.md`, `01_system_architecture.md`, `03_api.md`
