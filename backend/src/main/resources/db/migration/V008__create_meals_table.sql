-- 食事記録テーブル（meals）の作成
-- ドメインモデル MealRecord に対応
-- DDD設計書 docs/2_detail/06_domain_model.md に準拠
-- タスク要件：食事記録の基本情報と栄養集計を管理

CREATE TABLE IF NOT EXISTS meals (
    -- 基本情報（タスク要件準拠）
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    meal_date DATE NOT NULL,
    meal_type VARCHAR(20) NOT NULL, -- BREAKFAST, LUNCH, DINNER, SNACK
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- 栄養集計（キャッシュとして保存、アプリケーション層で算出）
    total_calories INTEGER DEFAULT 0,
    total_protein_g DECIMAL(8,2) DEFAULT 0.00,
    total_fat_g DECIMAL(8,2) DEFAULT 0.00,
    total_net_carbs_g DECIMAL(8,2) DEFAULT 0.00,
    
    -- メタデータ
    notes TEXT,
    is_deleted BOOLEAN DEFAULT false,
    
    -- 外部キー制約
    CONSTRAINT fk_meals_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    
    -- バリデーション制約
    CONSTRAINT meals_meal_type_valid CHECK (meal_type IN ('BREAKFAST', 'LUNCH', 'DINNER', 'SNACK')),
    CONSTRAINT meals_meal_date_valid CHECK (meal_date >= '2020-01-01' AND meal_date <= CURRENT_DATE + INTERVAL '7 days'),
    CONSTRAINT meals_total_calories_positive CHECK (total_calories >= 0 AND total_calories <= 20000),
    CONSTRAINT meals_total_protein_positive CHECK (total_protein_g >= 0 AND total_protein_g <= 2000),
    CONSTRAINT meals_total_fat_positive CHECK (total_fat_g >= 0 AND total_fat_g <= 2000),
    CONSTRAINT meals_total_net_carbs_positive CHECK (total_net_carbs_g >= 0 AND total_net_carbs_g <= 2000),
    
    -- 複合ユニーク制約（同一ユーザー・同一日・同一食事タイプは1つまで）
    CONSTRAINT unique_user_meal_date_type UNIQUE (user_id, meal_date, meal_type) DEFERRABLE INITIALLY DEFERRED
);

-- インデックス設計（タスク要件準拠）

-- 1. user_id, meal_date, meal_type の複合インデックス（主要検索パターン）
CREATE INDEX IF NOT EXISTS idx_meals_user_date_type ON meals(user_id, meal_date, meal_type);

-- 2. ユーザー別の日付範囲検索用
CREATE INDEX IF NOT EXISTS idx_meals_user_date ON meals(user_id, meal_date DESC) WHERE is_deleted = false;

-- 3. 日付別検索用（集計処理用）
CREATE INDEX IF NOT EXISTS idx_meals_date ON meals(meal_date DESC) WHERE is_deleted = false;

-- 4. ユーザー別の最新食事検索用
CREATE INDEX IF NOT EXISTS idx_meals_user_recent ON meals(user_id, created_at DESC) WHERE is_deleted = false;

-- 5. 栄養集計での検索用（分析機能向け）
CREATE INDEX IF NOT EXISTS idx_meals_nutrition_totals ON meals(total_calories, total_protein_g, total_fat_g, total_net_carbs_g) WHERE is_deleted = false;

-- 更新日時の自動更新トリガー
CREATE OR REPLACE FUNCTION update_meals_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_meals_updated_at
    BEFORE UPDATE ON meals
    FOR EACH ROW
    EXECUTE FUNCTION update_meals_updated_at();

-- 栄養集計の更新制約（整合性チェック用関数）
-- 注意：この関数はmeal_itemsテーブル作成後に有効化される
CREATE OR REPLACE FUNCTION validate_meal_nutrition_totals()
RETURNS TRIGGER AS $$
DECLARE
    calculated_calories INTEGER;
    calculated_protein DECIMAL(8,2);
    calculated_fat DECIMAL(8,2);
    calculated_net_carbs DECIMAL(8,2);
    table_exists BOOLEAN;
BEGIN
    -- meal_itemsテーブルの存在確認
    SELECT EXISTS (
        SELECT FROM information_schema.tables 
        WHERE table_schema = 'public' 
        AND table_name = 'meal_items'
    ) INTO table_exists;
    
    -- テーブルが存在しない場合はスキップ
    IF NOT table_exists THEN
        RETURN NEW;
    END IF;
    
    -- 食事アイテムから実際の栄養値を計算
    SELECT 
        COALESCE(SUM(ROUND((f.calories_per_100g * mi.quantity_g / 100.0)::numeric, 0)::integer), 0),
        COALESCE(SUM(ROUND((f.protein_g_per_100g * mi.quantity_g / 100.0)::numeric, 2)), 0.00),
        COALESCE(SUM(ROUND((f.fat_g_per_100g * mi.quantity_g / 100.0)::numeric, 2)), 0.00),
        COALESCE(SUM(ROUND(((f.carbohydrates_g_per_100g - f.fiber_g_per_100g) * mi.quantity_g / 100.0)::numeric, 2)), 0.00)
    INTO 
        calculated_calories,
        calculated_protein,
        calculated_fat,
        calculated_net_carbs
    FROM meal_items mi
    JOIN foods f ON mi.food_id = f.id
    WHERE mi.meal_id = NEW.id AND mi.is_deleted = false;
    
    -- 差異が閾値を超える場合は警告（ログ出力）
    IF ABS(calculated_calories - NEW.total_calories) > 10 OR
       ABS(calculated_protein - NEW.total_protein_g) > 1.0 OR
       ABS(calculated_fat - NEW.total_fat_g) > 1.0 OR
       ABS(calculated_net_carbs - NEW.total_net_carbs_g) > 1.0 THEN
        RAISE NOTICE 'Nutrition totals mismatch for meal_id %: calculated(cal:%, protein:%, fat:%, net_carbs:%) vs stored(cal:%, protein:%, fat:%, net_carbs:%)', 
            NEW.id, calculated_calories, calculated_protein, calculated_fat, calculated_net_carbs,
            NEW.total_calories, NEW.total_protein_g, NEW.total_fat_g, NEW.total_net_carbs_g;
    END IF;
    
    RETURN NEW;
END;
$$ language 'plpgsql';

-- テーブルコメント
COMMENT ON TABLE meals IS '食事記録テーブル - ユーザーの日別・食事タイプ別の食事記録を管理';
COMMENT ON COLUMN meals.id IS '食事記録ID（主キー）';
COMMENT ON COLUMN meals.user_id IS 'ユーザーID（外部キー：users.id）';
COMMENT ON COLUMN meals.meal_date IS '食事日付';
COMMENT ON COLUMN meals.meal_type IS '食事タイプ（BREAKFAST/LUNCH/DINNER/SNACK）';
COMMENT ON COLUMN meals.total_calories IS '合計カロリー（kcal）- キャッシュ値';
COMMENT ON COLUMN meals.total_protein_g IS '合計タンパク質量（g）- キャッシュ値';
COMMENT ON COLUMN meals.total_fat_g IS '合計脂質量（g）- キャッシュ値';
COMMENT ON COLUMN meals.total_net_carbs_g IS '合計正味炭水化物量（g）- キャッシュ値';
COMMENT ON COLUMN meals.notes IS '食事メモ・備考';
COMMENT ON COLUMN meals.is_deleted IS '論理削除フラグ';
