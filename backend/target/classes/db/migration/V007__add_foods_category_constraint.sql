-- foodsテーブルとcategoriesテーブルの関連付け
-- 外部キー制約の追加と既存データの更新

-- 1. 既存のfoodsテーブルのcategory_idを適切なカテゴリIDで更新
UPDATE foods SET category_id = (SELECT id FROM categories WHERE name = 'beef')
WHERE name LIKE '%牛肉%' OR name LIKE '%牛%';

UPDATE foods SET category_id = (SELECT id FROM categories WHERE name = 'pork') 
WHERE name LIKE '%豚肉%' OR name LIKE '%豚%';

UPDATE foods SET category_id = (SELECT id FROM categories WHERE name = 'chicken')
WHERE name LIKE '%鶏%';

UPDATE foods SET category_id = (SELECT id FROM categories WHERE name = 'salmon')
WHERE name LIKE '%サケ%';

UPDATE foods SET category_id = (SELECT id FROM categories WHERE name = 'tuna')
WHERE name LIKE '%マグロ%';

UPDATE foods SET category_id = (SELECT id FROM categories WHERE name = 'blue_fish')
WHERE name LIKE '%サバ%' OR name LIKE '%イワシ%';

UPDATE foods SET category_id = (SELECT id FROM categories WHERE name = 'chicken_eggs')
WHERE name LIKE '%鶏卵%' OR name LIKE '%卵%';

UPDATE foods SET category_id = (SELECT id FROM categories WHERE name = 'milk')
WHERE name LIKE '%牛乳%';

UPDATE foods SET category_id = (SELECT id FROM categories WHERE name = 'cheese')
WHERE name LIKE '%チーズ%';

UPDATE foods SET category_id = (SELECT id FROM categories WHERE name = 'butter')
WHERE name LIKE '%バター%';

UPDATE foods SET category_id = (SELECT id FROM categories WHERE name = 'cream')
WHERE name LIKE '%クリーム%';

UPDATE foods SET category_id = (SELECT id FROM categories WHERE name = 'yogurt')
WHERE name LIKE '%ヨーグルト%';

-- 2. category_idがNULLの食材があれば、適切なデフォルトカテゴリを設定
-- 肉類っぽいものは肉類のデフォルトカテゴリに
UPDATE foods SET category_id = (SELECT id FROM categories WHERE name = 'meat')
WHERE category_id IS NULL 
  AND (tags && ARRAY['肉類'] OR name ~* '肉|ベーコン|ソーセージ');

-- 魚類っぽいものは魚類のデフォルトカテゴリに  
UPDATE foods SET category_id = (SELECT id FROM categories WHERE name = 'fish')
WHERE category_id IS NULL 
  AND (tags && ARRAY['魚類'] OR name ~* '魚|フィッシュ');

-- 卵類っぽいものは卵類のデフォルトカテゴリに
UPDATE foods SET category_id = (SELECT id FROM categories WHERE name = 'eggs')
WHERE category_id IS NULL 
  AND (tags && ARRAY['卵類'] OR name ~* '卵');

-- 乳製品っぽいものは乳製品のデフォルトカテゴリに
UPDATE foods SET category_id = (SELECT id FROM categories WHERE name = 'dairy')
WHERE category_id IS NULL 
  AND (tags && ARRAY['乳製品'] OR name ~* '乳|ミルク');

-- 3. まだNULLのものは「その他」カテゴリを作成して設定
INSERT INTO categories (name, display_name, description, sort_order, icon_name, level)
VALUES ('others', 'その他', 'その他の食材', 99, 'question', 1)
ON CONFLICT (parent_id, name) DO NOTHING;

UPDATE foods SET category_id = (SELECT id FROM categories WHERE name = 'others')
WHERE category_id IS NULL;

-- 4. 外部キー制約を追加（これでcategory_idがNULLでない制約も追加）
ALTER TABLE foods 
    ADD CONSTRAINT fk_foods_category_id 
    FOREIGN KEY (category_id) 
    REFERENCES categories(id) 
    ON DELETE RESTRICT 
    ON UPDATE CASCADE;

-- 5. category_idのNOT NULL制約を追加
ALTER TABLE foods 
    ALTER COLUMN category_id SET NOT NULL;

-- 6. 外部キー制約用のインデックスを確認（既存のidx_foods_category_idがあるため不要）
-- CREATE INDEX IF NOT EXISTS idx_foods_category_id ON foods(category_id); -- 既に存在

-- 7. 統計情報の更新
ANALYZE foods;
ANALYZE categories;

-- 8. 制約確認用のクエリ（コメントアウト、必要時に実行）
-- SELECT 
--     f.name as food_name,
--     c.display_name as category_name,
--     c.level as category_level
-- FROM foods f
-- JOIN categories c ON f.category_id = c.id
-- ORDER BY c.level, c.sort_order, f.name
-- LIMIT 10;
