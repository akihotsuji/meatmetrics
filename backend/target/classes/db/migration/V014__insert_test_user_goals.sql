-- テスト用user_goalsデータの挿入
-- 開発・テスト環境専用
-- 既存のテストユーザーに対応する目標データを作成

-- テストユーザー1用の栄養目標（一般的な目標）
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
) VALUES (
    (SELECT id FROM users WHERE email = 'test1@example.com'),
    2000,
    150.00,
    120.00,
    20.00,
    CURRENT_DATE,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (user_id, effective_date) DO UPDATE SET
    daily_calorie_goal = EXCLUDED.daily_calorie_goal,
    protein_goal_g = EXCLUDED.protein_goal_g,
    fat_goal_g = EXCLUDED.fat_goal_g,
    net_carbs_goal_g = EXCLUDED.net_carbs_goal_g,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;

-- テストユーザー2用の栄養目標（高タンパク・低糖質）
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
) VALUES (
    (SELECT id FROM users WHERE email = 'test2@example.com'),
    2500,
    200.00,
    180.00,
    10.00,
    CURRENT_DATE,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (user_id, effective_date) DO UPDATE SET
    daily_calorie_goal = EXCLUDED.daily_calorie_goal,
    protein_goal_g = EXCLUDED.protein_goal_g,
    fat_goal_g = EXCLUDED.fat_goal_g,
    net_carbs_goal_g = EXCLUDED.net_carbs_goal_g,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;

-- テストユーザー3用の栄養目標（軽量目標）  
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
) VALUES (
    (SELECT id FROM users WHERE email = 'test3@example.com'),
    1500,
    100.00,
    80.00,
    30.00,
    CURRENT_DATE,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (user_id, effective_date) DO UPDATE SET
    daily_calorie_goal = EXCLUDED.daily_calorie_goal,
    protein_goal_g = EXCLUDED.protein_goal_g,
    fat_goal_g = EXCLUDED.fat_goal_g,
    net_carbs_goal_g = EXCLUDED.net_carbs_goal_g,
    is_active = EXCLUDED.is_active,
    updated_at = CURRENT_TIMESTAMP;

-- 目標履歴のテストデータ（過去の目標設定例）
-- テストユーザー1の過去目標（1ヶ月前）
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
) VALUES (
    (SELECT id FROM users WHERE email = 'test1@example.com'),
    1800,
    130.00,
    100.00,
    25.00,
    CURRENT_DATE - INTERVAL '30 days',
    false,  -- 過去の目標なので非アクティブ
    CURRENT_TIMESTAMP - INTERVAL '30 days',
    CURRENT_TIMESTAMP - INTERVAL '30 days'
) ON CONFLICT (user_id, effective_date) DO NOTHING;

-- データ挿入確認用ログ
DO $$
DECLARE
    goal_count INTEGER;
    active_goal_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO goal_count FROM user_goals;
    SELECT COUNT(*) INTO active_goal_count FROM user_goals WHERE is_active = true;
    
    RAISE NOTICE '=== Test User Goals Data Insertion Complete ===';
    RAISE NOTICE 'Total user goals: %', goal_count;
    RAISE NOTICE 'Active user goals: %', active_goal_count;
    RAISE NOTICE 'Test data insertion completed successfully.';
END $$;
