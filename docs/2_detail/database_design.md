# カーニボア専用栄養成分計算アプリ データベース設計書

## 1. 概要

### 1.1 設計方針

- **ドメイン駆動設計（DDD）**に基づく設計
- **PostgreSQL**を活用した高性能な検索・分析
- **JSONB**を活用した柔軟な栄養データ管理
- **正規化**と**非正規化**のバランスを考慮

### 1.2 データベース構成

- **メインデータベース**: `meatmetrics_prod`
- **開発用データベース**: `meatmetrics_dev`
- **テスト用データベース**: `meatmetrics_test`

## 1.3 MVP 用スキーマ差分

- 方針: 栄養計算はアプリケーション層（Java）で実施。ビュー/ストアドは MVP では未使用
- 目標: 「糖質」を追加（net carbs）。糖質(g) = 炭水化物(g) - 食物繊維(g)
- タグ: `foods` に複数タグを保持できる列を追加し、検索で利用

```sql
-- foods へのタグ列追加（存在しない場合のみ）
ALTER TABLE foods
  ADD COLUMN IF NOT EXISTS tags TEXT[];

CREATE INDEX IF NOT EXISTS idx_foods_tags ON foods USING gin (tags);

-- MVP最小テーブル（概略）
CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS user_goals (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  daily_calorie_goal INTEGER CHECK (daily_calorie_goal BETWEEN 800 AND 5000),
  protein_goal_g DECIMAL(6,2) CHECK (protein_goal_g BETWEEN 50 AND 500),
  fat_goal_g DECIMAL(6,2) CHECK (fat_goal_g BETWEEN 30 AND 400),
  net_carbs_goal_g DECIMAL(6,2) CHECK (net_carbs_goal_g BETWEEN 0 AND 150),
  is_active BOOLEAN DEFAULT true,
  effective_date DATE DEFAULT CURRENT_DATE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE(user_id, effective_date)
);

-- foods は既存構成を利用。MVPでは nutrition_data から
-- calories/protein/fat/carbohydrates/fiber を参照

CREATE TABLE IF NOT EXISTS meal_records (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  meal_date DATE NOT NULL,
  items JSONB NOT NULL, -- [{food_id, amount_g}]
  total_nutrition JSONB NOT NULL, -- {calories, protein, fat, net_carbs}
  notes TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_items_json CHECK (jsonb_typeof(items) = 'array'),
  CONSTRAINT chk_total_nutrition_json CHECK (jsonb_typeof(total_nutrition) = 'object')
);

CREATE INDEX IF NOT EXISTS idx_meal_records_user_date ON meal_records(user_id, meal_date);
```

## 2. テーブル設計

### 2.1 ユーザー管理系テーブル

#### 2.1.1 users（ユーザー基本情報）

```sql
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(20) UNIQUE NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  email_verified BOOLEAN DEFAULT false,
  email_verification_token VARCHAR(255),
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  -- 制約
  CONSTRAINT chk_username_length CHECK (LENGTH(username) >= 3 AND LENGTH(username) <= 20),
  CONSTRAINT chk_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$')
);

-- インデックス
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_created_at ON users(created_at);
```

#### 2.1.2 user_profiles（ユーザープロフィール）

```sql
CREATE TABLE user_profiles (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,

  -- 基本情報
  first_name VARCHAR(50),
  last_name VARCHAR(50),
  date_of_birth DATE,
  gender VARCHAR(20) CHECK (gender IN ('male', 'female', 'other', 'prefer_not_to_say')),

  -- 身体情報
  height_cm DECIMAL(5,2) CHECK (height_cm >= 100 AND height_cm <= 250),
  weight_kg DECIMAL(5,2) CHECK (weight_kg >= 30 AND weight_kg <= 200),
  activity_level VARCHAR(20) CHECK (activity_level IN ('sedentary', 'lightly_active', 'moderately_active', 'very_active', 'extremely_active')),

  -- カーニボア設定
  carnivore_start_date DATE,
  carnivore_level VARCHAR(20) DEFAULT 'beginner' CHECK (carnivore_level IN ('beginner', 'intermediate', 'advanced')),
  carnivore_goal VARCHAR(20) CHECK (carnivore_goal IN ('weight_loss', 'weight_gain', 'maintenance', 'health_improvement')),

  -- 設定
  measurement_unit VARCHAR(10) DEFAULT 'metric' CHECK (measurement_unit IN ('metric', 'imperial')),
  timezone VARCHAR(50) DEFAULT 'UTC',

  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  UNIQUE(user_id)
);

-- インデックス
CREATE INDEX idx_user_profiles_user_id ON user_profiles(user_id);
CREATE INDEX idx_user_profiles_carnivore_level ON user_profiles(carnivore_level);
```

#### 2.1.3 nutrition_goals（栄養目標）

```sql
CREATE TABLE nutrition_goals (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,

  -- カロリー目標
  daily_calorie_goal INTEGER CHECK (daily_calorie_goal >= 800 AND daily_calorie_goal <= 5000),

  -- マクロ栄養素目標（グラム）
  protein_goal_g DECIMAL(6,2) CHECK (protein_goal_g >= 50 AND protein_goal_g <= 500),
  fat_goal_g DECIMAL(6,2) CHECK (fat_goal_g >= 30 AND fat_goal_g <= 400),
  carbohydrate_goal_g DECIMAL(6,2) CHECK (carbohydrate_goal_g >= 0 AND carbohydrate_goal_g <= 100),

  -- ビタミン目標
  vitamin_b12_goal_mcg DECIMAL(6,2) DEFAULT 2.4,
  vitamin_d_goal_iu INTEGER DEFAULT 600,
  vitamin_c_goal_mg INTEGER DEFAULT 90,

  -- ミネラル目標
  iron_goal_mg DECIMAL(6,2) DEFAULT 18.0,
  zinc_goal_mg DECIMAL(6,2) DEFAULT 11.0,
  selenium_goal_mcg DECIMAL(6,2) DEFAULT 55.0,

  -- 設定
  is_active BOOLEAN DEFAULT true,
  effective_date DATE DEFAULT CURRENT_DATE,

  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  UNIQUE(user_id, effective_date)
);

-- インデックス
CREATE INDEX idx_nutrition_goals_user_id ON nutrition_goals(user_id);
CREATE INDEX idx_nutrition_goals_effective_date ON nutrition_goals(effective_date);
```

### 2.2 食材管理系テーブル

#### 2.2.1 food_categories（食材カテゴリ）

```sql
CREATE TABLE food_categories (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE,
  display_name VARCHAR(100) NOT NULL,
  description TEXT,
  parent_id BIGINT REFERENCES food_categories(id),
  sort_order INTEGER DEFAULT 0,
  is_active BOOLEAN DEFAULT true,
  icon_name VARCHAR(50),

  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 初期データ
INSERT INTO food_categories (name, display_name, description, sort_order) VALUES
('meat', '肉類', '牛肉、豚肉、鶏肉、羊肉などの肉類', 1),
('fish', '魚介類', '魚類、貝類、甲殻類などの魚介類', 2),
('eggs', '卵類', '鶏卵、うずら卵などの卵類', 3),
('dairy', '乳製品', 'チーズ、バター、生クリームなどの乳製品', 4),
('seasonings', '調味料・香辛料', '塩、胡椒、ハーブなどの調味料', 5);

-- サブカテゴリ
INSERT INTO food_categories (name, display_name, parent_id, sort_order) VALUES
('beef', '牛肉', (SELECT id FROM food_categories WHERE name = 'meat'), 1),
('pork', '豚肉', (SELECT id FROM food_categories WHERE name = 'meat'), 2),
('chicken', '鶏肉', (SELECT id FROM food_categories WHERE name = 'meat'), 3),
('lamb', '羊肉', (SELECT id FROM food_categories WHERE name = 'meat'), 4),
('salmon', 'サケ類', (SELECT id FROM food_categories WHERE name = 'fish'), 1),
('tuna', 'マグロ類', (SELECT id FROM food_categories WHERE name = 'fish'), 2);
```

#### 2.2.2 foods（食材マスター）

```sql
CREATE TABLE foods (
  id BIGSERIAL PRIMARY KEY,

  -- 基本情報
  name VARCHAR(255) NOT NULL,
  display_name VARCHAR(255) NOT NULL,
  category_id BIGINT NOT NULL REFERENCES food_categories(id),
  subcategory_id BIGINT REFERENCES food_categories(id),

  -- 栄養成分データ（100gあたり）
  nutrition_data JSONB NOT NULL,

  -- 調理方法による栄養成分の変化
  cooking_methods JSONB,

  -- メタデータ
  barcode VARCHAR(50),
  brand VARCHAR(100),
  origin VARCHAR(100),
  seasonality JSONB, -- 旬の情報

  -- アレルギー・制限情報
  allergens JSONB,
  dietary_restrictions JSONB, -- ベジタリアン、ハラルなど

  -- 品質管理
  is_active BOOLEAN DEFAULT true,
  data_source VARCHAR(100), -- データの出典
  last_verified_date DATE,

  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  -- 制約
  CONSTRAINT chk_nutrition_data CHECK (jsonb_typeof(nutrition_data) = 'object'),
  CONSTRAINT chk_cooking_methods CHECK (cooking_methods IS NULL OR jsonb_typeof(cooking_methods) = 'object')
);

-- インデックス
CREATE INDEX idx_foods_category_id ON foods(category_id);
CREATE INDEX idx_foods_name ON foods USING gin(to_tsvector('japanese', name));
CREATE INDEX idx_foods_nutrition_data ON foods USING gin(nutrition_data);
CREATE INDEX idx_foods_cooking_methods ON foods USING gin(cooking_methods);
CREATE INDEX idx_foods_is_active ON foods(is_active);
```

#### 2.2.3 nutrition_data_template（栄養成分テンプレート）

```sql
-- 栄養成分の標準的な構造を定義
CREATE TABLE nutrition_data_template (
  id BIGSERIAL PRIMARY KEY,
  template_name VARCHAR(100) NOT NULL,
  template_structure JSONB NOT NULL,
  description TEXT,
  is_active BOOLEAN DEFAULT true,

  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 標準的な栄養成分テンプレート
INSERT INTO nutrition_data_template (template_name, template_structure, description) VALUES
('standard_100g', '{
  "calories": {"unit": "kcal", "type": "number", "min": 0, "max": 1000},
  "protein": {"unit": "g", "type": "number", "min": 0, "max": 100},
  "fat": {"unit": "g", "type": "number", "min": 0, "max": 100},
  "carbohydrates": {"unit": "g", "type": "number", "min": 0, "max": 100},
  "fiber": {"unit": "g", "type": "number", "min": 0, "max": 50},
  "sugar": {"unit": "g", "type": "number", "min": 0, "max": 100},
  "sodium": {"unit": "mg", "type": "number", "min": 0, "max": 5000},
  "cholesterol": {"unit": "mg", "type": "number", "min": 0, "max": 1000},
  "vitamins": {
    "vitamin_a": {"unit": "mcg", "type": "number", "min": 0, "max": 10000},
    "vitamin_c": {"unit": "mg", "type": "number", "min": 0, "max": 1000},
    "vitamin_d": {"unit": "mcg", "type": "number", "min": 0, "max": 100},
    "vitamin_e": {"unit": "mg", "type": "number", "min": 0, "max": 1000},
    "vitamin_k": {"unit": "mcg", "type": "number", "min": 0, "max": 1000},
    "vitamin_b1": {"unit": "mg", "type": "number", "min": 0, "max": 100},
    "vitamin_b2": {"unit": "mg", "type": "number", "min": 0, "max": 100},
    "vitamin_b3": {"unit": "mg", "type": "number", "min": 0, "max": 1000},
    "vitamin_b6": {"unit": "mg", "type": "number", "min": 0, "max": 100},
    "vitamin_b12": {"unit": "mcg", "type": "number", "min": 0, "max": 100},
    "folate": {"unit": "mcg", "type": "number", "min": 0, "max": 1000}
  },
  "minerals": {
    "calcium": {"unit": "mg", "type": "number", "min": 0, "max": 2000},
    "iron": {"unit": "mg", "type": "number", "min": 0, "max": 100},
    "magnesium": {"unit": "mg", "type": "number", "min": 0, "max": 1000},
    "phosphorus": {"unit": "mg", "type": "number", "min": 0, "max": 2000},
    "potassium": {"unit": "mg", "type": "number", "min": 0, "max": 5000},
    "sodium": {"unit": "mg", "type": "number", "min": 0, "max": 5000},
    "zinc": {"unit": "mg", "type": "number", "min": 0, "max": 100},
    "copper": {"unit": "mg", "type": "number", "min": 0, "max": 100},
    "manganese": {"unit": "mg", "type": "number", "min": 0, "max": 100},
    "selenium": {"unit": "mcg", "type": "number", "min": 0, "max": 1000}
  }
}', '標準的な100gあたりの栄養成分テンプレート');
```

### 2.3 食事記録系テーブル

#### 2.3.1 meal_records（食事記録）

```sql
CREATE TABLE meal_records (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,

  -- 食事情報
  meal_date DATE NOT NULL,
  meal_type VARCHAR(20) NOT NULL CHECK (meal_type IN ('breakfast', 'lunch', 'dinner', 'snack', 'other')),
  meal_time TIME,

  -- 食材と量の情報
  foods JSONB NOT NULL, -- [{food_id, amount_g, cooking_method, notes}]

  -- 計算された栄養成分
  total_nutrition JSONB NOT NULL,

  -- メタデータ
  notes TEXT,
  mood VARCHAR(50), -- 食事後の気分
  energy_level INTEGER CHECK (energy_level >= 1 AND energy_level <= 10),

  -- 画像・添付ファイル
  images JSONB, -- [{url, description}]

  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  -- 制約
  CONSTRAINT chk_foods_structure CHECK (jsonb_typeof(foods) = 'array'),
  CONSTRAINT chk_total_nutrition CHECK (jsonb_typeof(total_nutrition) = 'object')
);

-- インデックス
CREATE INDEX idx_meal_records_user_id ON meal_records(user_id);
CREATE INDEX idx_meal_records_meal_date ON meal_records(meal_date);
CREATE INDEX idx_meal_records_meal_type ON meal_records(meal_type);
CREATE INDEX idx_meal_records_foods ON meal_records USING gin(foods);
CREATE INDEX idx_meal_records_total_nutrition ON meal_records USING gin(total_nutrition);
```

#### 2.3.2 meal_templates（食事テンプレート）

```sql
CREATE TABLE meal_templates (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,

  -- テンプレート情報
  name VARCHAR(100) NOT NULL,
  description TEXT,
  category VARCHAR(50), -- 'favorite', 'weekly', 'custom'

  -- 食材構成
  foods JSONB NOT NULL, -- [{food_id, amount_g, cooking_method, notes}]

  -- 栄養成分（事前計算）
  estimated_nutrition JSONB,

  -- 使用頻度
  usage_count INTEGER DEFAULT 0,
  last_used_date DATE,

  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  -- 制約
  CONSTRAINT chk_foods_structure CHECK (jsonb_typeof(foods) = 'array')
);

-- インデックス
CREATE INDEX idx_meal_templates_user_id ON meal_templates(user_id);
CREATE INDEX idx_meal_templates_category ON meal_templates(category);
CREATE INDEX idx_meal_templates_foods ON meal_templates USING gin(foods);
```

### 2.4 分析・レポート系テーブル

#### 2.4.1 nutrition_analytics（栄養分析結果）

```sql
CREATE TABLE nutrition_analytics (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,

  -- 分析対象期間
  analysis_date DATE NOT NULL,
  period_type VARCHAR(20) NOT NULL CHECK (period_type IN ('daily', 'weekly', 'monthly')),

  -- 分析結果
  nutrition_summary JSONB NOT NULL,
  goal_achievement JSONB NOT NULL,
  carnivore_score DECIMAL(5,2),
  recommendations JSONB,

  -- メタデータ
  analysis_version VARCHAR(20) DEFAULT '1.0',

  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  -- 制約
  CONSTRAINT chk_nutrition_summary CHECK (jsonb_typeof(nutrition_summary) = 'object'),
  CONSTRAINT chk_goal_achievement CHECK (jsonb_typeof(goal_achievement) = 'object'),
  CONSTRAINT chk_carnivore_score CHECK (carnivore_score >= 0 AND carnivore_score <= 100),

  UNIQUE(user_id, analysis_date, period_type)
);

-- インデックス
CREATE INDEX idx_nutrition_analytics_user_id ON nutrition_analytics(user_id);
CREATE INDEX idx_nutrition_analytics_date ON nutrition_analytics(analysis_date);
CREATE INDEX idx_nutrition_analytics_period_type ON nutrition_analytics(period_type);
CREATE INDEX idx_nutrition_analytics_carnivore_score ON nutrition_analytics(carnivore_score);
```

#### 2.4.2 progress_tracking（進捗追跡）

```sql
CREATE TABLE progress_tracking (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,

  -- 追跡日
  tracking_date DATE NOT NULL,

  -- 身体測定
  weight_kg DECIMAL(5,2),
  body_fat_percentage DECIMAL(4,2),
  muscle_mass_kg DECIMAL(5,2),

  -- 健康指標
  energy_level INTEGER CHECK (energy_level >= 1 AND energy_level <= 10),
  sleep_quality INTEGER CHECK (sleep_quality >= 1 AND sleep_quality <= 10),
  stress_level INTEGER CHECK (stress_level >= 1 AND stress_level <= 10),

  -- カーニボア関連
  carnivore_compliance_score DECIMAL(5,2) CHECK (carnivore_compliance_score >= 0 AND carnivore_compliance_score <= 100),
  symptoms JSONB, -- 体調の変化、症状など

  -- メモ
  notes TEXT,

  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  UNIQUE(user_id, tracking_date)
);

-- インデックス
CREATE INDEX idx_progress_tracking_user_id ON progress_tracking(user_id);
CREATE INDEX idx_progress_tracking_date ON progress_tracking(tracking_date);
CREATE INDEX idx_progress_tracking_weight ON progress_tracking(weight_kg);
CREATE INDEX idx_progress_tracking_carnivore_score ON progress_tracking(carnivore_compliance_score);
```

## 3. ビュー設計

### 3.1 栄養分析ビュー

```sql
CREATE VIEW nutrition_analysis_view AS
SELECT
  mr.user_id,
  mr.meal_date,
  mr.meal_type,
  jsonb_agg(
    jsonb_build_object(
      'food_name', f.display_name,
      'amount_g', (food_item->>'amount_g')::DECIMAL,
      'cooking_method', food_item->>'cooking_method',
      'calories', (f.nutrition_data->>'calories')::DECIMAL * (food_item->>'amount_g')::DECIMAL / 100,
      'protein', (f.nutrition_data->>'protein')::DECIMAL * (food_item->>'amount_g')::DECIMAL / 100,
      'fat', (f.nutrition_data->>'fat')::DECIMAL * (food_item->>'amount_g')::DECIMAL / 100
    )
  ) as food_details,
  mr.total_nutrition,
  mr.notes
FROM meal_records mr
CROSS JOIN LATERAL jsonb_array_elements(mr.foods) as food_item
JOIN foods f ON (food_item->>'food_id')::BIGINT = f.id
WHERE mr.is_active = true
GROUP BY mr.id, mr.user_id, mr.meal_date, mr.meal_type, mr.total_nutrition, mr.notes;
```

### 3.2 ユーザー進捗ビュー

```sql
CREATE VIEW user_progress_view AS
SELECT
  u.id as user_id,
  u.username,
  up.carnivore_level,
  up.carnivore_goal,
  ng.daily_calorie_goal,
  ng.protein_goal_g,
  ng.fat_goal_g,
  pt.weight_kg,
  pt.carnivore_compliance_score,
  pt.energy_level,
  pt.tracking_date
FROM users u
JOIN user_profiles up ON u.id = up.user_id
LEFT JOIN nutrition_goals ng ON u.id = ng.user_id AND ng.is_active = true
LEFT JOIN progress_tracking pt ON u.id = pt.user_id
WHERE u.is_active = true;
```

## 4. ストアドプロシージャ

### 4.1 栄養計算プロシージャ

```sql
CREATE OR REPLACE FUNCTION calculate_meal_nutrition(
  p_foods JSONB
) RETURNS JSONB AS $$
DECLARE
  v_total_nutrition JSONB := '{}';
  v_food_item JSONB;
  v_food_id BIGINT;
  v_amount_g DECIMAL;
  v_food_nutrition JSONB;
  v_cooking_method VARCHAR(50);
BEGIN
  -- 各食材の栄養成分を計算
  FOR v_food_item IN SELECT * FROM jsonb_array_elements(p_foods)
  LOOP
    v_food_id := (v_food_item->>'food_id')::BIGINT;
    v_amount_g := (v_food_item->>'amount_g')::DECIMAL;
    v_cooking_method := COALESCE(v_food_item->>'cooking_method', 'raw');

    -- 食材の栄養成分を取得
    SELECT nutrition_data INTO v_food_nutrition
    FROM foods
    WHERE id = v_food_id AND is_active = true;

    -- 調理方法による栄養成分の調整
    IF v_cooking_method != 'raw' AND v_food_nutrition->'cooking_methods' ? v_cooking_method THEN
      v_food_nutrition := v_food_nutrition->'cooking_methods'->v_cooking_method;
    END IF;

    -- 量に応じて栄養成分を計算
    v_total_nutrition := v_total_nutrition || jsonb_build_object(
      'calories', COALESCE((v_total_nutrition->>'calories')::DECIMAL, 0) +
                 (v_food_nutrition->>'calories')::DECIMAL * v_amount_g / 100,
      'protein', COALESCE((v_total_nutrition->>'protein')::DECIMAL, 0) +
                (v_food_nutrition->>'protein')::DECIMAL * v_amount_g / 100,
      'fat', COALESCE((v_total_nutrition->>'fat')::DECIMAL, 0) +
            (v_food_nutrition->>'fat')::DECIMAL * v_amount_g / 100
    );
  END LOOP;

  RETURN v_total_nutrition;
END;
$$ LANGUAGE plpgsql;
```

### 4.2 カーニボアスコア計算プロシージャ

```sql
CREATE OR REPLACE FUNCTION calculate_carnivore_score(
  p_user_id BIGINT,
  p_analysis_date DATE
) RETURNS DECIMAL AS $$
DECLARE
  v_score DECIMAL := 0;
  v_meat_ratio DECIMAL;
  v_protein_quality DECIMAL;
  v_fat_balance DECIMAL;
  v_carb_restriction DECIMAL;
BEGIN
  -- 肉類の割合を計算
  SELECT
    COALESCE(
      SUM(CASE WHEN fc.name IN ('meat', 'fish', 'eggs') THEN 1 ELSE 0 END)::DECIMAL /
      COUNT(*)::DECIMAL * 100, 0
    ) INTO v_meat_ratio
  FROM meal_records mr
  CROSS JOIN LATERAL jsonb_array_elements(mr.foods) as food_item
  JOIN foods f ON (food_item->>'food_id')::BIGINT = f.id
  JOIN food_categories fc ON f.category_id = fc.id
  WHERE mr.user_id = p_user_id AND mr.meal_date = p_analysis_date;

  -- タンパク質品質を計算
  -- 必須アミノ酸のバランスなどを考慮

  -- 脂質バランスを計算
  -- オメガ3/オメガ6の比率などを考慮

  -- 炭水化物制限の評価
  -- 50g以下を目標とする

  -- 総合スコアの計算
  v_score := (v_meat_ratio * 0.4 + v_protein_quality * 0.3 +
              v_fat_balance * 0.2 + v_carb_restriction * 0.1);

  RETURN LEAST(GREATEST(v_score, 0), 100);
END;
$$ LANGUAGE plpgsql;
```

## 5. データ移行・初期データ

### 5.1 食材マスターデータの初期化

```sql
-- 牛肉のサンプルデータ
INSERT INTO foods (name, display_name, category_id, nutrition_data) VALUES
('beef_sirloin_lean', 'サーロイン（赤身）',
 (SELECT id FROM food_categories WHERE name = 'beef'),
 '{
   "calories": 250,
   "protein": 26.0,
   "fat": 15.0,
   "carbohydrates": 0.0,
   "vitamins": {
     "vitamin_b12": 2.1,
     "vitamin_b6": 0.5,
     "niacin": 6.0
   },
   "minerals": {
     "iron": 2.5,
     "zinc": 4.0,
     "selenium": 25.0
   }
 }'),
('beef_ribeye', 'リブロース',
 (SELECT id FROM food_categories WHERE name = 'beef'),
 '{
   "calories": 320,
   "protein": 25.0,
   "fat": 25.0,
   "carbohydrates": 0.0,
   "vitamins": {
     "vitamin_b12": 2.0,
     "vitamin_b6": 0.4,
     "niacin": 5.5
   },
   "minerals": {
     "iron": 2.3,
     "zinc": 3.8,
     "selenium": 23.0
   }
 }');
```

## 6. パフォーマンス最適化

### 6.1 パーティショニング戦略

```sql
-- 食事記録テーブルの月別パーティショニング
CREATE TABLE meal_records_2024_01 PARTITION OF meal_records
FOR VALUES FROM ('2024-01-01') TO ('2024-02-01');

CREATE TABLE meal_records_2024_02 PARTITION OF meal_records
FOR VALUES FROM ('2024-02-01') TO ('2024-03-01');
```

### 6.2 統計情報の更新

```sql
-- 定期的な統計情報の更新
ANALYZE users;
ANALYZE foods;
ANALYZE meal_records;
ANALYZE nutrition_analytics;
```

## 7. バックアップ・復旧戦略

### 7.1 バックアップ設定

```sql
-- 日次フルバックアップ
-- 1時間ごとのWALアーカイブ
-- 7日間の保持期間
```

### 7.2 復旧手順

```sql
-- ポイントインタイムリカバリ（PITR）
-- 特定の日時までの復旧
-- データ整合性の確認
```

---

**作成日**: 2024 年 12 月
**作成者**: 開発チーム
**最終更新**: 2024 年 12 月
