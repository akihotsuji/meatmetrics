-- 食事詳細テーブル（meal_items）の作成
-- ドメインモデル MealItem に対応
-- DDD設計書 docs/2_detail/06_domain_model.md に準拠
-- タスク要件：食事内容（food_id, quantity_g）を管理

CREATE TABLE IF NOT EXISTS meal_items (
    -- 基本情報
    id BIGSERIAL PRIMARY KEY,
    meal_id BIGINT NOT NULL,
    food_id BIGINT NOT NULL,
    quantity_g DECIMAL(8,2) NOT NULL, -- 食材の摂取量（グラム）
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- 栄養成分キャッシュ（パフォーマンス向上用）
    -- 食材の栄養成分×quantity_g/100で算出される値
    item_calories INTEGER DEFAULT 0,
    item_protein_g DECIMAL(8,2) DEFAULT 0.00,
    item_fat_g DECIMAL(8,2) DEFAULT 0.00,
    item_net_carbs_g DECIMAL(8,2) DEFAULT 0.00,
    
    -- メタデータ
    notes TEXT,
    is_deleted BOOLEAN DEFAULT false,
    
    -- 外部キー制約
    CONSTRAINT fk_meal_items_meal_id FOREIGN KEY (meal_id) REFERENCES meals(id) ON DELETE CASCADE,
    CONSTRAINT fk_meal_items_food_id FOREIGN KEY (food_id) REFERENCES foods(id) ON DELETE RESTRICT,
    
    -- バリデーション制約
    CONSTRAINT meal_items_quantity_positive CHECK (quantity_g > 0 AND quantity_g <= 10000), -- 最大10kg
    CONSTRAINT meal_items_item_calories_positive CHECK (item_calories >= 0 AND item_calories <= 10000),
    CONSTRAINT meal_items_item_protein_positive CHECK (item_protein_g >= 0 AND item_protein_g <= 1000),
    CONSTRAINT meal_items_item_fat_positive CHECK (item_fat_g >= 0 AND item_fat_g <= 1000),
    CONSTRAINT meal_items_item_net_carbs_positive CHECK (item_net_carbs_g >= 0 AND item_net_carbs_g <= 1000),
    
    -- 同一食事内での同一食材の重複を防ぐ（ただし、量を変更したい場合はUPDATE）
    CONSTRAINT unique_meal_food UNIQUE (meal_id, food_id) DEFERRABLE INITIALLY DEFERRED
);

-- インデックス設計

-- 1. meal_id での検索（主要検索パターン）
CREATE INDEX IF NOT EXISTS idx_meal_items_meal_id ON meal_items(meal_id) WHERE is_deleted = false;

-- 2. food_id での検索（食材使用履歴検索用）
CREATE INDEX IF NOT EXISTS idx_meal_items_food_id ON meal_items(food_id) WHERE is_deleted = false;

-- 3. 複合インデックス（meal_id + food_id）
CREATE INDEX IF NOT EXISTS idx_meal_items_meal_food ON meal_items(meal_id, food_id) WHERE is_deleted = false;

-- 4. 栄養成分での検索用（分析機能向け）
CREATE INDEX IF NOT EXISTS idx_meal_items_nutrition ON meal_items(item_calories, item_protein_g, item_fat_g, item_net_carbs_g) WHERE is_deleted = false;

-- 5. 作成日時でのソート用
CREATE INDEX IF NOT EXISTS idx_meal_items_created_at ON meal_items(created_at DESC) WHERE is_deleted = false;

-- 更新日時の自動更新トリガー
CREATE OR REPLACE FUNCTION update_meal_items_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_meal_items_updated_at
    BEFORE UPDATE ON meal_items
    FOR EACH ROW
    EXECUTE FUNCTION update_meal_items_updated_at();

-- 栄養成分の自動計算トリガー
-- INSERT/UPDATE時に食材の栄養成分から自動計算
CREATE OR REPLACE FUNCTION calculate_meal_item_nutrition()
RETURNS TRIGGER AS $$
DECLARE
    food_nutrition RECORD;
BEGIN
    -- 食材の栄養成分を取得
    SELECT 
        calories_per_100g,
        protein_g_per_100g,
        fat_g_per_100g,
        carbohydrates_g_per_100g,
        fiber_g_per_100g
    INTO food_nutrition
    FROM foods
    WHERE id = NEW.food_id;
    
    IF food_nutrition IS NULL THEN
        RAISE EXCEPTION 'Food with id % not found', NEW.food_id;
    END IF;
    
    -- 栄養成分を計算（quantity_g/100 倍）
    NEW.item_calories := ROUND((food_nutrition.calories_per_100g * NEW.quantity_g / 100.0)::numeric, 0)::integer;
    NEW.item_protein_g := ROUND((food_nutrition.protein_g_per_100g * NEW.quantity_g / 100.0)::numeric, 2);
    NEW.item_fat_g := ROUND((food_nutrition.fat_g_per_100g * NEW.quantity_g / 100.0)::numeric, 2);
    -- net_carbs = carbohydrates - fiber
    NEW.item_net_carbs_g := ROUND(((food_nutrition.carbohydrates_g_per_100g - food_nutrition.fiber_g_per_100g) * NEW.quantity_g / 100.0)::numeric, 2);
    
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_calculate_meal_item_nutrition
    BEFORE INSERT OR UPDATE ON meal_items
    FOR EACH ROW
    EXECUTE FUNCTION calculate_meal_item_nutrition();

-- 食事記録の栄養集計を更新するトリガー
-- meal_items の変更時に meals.total_* を再計算
CREATE OR REPLACE FUNCTION update_meal_totals()
RETURNS TRIGGER AS $$
DECLARE
    target_meal_id BIGINT;
BEGIN
    -- INSERT/UPDATE の場合
    IF TG_OP = 'INSERT' OR TG_OP = 'UPDATE' THEN
        target_meal_id := NEW.meal_id;
    -- DELETE の場合
    ELSIF TG_OP = 'DELETE' THEN
        target_meal_id := OLD.meal_id;
    END IF;
    
    -- 食事記録の栄養集計を更新
    UPDATE meals
    SET 
        total_calories = COALESCE((
            SELECT SUM(item_calories)
            FROM meal_items
            WHERE meal_id = target_meal_id AND is_deleted = false
        ), 0),
        total_protein_g = COALESCE((
            SELECT SUM(item_protein_g)
            FROM meal_items
            WHERE meal_id = target_meal_id AND is_deleted = false
        ), 0.00),
        total_fat_g = COALESCE((
            SELECT SUM(item_fat_g)
            FROM meal_items
            WHERE meal_id = target_meal_id AND is_deleted = false
        ), 0.00),
        total_net_carbs_g = COALESCE((
            SELECT SUM(item_net_carbs_g)
            FROM meal_items
            WHERE meal_id = target_meal_id AND is_deleted = false
        ), 0.00),
        updated_at = CURRENT_TIMESTAMP
    WHERE id = target_meal_id;
    
    RETURN COALESCE(NEW, OLD);
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_update_meal_totals
    AFTER INSERT OR UPDATE OR DELETE ON meal_items
    FOR EACH ROW
    EXECUTE FUNCTION update_meal_totals();

-- テーブルコメント
COMMENT ON TABLE meal_items IS '食事詳細テーブル - 食事記録内の個別食材と摂取量を管理';
COMMENT ON COLUMN meal_items.id IS '食事詳細ID（主キー）';
COMMENT ON COLUMN meal_items.meal_id IS '食事記録ID（外部キー：meals.id）';
COMMENT ON COLUMN meal_items.food_id IS '食材ID（外部キー：foods.id）';
COMMENT ON COLUMN meal_items.quantity_g IS '摂取量（グラム）';
COMMENT ON COLUMN meal_items.item_calories IS 'この食材分のカロリー（kcal）- キャッシュ値';
COMMENT ON COLUMN meal_items.item_protein_g IS 'この食材分のタンパク質量（g）- キャッシュ値';
COMMENT ON COLUMN meal_items.item_fat_g IS 'この食材分の脂質量（g）- キャッシュ値';
COMMENT ON COLUMN meal_items.item_net_carbs_g IS 'この食材分の正味炭水化物量（g）- キャッシュ値';
COMMENT ON COLUMN meal_items.notes IS '食材に関するメモ・備考';
COMMENT ON COLUMN meal_items.is_deleted IS '論理削除フラグ';
