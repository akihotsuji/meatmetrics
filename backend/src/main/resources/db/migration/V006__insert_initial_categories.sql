-- 初期カテゴリデータの投入
-- カーニボア向けの階層構造（肉類、魚類、卵・乳製品等）
-- 設計書の food_categories テーブル構造を参考

-- ルートカテゴリ（Level 1）
INSERT INTO categories (name, display_name, description, sort_order, icon_name, level)
VALUES 
('meat', '肉類', '牛肉、豚肉、鶏肉、羊肉などの食肉類', 1, 'meat', 1),
('fish', '魚介類', '魚類、貝類、甲殻類などの魚介類', 2, 'fish', 1),
('eggs', '卵類', '鶏卵、うずら卵などの卵類', 3, 'egg', 1),
('dairy', '乳製品', 'チーズ、バター、生クリームなどの乳製品', 4, 'milk', 1),
('seasonings', '調味料・香辛料', '塩、胡椒、ハーブなどの調味料', 5, 'spice', 1),
('offal', '内臓', 'レバー、ハート、タンなどの内臓類', 6, 'organ', 1)
ON CONFLICT (parent_id, name) DO NOTHING;

-- 肉類のサブカテゴリ（Level 2）
INSERT INTO categories (name, display_name, parent_id, description, sort_order, icon_name)
VALUES 
('beef', '牛肉', (SELECT id FROM categories WHERE name = 'meat'), 'ビーフ、牛肉全般', 1, 'beef'),
('pork', '豚肉', (SELECT id FROM categories WHERE name = 'meat'), 'ポーク、豚肉全般', 2, 'pork'),
('chicken', '鶏肉', (SELECT id FROM categories WHERE name = 'meat'), 'チキン、鶏肉全般', 3, 'chicken'),
('lamb', '羊肉', (SELECT id FROM categories WHERE name = 'meat'), 'ラム、マトン、羊肉全般', 4, 'lamb'),
('game', 'ジビエ', (SELECT id FROM categories WHERE name = 'meat'), '鹿、猪、野鳥などのジビエ', 5, 'deer'),
('processed_meat', '加工肉', (SELECT id FROM categories WHERE name = 'meat'), 'ソーセージ、ベーコンなどの加工肉', 6, 'sausage')
ON CONFLICT (parent_id, name) DO NOTHING;

-- 魚介類のサブカテゴリ（Level 2）
INSERT INTO categories (name, display_name, parent_id, description, sort_order, icon_name)
VALUES 
('salmon', 'サケ類', (SELECT id FROM categories WHERE name = 'fish'), 'サーモン、サケ、マス類', 1, 'salmon'),
('tuna', 'マグロ類', (SELECT id FROM categories WHERE name = 'fish'), 'マグロ、ツナ類', 2, 'tuna'),
('white_fish', '白身魚', (SELECT id FROM categories WHERE name = 'fish'), 'タイ、ヒラメ、タラなどの白身魚', 3, 'fish'),
('blue_fish', '青魚', (SELECT id FROM categories WHERE name = 'fish'), 'サバ、イワシ、アジなどの青魚', 4, 'mackerel'),
('shellfish', '貝類', (SELECT id FROM categories WHERE name = 'fish'), '牡蠣、ホタテ、あさりなどの貝類', 5, 'shell'),
('crustacean', '甲殻類', (SELECT id FROM categories WHERE name = 'fish'), 'エビ、カニ、ロブスターなどの甲殻類', 6, 'crab'),
('canned_fish', '缶詰魚', (SELECT id FROM categories WHERE name = 'fish'), 'ツナ缶、サバ缶などの缶詰', 7, 'can')
ON CONFLICT (parent_id, name) DO NOTHING;

-- 卵類のサブカテゴリ（Level 2）
INSERT INTO categories (name, display_name, parent_id, description, sort_order, icon_name)
VALUES 
('chicken_eggs', '鶏卵', (SELECT id FROM categories WHERE name = 'eggs'), '鶏の卵', 1, 'egg'),
('quail_eggs', 'うずら卵', (SELECT id FROM categories WHERE name = 'eggs'), 'うずらの卵', 2, 'quail_egg'),
('duck_eggs', 'アヒル卵', (SELECT id FROM categories WHERE name = 'eggs'), 'アヒルの卵', 3, 'duck_egg')
ON CONFLICT (parent_id, name) DO NOTHING;

-- 乳製品のサブカテゴリ（Level 2）
INSERT INTO categories (name, display_name, parent_id, description, sort_order, icon_name)
VALUES 
('milk', '牛乳', (SELECT id FROM categories WHERE name = 'dairy'), '牛乳、生乳', 1, 'milk'),
('cheese', 'チーズ', (SELECT id FROM categories WHERE name = 'dairy'), 'ナチュラルチーズ、プロセスチーズ', 2, 'cheese'),
('butter', 'バター', (SELECT id FROM categories WHERE name = 'dairy'), 'バター、マーガリン', 3, 'butter'),
('cream', 'クリーム', (SELECT id FROM categories WHERE name = 'dairy'), '生クリーム、サワークリーム', 4, 'cream'),
('yogurt', 'ヨーグルト', (SELECT id FROM categories WHERE name = 'dairy'), 'ヨーグルト、発酵乳製品', 5, 'yogurt')
ON CONFLICT (parent_id, name) DO NOTHING;

-- 調味料のサブカテゴリ（Level 2）
INSERT INTO categories (name, display_name, parent_id, description, sort_order, icon_name)
VALUES 
('salt', '塩', (SELECT id FROM categories WHERE name = 'seasonings'), '塩、岩塩、海塩', 1, 'salt'),
('pepper', '胡椒', (SELECT id FROM categories WHERE name = 'seasonings'), 'ブラックペッパー、ホワイトペッパー', 2, 'pepper'),
('herbs', 'ハーブ', (SELECT id FROM categories WHERE name = 'seasonings'), 'ローズマリー、タイム、オレガノ等', 3, 'herb'),
('spices', 'スパイス', (SELECT id FROM categories WHERE name = 'seasonings'), 'クミン、コリアンダー、パプリカ等', 4, 'spice'),
('animal_fat', '動物性油脂', (SELECT id FROM categories WHERE name = 'seasonings'), 'ラード、牛脂、鴨脂等', 5, 'lard')
ON CONFLICT (parent_id, name) DO NOTHING;

-- 内臓のサブカテゴリ（Level 2）
INSERT INTO categories (name, display_name, parent_id, description, sort_order, icon_name)
VALUES 
('liver', 'レバー', (SELECT id FROM categories WHERE name = 'offal'), '肝臓（牛、豚、鶏）', 1, 'liver'),
('heart', 'ハート', (SELECT id FROM categories WHERE name = 'offal'), '心臓（牛、豚、鶏）', 2, 'heart'),
('tongue', 'タン', (SELECT id FROM categories WHERE name = 'offal'), '舌（牛、豚）', 3, 'tongue'),
('kidney', 'キドニー', (SELECT id FROM categories WHERE name = 'offal'), '腎臓（牛、豚、羊）', 4, 'kidney'),
('brain', 'ブレイン', (SELECT id FROM categories WHERE name = 'offal'), '脳（牛、豚）', 5, 'brain')
ON CONFLICT (parent_id, name) DO NOTHING;

-- 牛肉の詳細カテゴリ（Level 3）
INSERT INTO categories (name, display_name, parent_id, description, sort_order, icon_name)
VALUES 
('beef_steak', 'ステーキ用', (SELECT id FROM categories WHERE name = 'beef'), 'サーロイン、ヒレ、リブロース等', 1, 'steak'),
('beef_roast', 'ローストビーフ用', (SELECT id FROM categories WHERE name = 'beef'), 'もも、うち、そと等', 2, 'roast'),
('beef_ground', '挽肉', (SELECT id FROM categories WHERE name = 'beef'), '牛挽肉、合挽肉', 3, 'ground_beef'),
('beef_short_ribs', 'ショートリブ', (SELECT id FROM categories WHERE name = 'beef'), 'カルビ、ショートリブ', 4, 'ribs')
ON CONFLICT (parent_id, name) DO NOTHING;

-- 豚肉の詳細カテゴリ（Level 3）
INSERT INTO categories (name, display_name, parent_id, description, sort_order, icon_name)
VALUES 
('pork_chops', 'ポークチョップ', (SELECT id FROM categories WHERE name = 'pork'), 'ロース、ヒレ', 1, 'pork_chop'),
('pork_belly', 'ポークベリー', (SELECT id FROM categories WHERE name = 'pork'), 'バラ肉、三枚肉', 2, 'pork_belly'),
('pork_shoulder', 'ポークショルダー', (SELECT id FROM categories WHERE name = 'pork'), '肩ロース、肩肉', 3, 'pork_shoulder'),
('pork_ground', '豚挽肉', (SELECT id FROM categories WHERE name = 'pork'), '豚挽肉', 4, 'ground_pork')
ON CONFLICT (parent_id, name) DO NOTHING;

-- 統計情報更新
ANALYZE categories;
