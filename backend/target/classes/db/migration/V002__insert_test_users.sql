-- 初期テストユーザーデータの投入
-- パスワードはすべて "password123" のBCryptハッシュ値
-- 開発・テスト環境専用

-- テストユーザー1: 一般的な栄養目標
INSERT INTO users (
    email, 
    username, 
    password_hash, 
    calorie_goal, 
    protein_goal_g, 
    fat_goal_g, 
    net_carbs_goal_g,
    created_at,
    updated_at
) VALUES (
    'test1@example.com',
    'testuser1',
    '$2a$12$LQv3c1yqBw/zK6z1WZj4XOX9XcHhQzC9kW1z2XwJx7E8X8.5YyWnC', -- password123
    2000,
    150.00,
    120.00,
    20.00,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- テストユーザー2: 高タンパク・低糖質目標
INSERT INTO users (
    email, 
    username, 
    password_hash, 
    calorie_goal, 
    protein_goal_g, 
    fat_goal_g, 
    net_carbs_goal_g,
    created_at,
    updated_at
) VALUES (
    'test2@example.com',
    'carnivore_master',
    '$2a$12$LQv3c1yqBw/zK6z1WZj4XOX9XcHhQzC9kW1z2XwJx7E8X8.5YyWnC', -- password123
    2500,
    200.00,
    180.00,
    10.00,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- テストユーザー3: 軽量目標
INSERT INTO users (
    email, 
    username, 
    password_hash, 
    calorie_goal, 
    protein_goal_g, 
    fat_goal_g, 
    net_carbs_goal_g,
    created_at,
    updated_at
) VALUES (
    'test3@example.com',
    'light_eater',
    '$2a$12$LQv3c1yqBw/zK6z1WZj4XOX9XcHhQzC9kW1z2XwJx7E8X8.5YyWnC', -- password123
    1500,
    100.00,
    80.00,
    30.00,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
) ON CONFLICT (email) DO NOTHING;

-- インデックス確認用コメント
-- email と username の UNIQUE 制約により、重複データは自動的に回避される
