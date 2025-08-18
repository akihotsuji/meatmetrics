-- 食事記録のサンプルデータ投入
-- 開発・テスト環境向けのサンプルデータ
-- 既存のユーザーと食材データを使用

-- サンプル食事記録の投入
-- user_id=1 の過去1週間分の食事データ

-- 1週間前（月曜日）
INSERT INTO meals (user_id, meal_date, meal_type, total_calories, total_protein_g, total_fat_g, total_net_carbs_g, notes) VALUES
(1, CURRENT_DATE - INTERVAL '7 days', 'BREAKFAST', 450, 35.2, 28.5, 2.1, '朝食：牛ステーキとゆで卵'),
(1, CURRENT_DATE - INTERVAL '7 days', 'LUNCH', 520, 42.8, 35.6, 1.8, '昼食：鶏もも肉とチーズ'),
(1, CURRENT_DATE - INTERVAL '7 days', 'DINNER', 680, 48.5, 52.3, 3.2, '夕食：豚バラ肉と卵焼き');

-- 6日前（火曜日）
INSERT INTO meals (user_id, meal_date, meal_type, total_calories, total_protein_g, total_fat_g, total_net_carbs_g, notes) VALUES
(1, CURRENT_DATE - INTERVAL '6 days', 'BREAKFAST', 380, 28.6, 25.4, 1.5, '朝食：ゆで卵とバター'),
(1, CURRENT_DATE - INTERVAL '6 days', 'LUNCH', 590, 45.2, 38.9, 2.8, '昼食：牛ひき肉炒め'),
(1, CURRENT_DATE - INTERVAL '6 days', 'DINNER', 720, 52.3, 58.7, 4.1, '夕食：サーモンとアボカド'),
(1, CURRENT_DATE - INTERVAL '6 days', 'SNACK', 180, 12.5, 14.2, 0.8, '間食：チーズ');

-- 5日前（水曜日）
INSERT INTO meals (user_id, meal_date, meal_type, total_calories, total_protein_g, total_fat_g, total_net_carbs_g, notes) VALUES
(1, CURRENT_DATE - INTERVAL '5 days', 'BREAKFAST', 420, 32.1, 28.9, 1.9, '朝食：鶏胸肉とスクランブルエッグ'),
(1, CURRENT_DATE - INTERVAL '5 days', 'LUNCH', 480, 38.5, 32.6, 2.3, '昼食：豚ロース肉'),
(1, CURRENT_DATE - INTERVAL '5 days', 'DINNER', 640, 46.8, 48.2, 3.5, '夕食：牛カルビと野菜炒め');

-- 4日前（木曜日）
INSERT INTO meals (user_id, meal_date, meal_type, total_calories, total_protein_g, total_fat_g, total_net_carbs_g, notes) VALUES
(1, CURRENT_DATE - INTERVAL '4 days', 'BREAKFAST', 350, 26.4, 23.8, 1.2, '朝食：目玉焼きとベーコン'),
(1, CURRENT_DATE - INTERVAL '4 days', 'LUNCH', 610, 48.9, 41.2, 3.1, '昼食：豚バラ肉の塩焼き'),
(1, CURRENT_DATE - INTERVAL '4 days', 'DINNER', 580, 44.6, 42.5, 2.7, '夕食：鶏もも肉のグリル'),
(1, CURRENT_DATE - INTERVAL '4 days', 'SNACK', 150, 10.2, 11.8, 0.5, '間食：ナッツ');

-- 3日前（金曜日）
INSERT INTO meals (user_id, meal_date, meal_type, total_calories, total_protein_g, total_fat_g, total_net_carbs_g, notes) VALUES
(1, CURRENT_DATE - INTERVAL '3 days', 'BREAKFAST', 390, 29.8, 26.7, 1.6, '朝食：牛ステーキ'),
(1, CURRENT_DATE - INTERVAL '3 days', 'LUNCH', 550, 43.2, 37.8, 2.5, '昼食：サーモン刺身'),
(1, CURRENT_DATE - INTERVAL '3 days', 'DINNER', 700, 51.5, 56.2, 4.0, '夕食：豚カツ（衣なし）');

-- 2日前（土曜日）
INSERT INTO meals (user_id, meal_date, meal_type, total_calories, total_protein_g, total_fat_g, total_net_carbs_g, notes) VALUES
(1, CURRENT_DATE - INTERVAL '2 days', 'BREAKFAST', 460, 34.5, 31.2, 2.0, '朝食：オムレツ'),
(1, CURRENT_DATE - INTERVAL '2 days', 'LUNCH', 620, 47.8, 43.5, 3.2, '昼食：牛カルビ焼肉'),
(1, CURRENT_DATE - INTERVAL '2 days', 'DINNER', 590, 45.2, 41.8, 2.9, '夕食：鶏もも肉と野菜'),
(1, CURRENT_DATE - INTERVAL '2 days', 'SNACK', 200, 14.8, 16.2, 0.9, '間食：チーズとナッツ');

-- 1日前（日曜日）
INSERT INTO meals (user_id, meal_date, meal_type, total_calories, total_protein_g, total_fat_g, total_net_carbs_g, notes) VALUES
(1, CURRENT_DATE - INTERVAL '1 day', 'BREAKFAST', 380, 28.9, 25.8, 1.4, '朝食：ゆで卵とハム'),
(1, CURRENT_DATE - INTERVAL '1 day', 'LUNCH', 580, 44.6, 39.2, 2.8, '昼食：豚ロース肉のソテー'),
(1, CURRENT_DATE - INTERVAL '1 day', 'DINNER', 650, 48.7, 49.5, 3.6, '夕食：牛ひき肉ハンバーグ');

-- 今日
INSERT INTO meals (user_id, meal_date, meal_type, total_calories, total_protein_g, total_fat_g, total_net_carbs_g, notes) VALUES
(1, CURRENT_DATE, 'BREAKFAST', 400, 30.5, 27.2, 1.7, '朝食：スクランブルエッグとベーコン');

-- user_id=2 の直近3日分のデータ
INSERT INTO meals (user_id, meal_date, meal_type, total_calories, total_protein_g, total_fat_g, total_net_carbs_g, notes) VALUES
(2, CURRENT_DATE - INTERVAL '2 days', 'BREAKFAST', 420, 32.8, 28.9, 1.8, 'ユーザー2の朝食'),
(2, CURRENT_DATE - INTERVAL '2 days', 'LUNCH', 560, 43.5, 38.2, 2.6, 'ユーザー2の昼食'),
(2, CURRENT_DATE - INTERVAL '2 days', 'DINNER', 680, 49.8, 52.1, 3.4, 'ユーザー2の夕食'),
(2, CURRENT_DATE - INTERVAL '1 day', 'BREAKFAST', 380, 29.2, 25.6, 1.5, 'ユーザー2の朝食'),
(2, CURRENT_DATE - INTERVAL '1 day', 'LUNCH', 540, 41.8, 36.7, 2.4, 'ユーザー2の昼食'),
(2, CURRENT_DATE - INTERVAL '1 day', 'DINNER', 620, 46.5, 47.8, 3.1, 'ユーザー2の夕食'),
(2, CURRENT_DATE, 'BREAKFAST', 390, 30.1, 26.4, 1.6, 'ユーザー2の朝食');

-- user_id=1の食事詳細データ（最新の朝食分）
DO $$
DECLARE
    breakfast_meal_id BIGINT;
    beef_id BIGINT;
    egg_id BIGINT;
    butter_id BIGINT;
BEGIN
    -- 今日の朝食のIDを取得
    SELECT id INTO breakfast_meal_id 
    FROM meals 
    WHERE user_id = 1 AND meal_date = CURRENT_DATE AND meal_type = 'BREAKFAST';
    
    -- 食材IDを取得（存在する場合のみ）
    SELECT id INTO beef_id FROM foods WHERE name LIKE '%牛%' LIMIT 1;
    SELECT id INTO egg_id FROM foods WHERE name LIKE '%卵%' LIMIT 1;
    SELECT id INTO butter_id FROM foods WHERE name LIKE '%バター%' LIMIT 1;
    
    -- 食事詳細を追加
    IF breakfast_meal_id IS NOT NULL THEN
        -- 牛ステーキ 150g
        IF beef_id IS NOT NULL THEN
            INSERT INTO meal_items (meal_id, food_id, quantity_g, notes) 
            VALUES (breakfast_meal_id, beef_id, 150.00, '牛ステーキ');
        END IF;
        
        -- 卵 2個（約100g）
        IF egg_id IS NOT NULL THEN
            INSERT INTO meal_items (meal_id, food_id, quantity_g, notes) 
            VALUES (breakfast_meal_id, egg_id, 100.00, 'スクランブルエッグ');
        END IF;
        
        -- バター 10g
        IF butter_id IS NOT NULL THEN
            INSERT INTO meal_items (meal_id, food_id, quantity_g, notes) 
            VALUES (breakfast_meal_id, butter_id, 10.00, '調理用バター');
        END IF;
    END IF;
END $$;

-- 統計情報の更新
ANALYZE meals;
ANALYZE meal_items;

-- 投入データの確認用クエリ（コメントアウト）
/*
-- 食事記録の件数確認
SELECT 
    user_id,
    COUNT(*) as meal_count,
    MIN(meal_date) as earliest_date,
    MAX(meal_date) as latest_date
FROM meals 
GROUP BY user_id 
ORDER BY user_id;

-- 食事タイプ別の件数確認
SELECT 
    meal_type,
    COUNT(*) as count,
    AVG(total_calories) as avg_calories,
    AVG(total_protein_g) as avg_protein
FROM meals 
GROUP BY meal_type 
ORDER BY meal_type;

-- 食事詳細の件数確認
SELECT 
    COUNT(*) as meal_item_count,
    COUNT(DISTINCT meal_id) as distinct_meals,
    COUNT(DISTINCT food_id) as distinct_foods
FROM meal_items;
*/
