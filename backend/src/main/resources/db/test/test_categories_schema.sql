-- カテゴリテーブルのスキーマ検証とテスト用SQL
-- 階層構造の動作確認用

-- 1. テーブル構造の確認
\echo '=== Categories Table Structure ==='
SELECT 
    column_name, 
    data_type, 
    is_nullable, 
    column_default,
    character_maximum_length,
    numeric_precision,
    numeric_scale
FROM information_schema.columns 
WHERE table_name = 'categories' 
  AND table_schema = 'public'
ORDER BY ordinal_position;

-- 2. 制約の確認
\echo '=== Categories Table Constraints ==='
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
WHERE tc.table_name = 'categories' 
  AND tc.table_schema = 'public'
ORDER BY tc.constraint_type, tc.constraint_name;

-- 3. 外部キー制約の確認
\echo '=== Foreign Key Constraints ==='
SELECT 
    tc.constraint_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc 
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY' 
  AND (tc.table_name = 'categories' OR ccu.table_name = 'categories');

-- 4. インデックスの確認
\echo '=== Categories Table Indexes ==='
SELECT 
    indexname,
    indexdef
FROM pg_indexes 
WHERE tablename = 'categories' 
  AND schemaname = 'public'
ORDER BY indexname;

-- 5. 階層構造の確認
\echo '=== Category Hierarchy Overview ==='
SELECT 
    level,
    COUNT(*) as category_count,
    MIN(sort_order) as min_sort_order,
    MAX(sort_order) as max_sort_order
FROM categories
WHERE is_active = true
GROUP BY level
ORDER BY level;

-- 6. ルートカテゴリの確認
\echo '=== Root Categories ==='
SELECT 
    id,
    name,
    display_name,
    sort_order,
    (SELECT COUNT(*) FROM categories c2 WHERE c2.parent_id = c1.id) as child_count
FROM categories c1
WHERE parent_id IS NULL 
  AND is_active = true
ORDER BY sort_order;

-- 7. 各ルートカテゴリの子カテゴリ確認
\echo '=== Category Tree Structure ==='
SELECT * FROM category_hierarchy
WHERE level <= 3
ORDER BY path_names
LIMIT 20;

-- 8. 食材との関連確認
\echo '=== Categories with Food Count ==='
SELECT 
    c.id,
    c.name,
    c.display_name,
    c.level,
    COUNT(f.id) as food_count
FROM categories c
LEFT JOIN foods f ON c.id = f.category_id
WHERE c.is_active = true
GROUP BY c.id, c.name, c.display_name, c.level
HAVING COUNT(f.id) > 0
ORDER BY c.level, COUNT(f.id) DESC;

-- 9. 階層整合性テスト（正常系）
\echo '=== Hierarchy Integrity Tests (Success Cases) ==='
BEGIN;
-- 正常な子カテゴリ追加
INSERT INTO categories (name, display_name, parent_id, description)
VALUES ('test_beef_cut', 'テスト牛肉部位', 
        (SELECT id FROM categories WHERE name = 'beef'), 
        'テスト用の牛肉部位');

SELECT 'Test 1 PASSED: 正常な子カテゴリ作成' as test_result;
ROLLBACK;

-- 10. 階層整合性テスト（異常系）
\echo '=== Hierarchy Integrity Tests (Error Cases) ==='

-- 10.1 循環参照テスト
\echo 'Testing circular reference prevention...'
BEGIN;
-- 存在しない親IDでの作成テスト
INSERT INTO categories (name, display_name, parent_id)
VALUES ('test_invalid', 'テスト無効カテゴリ', 99999);
-- この操作は外部キー制約違反でエラーになるはず
ROLLBACK;

-- 10.2 レベル制約テスト
\echo 'Testing level constraints...'
BEGIN;
-- 深すぎる階層のテスト
INSERT INTO categories (name, display_name, level)
VALUES ('test_deep', 'テスト深い階層', 15);
-- この操作は制約違反でエラーになるはず
ROLLBACK;

-- 11. 階層クエリのパフォーマンステスト
\echo '=== Hierarchy Query Performance Test ==='
EXPLAIN (ANALYZE, BUFFERS)
WITH RECURSIVE category_children AS (
    SELECT id, name, display_name, parent_id, level, 1 as depth
    FROM categories 
    WHERE name = 'meat' AND is_active = true
    
    UNION ALL
    
    SELECT c.id, c.name, c.display_name, c.parent_id, c.level, cc.depth + 1
    FROM categories c
    INNER JOIN category_children cc ON cc.id = c.parent_id
    WHERE c.is_active = true AND cc.depth < 10
)
SELECT id, name, display_name, level, depth
FROM category_children
ORDER BY depth, name;

-- 12. トリガー動作確認（階層レベル自動計算）
\echo '=== Trigger Test (Auto Level Calculation) ==='
BEGIN;
-- 新しい子カテゴリを追加してレベルが自動計算されるかテスト
INSERT INTO categories (name, display_name, parent_id, description)
VALUES ('auto_level_test', 'レベル自動計算テスト', 
        (SELECT id FROM categories WHERE name = 'beef'), 
        'レベル自動計算のテスト');

SELECT 
    name,
    parent_id,
    level,
    (SELECT level FROM categories WHERE id = categories.parent_id) as parent_level
FROM categories 
WHERE name = 'auto_level_test';

ROLLBACK;

-- 13. 更新日時トリガー確認
\echo '=== Updated At Trigger Test ==='
BEGIN;
UPDATE categories 
SET description = description || ' (更新テスト)'
WHERE name = 'meat'
LIMIT 1;

SELECT 
    name,
    created_at,
    updated_at,
    (updated_at > created_at) as trigger_working
FROM categories 
WHERE name = 'meat'
LIMIT 1;
ROLLBACK;

-- 14. カテゴリビューの確認
\echo '=== Category Hierarchy View Test ==='
SELECT 
    id,
    name,
    display_name,
    level,
    full_path
FROM category_hierarchy
WHERE level <= 2
ORDER BY level, sort_order
LIMIT 15;

-- 15. 食材カテゴリ関連のクエリテスト
\echo '=== Food-Category Relationship Test ==='
SELECT 
    c.display_name as category,
    c.level,
    f.name as food_name,
    f.protein_g_per_100g,
    f.tags
FROM categories c
JOIN foods f ON c.id = f.category_id
WHERE c.name IN ('beef', 'chicken', 'salmon')
ORDER BY c.sort_order, f.protein_g_per_100g DESC
LIMIT 10;

-- 16. 統計情報
\echo '=== Category Statistics ==='
SELECT 
    'Total Categories' as metric,
    COUNT(*) as value
FROM categories
WHERE is_active = true
UNION ALL
SELECT 
    'Max Hierarchy Level',
    MAX(level)
FROM categories
WHERE is_active = true
UNION ALL
SELECT 
    'Categories with Foods',
    COUNT(DISTINCT f.category_id)
FROM foods f
JOIN categories c ON f.category_id = c.id
WHERE c.is_active = true
UNION ALL
SELECT 
    'Orphaned Categories',
    COUNT(*)
FROM categories c
LEFT JOIN foods f ON c.id = f.category_id
WHERE c.is_active = true AND f.id IS NULL;

\echo '=== Categories Schema Tests Completed ==='
