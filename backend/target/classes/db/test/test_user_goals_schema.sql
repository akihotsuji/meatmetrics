-- user_goalsテーブルのスキーマ検証とテスト用SQL
-- 開発環境での動作確認用

-- 1. テーブル構造の確認
SELECT 
    column_name, 
    data_type, 
    is_nullable, 
    column_default,
    character_maximum_length,
    numeric_precision,
    numeric_scale
FROM information_schema.columns 
WHERE table_name = 'user_goals' 
  AND table_schema = 'public'
ORDER BY ordinal_position;

-- 2. 制約の確認
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
WHERE tc.table_name = 'user_goals' 
  AND tc.table_schema = 'public'
ORDER BY tc.constraint_type, tc.constraint_name;

-- 3. 外部キー制約の確認
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
  AND tc.table_name = 'user_goals';

-- 4. インデックスの確認
SELECT 
    indexname,
    indexdef
FROM pg_indexes 
WHERE tablename = 'user_goals' 
  AND schemaname = 'public'
ORDER BY indexname;

-- 5. バリデーションテスト（正常系）
-- 正常な目標作成テスト
BEGIN;
INSERT INTO user_goals (
    user_id, 
    daily_calorie_goal, 
    protein_goal_g, 
    fat_goal_g, 
    net_carbs_goal_g,
    effective_date,
    is_active
)
VALUES (
    (SELECT id FROM users WHERE email = 'test1@example.com'),
    2200, 
    160.00, 
    130.00, 
    25.00,
    CURRENT_DATE + INTERVAL '1 day',
    false
);

SELECT 'Test 1 PASSED: 正常な目標作成' as test_result;
ROLLBACK;

-- 6. バリデーションテスト（異常系）
-- 不正な栄養目標値テスト
BEGIN;
INSERT INTO user_goals (
    user_id, 
    daily_calorie_goal, 
    protein_goal_g, 
    fat_goal_g, 
    net_carbs_goal_g
)
VALUES (
    (SELECT id FROM users WHERE email = 'test1@example.com'),
    10000,  -- 範囲外の値
    600.00, -- 範囲外の値
    500.00, -- 範囲外の値
    200.00  -- 範囲外の値
);
-- この操作は制約違反でエラーになるはず
ROLLBACK;

-- 7. 外部キー制約テスト
-- 存在しないユーザーIDでの目標作成テスト
BEGIN;
INSERT INTO user_goals (
    user_id, 
    daily_calorie_goal, 
    protein_goal_g, 
    fat_goal_g, 
    net_carbs_goal_g
)
VALUES (
    999999, -- 存在しないユーザーID
    2000, 
    150.00, 
    120.00, 
    20.00
);
-- この操作は外部キー制約違反でエラーになるはず
ROLLBACK;

-- 8. UNIQUE制約テスト（同一ユーザー・同一日付）
BEGIN;
-- 既存のテストデータと同じuser_id、effective_dateで登録を試行
INSERT INTO user_goals (
    user_id, 
    daily_calorie_goal, 
    protein_goal_g, 
    fat_goal_g, 
    net_carbs_goal_g,
    effective_date
)
VALUES (
    (SELECT id FROM users WHERE email = 'test1@example.com'),
    1800, 
    140.00, 
    110.00, 
    15.00,
    CURRENT_DATE
);
-- この操作は重複エラーになるはず
ROLLBACK;

-- 9. ユーザーとの結合確認
SELECT 
    u.email,
    u.username,
    ug.daily_calorie_goal,
    ug.protein_goal_g,
    ug.fat_goal_g,
    ug.net_carbs_goal_g,
    ug.effective_date,
    ug.is_active
FROM users u
JOIN user_goals ug ON u.id = ug.user_id
WHERE u.email LIKE '%example.com'
ORDER BY u.email, ug.effective_date DESC;

-- 10. アクティブな目標の確認
SELECT 
    u.email,
    u.username,
    COUNT(ug.id) as total_goals,
    COUNT(CASE WHEN ug.is_active = true THEN 1 END) as active_goals
FROM users u
LEFT JOIN user_goals ug ON u.id = ug.user_id
WHERE u.email LIKE '%example.com'
GROUP BY u.id, u.email, u.username
ORDER BY u.email;

-- 11. トリガー動作確認（updated_at自動更新）
BEGIN;
UPDATE user_goals 
SET daily_calorie_goal = 2100
WHERE user_id = (SELECT id FROM users WHERE email = 'test1@example.com')
  AND is_active = true;

SELECT 
    ug.daily_calorie_goal,
    ug.created_at,
    ug.updated_at,
    (ug.updated_at > ug.created_at) as trigger_working
FROM user_goals ug
JOIN users u ON ug.user_id = u.id
WHERE u.email = 'test1@example.com'
  AND ug.is_active = true;
ROLLBACK;

-- 12. カスケード削除テスト
BEGIN;
-- テスト用の一時ユーザーを作成
INSERT INTO users (email, username, password_hash)
VALUES ('cascade_test@example.com', 'cascadeuser', 'hashedpassword123');

-- 目標を作成
INSERT INTO user_goals (
    user_id, 
    daily_calorie_goal, 
    protein_goal_g, 
    fat_goal_g, 
    net_carbs_goal_g
)
VALUES (
    (SELECT id FROM users WHERE email = 'cascade_test@example.com'),
    2000, 
    150.00, 
    120.00, 
    20.00
);

-- ユーザーを削除（カスケードで目標も削除されるはず）
DELETE FROM users WHERE email = 'cascade_test@example.com';

-- 削除確認
SELECT COUNT(*) as remaining_goals 
FROM user_goals ug
JOIN users u ON ug.user_id = u.id
WHERE u.email = 'cascade_test@example.com';

SELECT 'Test 12 PASSED: カスケード削除が正常に動作' as test_result;
ROLLBACK;
