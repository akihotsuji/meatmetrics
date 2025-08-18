-- カテゴリテーブル（categories）の作成
-- 階層構造に対応した食材カテゴリ管理
-- タスク要件に準拠した構造（id, name, parent_id, level, created_at）

CREATE TABLE IF NOT EXISTS categories (
    -- 基本情報（タスク要件に準拠）
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    parent_id BIGINT REFERENCES categories(id) ON DELETE CASCADE,
    level INTEGER NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- 追加メタデータ（MVP用拡張）
    display_name VARCHAR(100) NOT NULL,
    description TEXT,
    sort_order INTEGER DEFAULT 0,
    is_active BOOLEAN DEFAULT true,
    icon_name VARCHAR(50),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- 階層整合性制約
    CONSTRAINT categories_name_not_empty CHECK (LENGTH(TRIM(name)) > 0),
    CONSTRAINT categories_display_name_not_empty CHECK (LENGTH(TRIM(display_name)) > 0),
    CONSTRAINT categories_level_positive CHECK (level >= 1 AND level <= 10),
    CONSTRAINT categories_sort_order_positive CHECK (sort_order >= 0),
    CONSTRAINT categories_no_self_parent CHECK (id != parent_id),
    
    -- 一意性制約（同じ親内での名前重複を防ぐ）
    CONSTRAINT categories_unique_name_per_parent UNIQUE (parent_id, name)
);

-- インデックス設計（タスク要件準拠）

-- 1. parent_id インデックス（階層クエリ用）
CREATE INDEX IF NOT EXISTS idx_categories_parent_id ON categories(parent_id);

-- 2. level インデックス（階層レベル検索用）
CREATE INDEX IF NOT EXISTS idx_categories_level ON categories(level);

-- 3. パフォーマンス向上のための追加インデックス
CREATE INDEX IF NOT EXISTS idx_categories_name ON categories(name);
CREATE INDEX IF NOT EXISTS idx_categories_is_active ON categories(is_active);
CREATE INDEX IF NOT EXISTS idx_categories_sort_order ON categories(sort_order);

-- 4. 階層クエリ最適化のための複合インデックス
CREATE INDEX IF NOT EXISTS idx_categories_parent_sort ON categories(parent_id, sort_order) WHERE is_active = true;
CREATE INDEX IF NOT EXISTS idx_categories_level_sort ON categories(level, sort_order) WHERE is_active = true;

-- 階層整合性を保証するトリガー関数
CREATE OR REPLACE FUNCTION validate_category_hierarchy()
RETURNS TRIGGER AS $$
DECLARE
    max_depth INTEGER := 10;
    current_level INTEGER := 1;
    parent_level INTEGER;
    check_parent_id BIGINT;
BEGIN
    -- 親IDが指定されている場合
    IF NEW.parent_id IS NOT NULL THEN
        -- 親カテゴリの存在確認
        SELECT level INTO parent_level 
        FROM categories 
        WHERE id = NEW.parent_id AND is_active = true;
        
        IF parent_level IS NULL THEN
            RAISE EXCEPTION 'Parent category does not exist or is inactive: %', NEW.parent_id;
        END IF;
        
        -- レベルの自動計算
        NEW.level := parent_level + 1;
        
        -- 最大深度チェック
        IF NEW.level > max_depth THEN
            RAISE EXCEPTION 'Category hierarchy too deep. Maximum depth is %', max_depth;
        END IF;
        
        -- 循環参照チェック
        check_parent_id := NEW.parent_id;
        current_level := NEW.level;
        
        WHILE check_parent_id IS NOT NULL AND current_level > 0 LOOP
            IF check_parent_id = NEW.id THEN
                RAISE EXCEPTION 'Circular reference detected in category hierarchy';
            END IF;
            
            SELECT parent_id INTO check_parent_id 
            FROM categories 
            WHERE id = check_parent_id;
            
            current_level := current_level - 1;
        END LOOP;
    ELSE
        -- 親IDが指定されていない場合はルートレベル
        NEW.level := 1;
    END IF;
    
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 階層整合性トリガー
CREATE TRIGGER trigger_validate_category_hierarchy
    BEFORE INSERT OR UPDATE ON categories
    FOR EACH ROW
    EXECUTE FUNCTION validate_category_hierarchy();

-- 更新日時の自動更新トリガー
CREATE OR REPLACE FUNCTION update_categories_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_categories_updated_at
    BEFORE UPDATE ON categories
    FOR EACH ROW
    EXECUTE FUNCTION update_categories_updated_at();

-- 便利なビュー：階層パス表示
CREATE OR REPLACE VIEW category_hierarchy AS
WITH RECURSIVE category_tree AS (
    -- ルートカテゴリ
    SELECT 
        id,
        name,
        display_name,
        parent_id,
        level,
        sort_order,
        ARRAY[name] as path_names,
        ARRAY[id] as path_ids,
        name as full_path
    FROM categories 
    WHERE parent_id IS NULL AND is_active = true
    
    UNION ALL
    
    -- 子カテゴリ
    SELECT 
        c.id,
        c.name,
        c.display_name,
        c.parent_id,
        c.level,
        c.sort_order,
        ct.path_names || c.name,
        ct.path_ids || c.id,
        ct.full_path || ' > ' || c.name
    FROM categories c
    INNER JOIN category_tree ct ON ct.id = c.parent_id
    WHERE c.is_active = true
)
SELECT 
    id,
    name,
    display_name,
    parent_id,
    level,
    sort_order,
    path_names,
    path_ids,
    full_path
FROM category_tree
ORDER BY path_names;

-- テーブルコメント
COMMENT ON TABLE categories IS 'カテゴリマスタテーブル - 階層構造による食材分類を管理';
COMMENT ON COLUMN categories.id IS 'カテゴリID（主キー）';
COMMENT ON COLUMN categories.name IS 'カテゴリ名（英語、システム用）';
COMMENT ON COLUMN categories.parent_id IS '親カテゴリID（階層構造用）';
COMMENT ON COLUMN categories.level IS '階層レベル（1=ルート）';
COMMENT ON COLUMN categories.display_name IS '表示名（日本語）';
COMMENT ON COLUMN categories.description IS 'カテゴリの説明';
COMMENT ON COLUMN categories.sort_order IS '表示順序（同階層内）';
COMMENT ON COLUMN categories.is_active IS '有効フラグ（論理削除用）';
COMMENT ON COLUMN categories.icon_name IS 'アイコン名（UI用）';
