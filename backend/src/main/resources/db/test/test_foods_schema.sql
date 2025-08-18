-- 食材テーブルのスキーマ検証とテスト用SQL
-- 開発環境での動作確認用

-- 1. テーブル構造の確認
\echo '=== Foods Table Structure ==='
SELECT 
    column_name, 
    data_type, 
    is_nullable, 
    column_default,
    character_maximum_length,
    numeric_precision,
    numeric_scale
FROM information_schema.columns 
WHERE table_name = 'foods' 
  AND table_schema = 'public'
ORDER BY ordinal_position;

-- 2. 制約の確認
\echo '=== Foods Table Constraints ==='
SELECT 
    tc.constraint_name,
    tc.constraint_type,
    kcu.column_name,
    cc.check_clause
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu 
    ON tc.constraint_name = kcu.constraint_name
LEFT JOIN information_schema.check_constraints cc 
    ON tc.constraint_name = cc.constraint_name
WHERE tc.table_name = 'foods' 
  AND tc.table_schema = 'public'
ORDER BY tc.constraint_type, tc.constraint_name;

-- 3. インデックスの確認
\echo '=== Foods Table Indexes ==='
SELECT 
    indexname,
    indexdef
FROM pg_indexes 
WHERE tablename = 'foods' 
  AND schemaname = 'public'
ORDER BY indexname;

-- 4. 初期データの確認
\echo '=== Initial Foods Data ==='
SELECT 
    COUNT(*) as total_foods,
    COUNT(CASE WHEN is_active = true THEN 1 END) as active_foods,
    COUNT(DISTINCT ARRAY_LENGTH(tags, 1)) as unique_tag_counts
FROM foods;

-- 5. タグ分析
\echo '=== Tag Analysis ==='
SELECT 
    tag,
    COUNT(*) as food_count
FROM foods, UNNEST(tags) as tag
GROUP BY tag
ORDER BY food_count DESC, tag;

-- 6. 栄養成分の統計
\echo '=== Nutrition Statistics ==='
SELECT 
    'Calories' as nutrient,
    MIN(calories_per_100g) as min_value,
    MAX(calories_per_100g) as max_value,
    ROUND(AVG(calories_per_100g), 2) as avg_value
FROM foods WHERE is_active = true
UNION ALL
SELECT 
    'Protein',
    MIN(protein_g_per_100g),
    MAX(protein_g_per_100g),
    ROUND(AVG(protein_g_per_100g), 2)
FROM foods WHERE is_active = true
UNION ALL
SELECT 
    'Fat',
    MIN(fat_g_per_100g),
    MAX(fat_g_per_100g),
    ROUND(AVG(fat_g_per_100g), 2)
FROM foods WHERE is_active = true
UNION ALL
SELECT 
    'Carbs',
    MIN(carbohydrates_g_per_100g),
    MAX(carbohydrates_g_per_100g),
    ROUND(AVG(carbohydrates_g_per_100g), 2)
FROM foods WHERE is_active = true
ORDER BY nutrient;

-- 7. バリデーションテスト（正常系）
\echo '=== Validation Tests (Success Cases) ==='
BEGIN;
INSERT INTO foods (name, calories_per_100g, protein_g_per_100g, fat_g_per_100g, carbohydrates_g_per_100g, tags)
VALUES ('テスト食材', 200, 20.5, 10.2, 5.0, '{"テスト", "バリデーション"}');

SELECT 'Test 1 PASSED: 正常な食材作成' as test_result;
ROLLBACK;

-- 8. バリデーションテスト（異常系）
\echo '=== Validation Tests (Error Cases) ==='

-- 8.1 負の栄養成分値テスト
BEGIN;
INSERT INTO foods (name, calories_per_100g, protein_g_per_100g, fat_g_per_100g, carbohydrates_g_per_100g)
VALUES ('無効食材', -100, 20.0, 10.0, 5.0);
-- この操作は制約違反でエラーになるはず
ROLLBACK;

-- 8.2 栄養成分上限値テスト
BEGIN;
INSERT INTO foods (name, calories_per_100g, protein_g_per_100g, fat_g_per_100g, carbohydrates_g_per_100g)
VALUES ('無効食材2', 3000, 20.0, 10.0, 5.0);
-- この操作は制約違反でエラーになるはず
ROLLBACK;

-- 8.3 食物繊維 > 炭水化物テスト
BEGIN;
INSERT INTO foods (name, calories_per_100g, protein_g_per_100g, fat_g_per_100g, carbohydrates_g_per_100g, fiber_g_per_100g)
VALUES ('無効食材3', 100, 20.0, 10.0, 5.0, 10.0);
-- この操作は制約違反でエラーになるはず
ROLLBACK;

-- 9. 検索機能テスト
\echo '=== Search Function Tests ==='

-- 9.1 名前での検索
SELECT 'Name Search Test' as test_name, COUNT(*) as match_count
FROM foods
WHERE name ILIKE '%牛肉%';

-- 9.2 タグでの検索
SELECT 'Tag Search Test' as test_name, COUNT(*) as match_count
FROM foods
WHERE tags && ARRAY['高タンパク'];

-- 9.3 栄養成分での検索（高タンパク・低糖質）
SELECT 'Nutrition Search Test' as test_name, COUNT(*) as match_count
FROM foods
WHERE protein_g_per_100g >= 20.0 
  AND carbohydrates_g_per_100g <= 5.0
  AND is_active = true;

-- 10. 全文検索インデックステスト
\echo '=== Full-text Search Index Test ==='
EXPLAIN (ANALYZE, BUFFERS) 
SELECT name, protein_g_per_100g, fat_g_per_100g 
FROM foods 
WHERE to_tsvector('simple', name) @@ to_tsquery('simple', '牛肉')
  AND is_active = true;

-- 11. GINインデックステスト
\echo '=== GIN Index Test ==='
EXPLAIN (ANALYZE, BUFFERS)
SELECT name, tags
FROM foods
WHERE tags && ARRAY['高タンパク', '低糖質']
  AND is_active = true;

-- 12. トリガー動作確認（updated_at自動更新）
\echo '=== Trigger Test ==='
BEGIN;
-- 既存食材の名前を更新
UPDATE foods 
SET name = name || ' (更新テスト)'
WHERE name LIKE '%牛肉サーロイン%'
LIMIT 1;

SELECT 
    name,
    created_at,
    updated_at,
    (updated_at > created_at) as trigger_working
FROM foods 
WHERE name LIKE '%更新テスト%'
LIMIT 1;
ROLLBACK;

-- 13. カーニボア向け栄養成分ランキング
\echo '=== Carnivore-friendly Foods Ranking ==='
SELECT 
    name,
    protein_g_per_100g as protein,
    fat_g_per_100g as fat,
    carbohydrates_g_per_100g as carbs,
    ROUND((protein_g_per_100g * 2 + fat_g_per_100g - carbohydrates_g_per_100g * 5), 2) as carnivore_score
FROM foods
WHERE is_active = true
ORDER BY carnivore_score DESC
LIMIT 10;

\echo '=== Foods Schema Tests Completed ==='
