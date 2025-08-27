-- usersテーブルの構造変更
-- 栄養目標関連カラムを削除（user_goalsテーブルに分離するため）
-- 設計書: docs/2_detail/02_database.md および auth_context_design.md に準拠

-- 1. 既存データのバックアップ（一時テーブル作成）
CREATE TABLE IF NOT EXISTS temp_user_goals_backup AS
SELECT 
    id as user_id,
    calorie_goal as daily_calorie_goal,
    protein_goal_g,
    fat_goal_g,
    net_carbs_goal_g,
    created_at,
    updated_at
FROM users 
WHERE calorie_goal IS NOT NULL;

-- 2. usersテーブルから栄養目標カラムを削除
ALTER TABLE users DROP COLUMN IF EXISTS calorie_goal;
ALTER TABLE users DROP COLUMN IF EXISTS protein_goal_g;
ALTER TABLE users DROP COLUMN IF EXISTS fat_goal_g;
ALTER TABLE users DROP COLUMN IF EXISTS net_carbs_goal_g;

-- 3. 削除された制約も自動的に削除される
-- users_calorie_goal_positive, users_protein_goal_positive, users_fat_goal_positive, users_net_carbs_goal_positive

-- テーブルコメントの更新
COMMENT ON TABLE users IS 'ユーザーマスタテーブル - 認証情報とプロフィールを管理（栄養目標はuser_goalsテーブルに分離）';
