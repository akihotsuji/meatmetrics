-- user_goals分離マイグレーション実行用スクリプト
-- 本番環境での実行前に必ずバックアップを取得してください

-- 1. 現在のマイグレーション状況確認
SELECT version, description, installed_on, success 
FROM flyway_schema_history 
ORDER BY installed_rank DESC 
LIMIT 10;

-- 2. usersテーブルの現在の構造確認
\d users

-- 3. 既存ユーザー数とデータ確認
SELECT 
    COUNT(*) as total_users,
    COUNT(CASE WHEN calorie_goal IS NOT NULL THEN 1 END) as users_with_goals
FROM users;

-- 4. マイグレーション実行後の確認クエリ
-- （マイグレーション実行後に以下を実行）

-- user_goalsテーブルの構造確認
-- \d user_goals

-- データ移行確認
-- SELECT 
--     u.email,
--     u.username,
--     ug.daily_calorie_goal,
--     ug.protein_goal_g,
--     ug.fat_goal_g,
--     ug.net_carbs_goal_g,
--     ug.is_active,
--     ug.effective_date
-- FROM users u
-- JOIN user_goals ug ON u.id = ug.user_id
-- WHERE u.email LIKE '%example.com'
-- ORDER BY u.email, ug.effective_date DESC;

-- アクティブな目標の一意性確認
-- SELECT 
--     user_id,
--     COUNT(*) as active_goals_count
-- FROM user_goals 
-- WHERE is_active = true
-- GROUP BY user_id
-- HAVING COUNT(*) > 1;

-- マイグレーション完了確認
-- SELECT version, description, installed_on, success 
-- FROM flyway_schema_history 
-- WHERE version IN ('011', '012', '013', '014')
-- ORDER BY version;
