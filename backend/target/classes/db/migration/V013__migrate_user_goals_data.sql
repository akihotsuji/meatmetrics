-- 既存ユーザーの栄養目標データをuser_goalsテーブルに移行
-- temp_user_goals_backupテーブルからデータを移行

-- 既存ユーザーの栄養目標データを新しいuser_goalsテーブルに移行
INSERT INTO user_goals (
    user_id,
    daily_calorie_goal,
    protein_goal_g,
    fat_goal_g,
    net_carbs_goal_g,
    effective_date,
    is_active,
    created_at,
    updated_at
)
SELECT 
    user_id,
    daily_calorie_goal,
    protein_goal_g,
    fat_goal_g,
    net_carbs_goal_g,
    CURRENT_DATE as effective_date,  -- 既存データは本日から有効とする
    true as is_active,               -- 既存データはすべてアクティブとする
    created_at,
    updated_at
FROM temp_user_goals_backup
WHERE user_id IS NOT NULL
  AND daily_calorie_goal IS NOT NULL
  AND protein_goal_g IS NOT NULL
  AND fat_goal_g IS NOT NULL
  AND net_carbs_goal_g IS NOT NULL
ON CONFLICT (user_id, effective_date) DO NOTHING;

-- バックアップテーブルの削除
DROP TABLE IF EXISTS temp_user_goals_backup;

-- データ移行の確認用ログ出力（PostgreSQLのRAISE NOTICEを使用）
DO $$
DECLARE
    migrated_count INTEGER;
    user_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO migrated_count FROM user_goals;
    SELECT COUNT(*) INTO user_count FROM users;
    
    RAISE NOTICE '=== User Goals Data Migration Complete ===';
    RAISE NOTICE 'Total users: %', user_count;
    RAISE NOTICE 'Migrated user goals: %', migrated_count;
    RAISE NOTICE 'Migration completed successfully.';
END $$;
