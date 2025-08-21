-- 食材テーブル（foods）の作成
-- MVP段階での基本情報と栄養成分を含む
-- タスク要件に準拠したシンプルな構造

CREATE TABLE IF NOT EXISTS foods (
    -- 基本情報（タスク要件に準拠）
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    category_id BIGINT, -- 将来的にcategoriesテーブルとの外部キー制約予定
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- 栄養成分（100gあたり、タスク要件準拠）
    calories_per_100g INTEGER NOT NULL,
    protein_g_per_100g DECIMAL(6,2) NOT NULL,
    fat_g_per_100g DECIMAL(6,2) NOT NULL,
    carbohydrates_g_per_100g DECIMAL(6,2) NOT NULL,
    fiber_g_per_100g DECIMAL(6,2) DEFAULT 0.00,
    
    -- タグ管理（TEXT[] 配列、検索とフィルタリング用）
    tags TEXT[] DEFAULT '{}',
    
    -- メタデータ
    description TEXT,
    is_active BOOLEAN DEFAULT true,
    
    -- バリデーション制約
    CONSTRAINT foods_name_not_empty CHECK (LENGTH(TRIM(name)) > 0),
    CONSTRAINT foods_calories_positive CHECK (calories_per_100g >= 0 AND calories_per_100g <= 2000),
    CONSTRAINT foods_protein_positive CHECK (protein_g_per_100g >= 0 AND protein_g_per_100g <= 200),
    CONSTRAINT foods_fat_positive CHECK (fat_g_per_100g >= 0 AND fat_g_per_100g <= 200),
    CONSTRAINT foods_carbs_positive CHECK (carbohydrates_g_per_100g >= 0 AND carbohydrates_g_per_100g <= 200),
    CONSTRAINT foods_fiber_positive CHECK (fiber_g_per_100g >= 0 AND fiber_g_per_100g <= carbohydrates_g_per_100g),
    CONSTRAINT foods_category_positive CHECK (category_id IS NULL OR category_id > 0)
);

-- インデックス設計（タスク要件準拠）

-- 1. name の全文検索インデックス（日本語対応）
CREATE INDEX IF NOT EXISTS idx_foods_name_search ON foods USING gin(to_tsvector('simple', name));

-- 2. category_id インデックス（カテゴリ別検索用）
CREATE INDEX IF NOT EXISTS idx_foods_category_id ON foods(category_id);

-- 3. tags の GIN インデックス（タグ検索用）
CREATE INDEX IF NOT EXISTS idx_foods_tags ON foods USING gin(tags);

-- 4. パフォーマンス向上のための追加インデックス
CREATE INDEX IF NOT EXISTS idx_foods_name ON foods(name);
CREATE INDEX IF NOT EXISTS idx_foods_is_active ON foods(is_active);
CREATE INDEX IF NOT EXISTS idx_foods_created_at ON foods(created_at);

-- 5. 栄養成分での検索用複合インデックス（カーニボア向け）
CREATE INDEX IF NOT EXISTS idx_foods_nutrition_carnivore ON foods(protein_g_per_100g DESC, fat_g_per_100g DESC, carbohydrates_g_per_100g ASC) WHERE is_active = true;

-- 更新日時の自動更新トリガー
CREATE OR REPLACE FUNCTION update_foods_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_foods_updated_at
    BEFORE UPDATE ON foods
    FOR EACH ROW
    EXECUTE FUNCTION update_foods_updated_at();

-- テーブルコメント
COMMENT ON TABLE foods IS '食材マスタテーブル - 栄養成分とタグ情報を管理';
COMMENT ON COLUMN foods.id IS '食材ID（主キー）';
COMMENT ON COLUMN foods.name IS '食材名（検索対象）';
COMMENT ON COLUMN foods.category_id IS 'カテゴリID（将来的に外部キー制約予定）';
COMMENT ON COLUMN foods.calories_per_100g IS '100gあたりのカロリー（kcal）';
COMMENT ON COLUMN foods.protein_g_per_100g IS '100gあたりのタンパク質量（g）';
COMMENT ON COLUMN foods.fat_g_per_100g IS '100gあたりの脂質量（g）';
COMMENT ON COLUMN foods.carbohydrates_g_per_100g IS '100gあたりの炭水化物量（g）';
COMMENT ON COLUMN foods.fiber_g_per_100g IS '100gあたりの食物繊維量（g）';
COMMENT ON COLUMN foods.tags IS 'タグ配列（低糖質、高タンパク等）';
COMMENT ON COLUMN foods.description IS '食材の説明・備考';
COMMENT ON COLUMN foods.is_active IS '有効フラグ（論理削除用）';
