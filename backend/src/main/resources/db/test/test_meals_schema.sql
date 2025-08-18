-- 食事記録テーブル（meals, meal_items）のテストスキーマ
-- 開発・テスト用のスキーマ検証とサンプルクエリ

-- ===== テーブル存在確認 =====
SELECT 
    table_name,
    table_type,
    is_insertable_into
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN ('meals', 'meal_items')
ORDER BY table_name;

-- ===== カラム定義確認 =====
-- meals テーブル
SELECT 
    column_name,
    data_type,
    character_maximum_length,
    is_nullable,
    column_default,
    ordinal_position
FROM information_schema.columns 
WHERE table_schema = 'public' 
AND table_name = 'meals'
ORDER BY ordinal_position;

-- meal_items テーブル
SELECT 
    column_name,
    data_type,
    character_maximum_length,
    is_nullable,
    column_default,
    ordinal_position
FROM information_schema.columns 
WHERE table_schema = 'public' 
AND table_name = 'meal_items'
ORDER BY ordinal_position;

-- ===== インデックス確認 =====
SELECT 
    t.relname as table_name,
    i.relname as index_name,
    ix.indisunique as is_unique,
    ix.indisprimary as is_primary,
    array_to_string(array_agg(a.attname ORDER BY c.ordinality), ', ') as columns
FROM pg_class t
JOIN pg_index ix ON t.oid = ix.indrelid
JOIN pg_class i ON i.oid = ix.indexrelid
JOIN unnest(ix.indkey) WITH ORDINALITY c(key, ordinality) ON true
JOIN pg_attribute a ON a.attrelid = t.oid AND a.attnum = c.key
WHERE t.relname IN ('meals', 'meal_items')
GROUP BY t.relname, i.relname, ix.indisunique, ix.indisprimary
ORDER BY t.relname, i.relname;

-- ===== 外部キー制約確認 =====
SELECT 
    tc.table_name,
    tc.constraint_name,
    tc.constraint_type,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu 
    ON tc.constraint_name = kcu.constraint_name
    AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu 
    ON ccu.constraint_name = tc.constraint_name
    AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY' 
    AND tc.table_name IN ('meals', 'meal_items')
ORDER BY tc.table_name, tc.constraint_name;

-- ===== CHECK制約確認 =====
SELECT 
    tc.table_name,
    tc.constraint_name,
    cc.check_clause
FROM information_schema.table_constraints AS tc
JOIN information_schema.check_constraints AS cc 
    ON tc.constraint_name = cc.constraint_name
WHERE tc.constraint_type = 'CHECK' 
    AND tc.table_name IN ('meals', 'meal_items')
ORDER BY tc.table_name, tc.constraint_name;

-- ===== トリガー確認 =====
SELECT 
    trigger_name,
    event_manipulation,
    event_object_table,
    action_timing,
    action_statement
FROM information_schema.triggers
WHERE event_object_table IN ('meals', 'meal_items')
ORDER BY event_object_table, trigger_name;

-- ===== サンプルデータの確認 =====
-- 食事記録の件数確認
SELECT 
    user_id,
    COUNT(*) as meal_count,
    MIN(meal_date) as earliest_date,
    MAX(meal_date) as latest_date,
    SUM(total_calories) as total_calories_sum,
    AVG(total_calories) as avg_calories_per_meal
FROM meals 
GROUP BY user_id 
ORDER BY user_id;

-- 食事タイプ別の統計
SELECT 
    meal_type,
    COUNT(*) as count,
    ROUND(AVG(total_calories), 1) as avg_calories,
    ROUND(AVG(total_protein_g), 1) as avg_protein,
    ROUND(AVG(total_fat_g), 1) as avg_fat,
    ROUND(AVG(total_net_carbs_g), 1) as avg_net_carbs
FROM meals 
WHERE is_deleted = false
GROUP BY meal_type 
ORDER BY meal_type;

-- 日別の栄養摂取量（user_id=1の過去7日分）
SELECT 
    meal_date,
    SUM(total_calories) as daily_calories,
    ROUND(SUM(total_protein_g), 1) as daily_protein,
    ROUND(SUM(total_fat_g), 1) as daily_fat,
    ROUND(SUM(total_net_carbs_g), 1) as daily_net_carbs,
    COUNT(*) as meals_per_day
FROM meals 
WHERE user_id = 1 
    AND meal_date >= CURRENT_DATE - INTERVAL '7 days'
    AND is_deleted = false
GROUP BY meal_date 
ORDER BY meal_date DESC;

-- 食事詳細の統計（存在する場合）
SELECT 
    COUNT(*) as meal_item_count,
    COUNT(DISTINCT meal_id) as distinct_meals,
    COUNT(DISTINCT food_id) as distinct_foods,
    ROUND(AVG(quantity_g), 1) as avg_quantity_g,
    ROUND(SUM(item_calories), 0) as total_item_calories
FROM meal_items 
WHERE is_deleted = false;

-- 食事と食事詳細の結合確認（栄養値の整合性チェック）
SELECT 
    m.id as meal_id,
    m.meal_date,
    m.meal_type,
    m.total_calories as meal_total_calories,
    COALESCE(SUM(mi.item_calories), 0) as calculated_calories,
    ABS(m.total_calories - COALESCE(SUM(mi.item_calories), 0)) as calories_diff,
    m.total_protein_g as meal_total_protein,
    COALESCE(SUM(mi.item_protein_g), 0) as calculated_protein,
    ABS(m.total_protein_g - COALESCE(SUM(mi.item_protein_g), 0)) as protein_diff
FROM meals m
LEFT JOIN meal_items mi ON m.id = mi.meal_id AND mi.is_deleted = false
WHERE m.is_deleted = false
GROUP BY m.id, m.meal_date, m.meal_type, m.total_calories, m.total_protein_g
ORDER BY m.meal_date DESC, m.meal_type
LIMIT 10;

-- ===== パフォーマンステスト用クエリ =====
-- 大量データでのパフォーマンステスト（実際のデータ量では実行時間は短い）
EXPLAIN (ANALYZE, BUFFERS) 
SELECT 
    m.user_id,
    m.meal_date,
    COUNT(*) as meals_count,
    SUM(m.total_calories) as daily_calories
FROM meals m
WHERE m.meal_date >= CURRENT_DATE - INTERVAL '30 days'
    AND m.is_deleted = false
GROUP BY m.user_id, m.meal_date
ORDER BY m.user_id, m.meal_date DESC;

-- 食材別使用頻度（TOP 10）
SELECT 
    f.name as food_name,
    COUNT(*) as usage_count,
    ROUND(AVG(mi.quantity_g), 1) as avg_quantity_g,
    ROUND(SUM(mi.item_calories), 0) as total_calories_contributed
FROM meal_items mi
JOIN foods f ON mi.food_id = f.id
WHERE mi.is_deleted = false
GROUP BY f.id, f.name
ORDER BY usage_count DESC, total_calories_contributed DESC
LIMIT 10;

-- ===== データ整合性チェック =====
-- 孤児レコードの確認
SELECT 'meals with invalid user_id' as check_type, COUNT(*) as count
FROM meals m
LEFT JOIN users u ON m.user_id = u.id
WHERE u.id IS NULL

UNION ALL

SELECT 'meal_items with invalid meal_id' as check_type, COUNT(*) as count
FROM meal_items mi
LEFT JOIN meals m ON mi.meal_id = m.id
WHERE m.id IS NULL

UNION ALL

SELECT 'meal_items with invalid food_id' as check_type, COUNT(*) as count
FROM meal_items mi
LEFT JOIN foods f ON mi.food_id = f.id
WHERE f.id IS NULL;

-- 栄養値の異常値チェック
SELECT 
    'meals with extreme nutrition values' as check_type,
    COUNT(*) as count
FROM meals
WHERE total_calories > 5000 
    OR total_calories < 0
    OR total_protein_g > 500
    OR total_protein_g < 0
    OR total_fat_g > 500
    OR total_fat_g < 0
    OR total_net_carbs_g > 500
    OR total_net_carbs_g < 0;

-- ===== 集計関数のテスト =====
-- 週別集計のテスト
SELECT 
    DATE_TRUNC('week', meal_date) as week_start,
    user_id,
    COUNT(*) as meals_in_week,
    ROUND(AVG(total_calories), 1) as avg_daily_calories,
    ROUND(SUM(total_calories), 0) as total_weekly_calories
FROM meals
WHERE meal_date >= CURRENT_DATE - INTERVAL '4 weeks'
    AND is_deleted = false
GROUP BY DATE_TRUNC('week', meal_date), user_id
ORDER BY week_start DESC, user_id;

-- ===== 最終メッセージ =====
SELECT 
    'Test schema verification completed' as status,
    NOW() as execution_time,
    (SELECT COUNT(*) FROM meals) as total_meals,
    (SELECT COUNT(*) FROM meal_items) as total_meal_items;
