-- テスト用user_profilesデータの挿入
-- 開発・テスト環境専用
-- 既存のテストユーザーに対応するプロフィールデータを作成

-- テストユーザー1用のプロフィール（一般的なカーニボア実践者）
INSERT INTO user_profiles (
    user_id,
    first_name,
    last_name,
    display_name,
    date_of_birth,
    gender,
    height_cm,
    weight_kg,
    activity_level,
    carnivore_start_date,
    carnivore_level,
    carnivore_goal,
    measurement_unit,
    timezone,
    created_at,
    updated_at
) VALUES (
    (SELECT id FROM users WHERE email = 'test1@example.com'),
    '太郎',
    '山田',
    'カーニボア太郎',
    '1985-05-15',
    'male',
    175.00,
    70.00,
    'moderately_active',
    '2024-01-01',
    'intermediate',
    'health_improvement',
    'metric',
    'Asia/Tokyo',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (user_id) DO UPDATE SET
    first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    display_name = EXCLUDED.display_name,
    date_of_birth = EXCLUDED.date_of_birth,
    gender = EXCLUDED.gender,
    height_cm = EXCLUDED.height_cm,
    weight_kg = EXCLUDED.weight_kg,
    activity_level = EXCLUDED.activity_level,
    carnivore_start_date = EXCLUDED.carnivore_start_date,
    carnivore_level = EXCLUDED.carnivore_level,
    carnivore_goal = EXCLUDED.carnivore_goal,
    measurement_unit = EXCLUDED.measurement_unit,
    timezone = EXCLUDED.timezone,
    updated_at = CURRENT_TIMESTAMP;

-- テストユーザー2用のプロフィール（上級カーニボア実践者）
INSERT INTO user_profiles (
    user_id,
    first_name,
    last_name,
    display_name,
    date_of_birth,
    gender,
    height_cm,
    weight_kg,
    activity_level,
    carnivore_start_date,
    carnivore_level,
    carnivore_goal,
    measurement_unit,
    timezone,
    created_at,
    updated_at
) VALUES (
    (SELECT id FROM users WHERE email = 'test2@example.com'),
    '花子',
    '佐藤',
    'カーニボア花子',
    '1990-08-22',
    'female',
    162.00,
    58.00,
    'very_active',
    '2023-06-01',
    'advanced',
    'weight_loss',
    'metric',
    'Asia/Tokyo',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (user_id) DO UPDATE SET
    first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    display_name = EXCLUDED.display_name,
    date_of_birth = EXCLUDED.date_of_birth,
    gender = EXCLUDED.gender,
    height_cm = EXCLUDED.height_cm,
    weight_kg = EXCLUDED.weight_kg,
    activity_level = EXCLUDED.activity_level,
    carnivore_start_date = EXCLUDED.carnivore_start_date,
    carnivore_level = EXCLUDED.carnivore_level,
    carnivore_goal = EXCLUDED.carnivore_goal,
    measurement_unit = EXCLUDED.measurement_unit,
    timezone = EXCLUDED.timezone,
    updated_at = CURRENT_TIMESTAMP;

-- テストユーザー3用のプロフィール（初心者カーニボア実践者）
INSERT INTO user_profiles (
    user_id,
    first_name,
    last_name,
    display_name,
    date_of_birth,
    gender,
    height_cm,
    weight_kg,
    activity_level,
    carnivore_start_date,
    carnivore_level,
    carnivore_goal,
    measurement_unit,
    timezone,
    created_at,
    updated_at
) VALUES (
    (SELECT id FROM users WHERE email = 'test3@example.com'),
    '次郎',
    '田中',
    'ビギナー次郎',
    '1992-12-03',
    'male',
    180.00,
    85.00,
    'lightly_active',
    '2024-11-01',
    'beginner',
    'weight_gain',
    'metric',
    'Asia/Tokyo',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (user_id) DO UPDATE SET
    first_name = EXCLUDED.first_name,
    last_name = EXCLUDED.last_name,
    display_name = EXCLUDED.display_name,
    date_of_birth = EXCLUDED.date_of_birth,
    gender = EXCLUDED.gender,
    height_cm = EXCLUDED.height_cm,
    weight_kg = EXCLUDED.weight_kg,
    activity_level = EXCLUDED.activity_level,
    carnivore_start_date = EXCLUDED.carnivore_start_date,
    carnivore_level = EXCLUDED.carnivore_level,
    carnivore_goal = EXCLUDED.carnivore_goal,
    measurement_unit = EXCLUDED.measurement_unit,
    timezone = EXCLUDED.timezone,
    updated_at = CURRENT_TIMESTAMP;

-- 一部項目のみのプロフィールテスト（最小限データ）
INSERT INTO user_profiles (
    user_id,
    display_name,
    carnivore_level,
    created_at,
    updated_at
) 
SELECT 
    u.id,
    'ミニマルユーザー',
    'beginner',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
FROM users u 
WHERE u.email NOT IN ('test1@example.com', 'test2@example.com', 'test3@example.com')
AND u.id <= 10  -- 最初の10ユーザーのみ
ON CONFLICT (user_id) DO NOTHING;

-- データ挿入確認用ログ
DO $$
DECLARE
    profile_count INTEGER;
    complete_profile_count INTEGER;
    minimal_profile_count INTEGER;
BEGIN
    SELECT COUNT(*) INTO profile_count FROM user_profiles;
    
    SELECT COUNT(*) INTO complete_profile_count 
    FROM user_profiles 
    WHERE first_name IS NOT NULL 
      AND last_name IS NOT NULL 
      AND date_of_birth IS NOT NULL;
      
    SELECT COUNT(*) INTO minimal_profile_count 
    FROM user_profiles 
    WHERE first_name IS NULL 
      AND last_name IS NULL;
    
    RAISE NOTICE '=== Test User Profiles Data Insertion Complete ===';
    RAISE NOTICE 'Total user profiles: %', profile_count;
    RAISE NOTICE 'Complete profiles: %', complete_profile_count;
    RAISE NOTICE 'Minimal profiles: %', minimal_profile_count;
    RAISE NOTICE 'Test data insertion completed successfully.';
END $$;
