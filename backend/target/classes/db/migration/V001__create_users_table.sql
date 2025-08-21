-- ユーザーテーブル（users）の作成
-- MVP段階での基本情報と栄養目標を含む
-- 設計書：docs/2_detail/02_database.md に準拠
-- タスク要件：基本情報 + 栄養目標をusersテーブルに格納

CREATE TABLE IF NOT EXISTS users (
    -- 基本情報（タスク要件に準拠）
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- 栄養目標（タスク要件：usersテーブル内に格納）
    calorie_goal INTEGER DEFAULT 2000,
    protein_goal_g DECIMAL(6,2) DEFAULT 150.00,
    fat_goal_g DECIMAL(6,2) DEFAULT 120.00,
    net_carbs_goal_g DECIMAL(6,2) DEFAULT 20.00,
    
    -- バリデーション制約
    CONSTRAINT users_email_format CHECK (email ~* '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$'),
    CONSTRAINT users_username_length CHECK (char_length(username) >= 3 AND char_length(username) <= 30),
    CONSTRAINT users_calorie_goal_positive CHECK (calorie_goal > 0 AND calorie_goal <= 10000),
    CONSTRAINT users_protein_goal_positive CHECK (protein_goal_g >= 0 AND protein_goal_g <= 1000),
    CONSTRAINT users_fat_goal_positive CHECK (fat_goal_g >= 0 AND fat_goal_g <= 1000),
    CONSTRAINT users_net_carbs_goal_positive CHECK (net_carbs_goal_g >= 0 AND net_carbs_goal_g <= 1000)
);

-- インデックス設計
-- email の unique インデックス（既にUNIQUE制約で作成済み）
-- username の unique インデックス（既にUNIQUE制約で作成済み）

-- パフォーマンス向上のための追加インデックス
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_users_created_at ON users(created_at);

-- 更新日時の自動更新トリガー
CREATE OR REPLACE FUNCTION update_users_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW
    EXECUTE FUNCTION update_users_updated_at();

-- テーブルコメント
COMMENT ON TABLE users IS 'ユーザーマスタテーブル - 認証情報と栄養目標を管理';
COMMENT ON COLUMN users.id IS 'ユーザーID（主キー）';
COMMENT ON COLUMN users.email IS 'メールアドレス（ログイン用、重複不可）';
COMMENT ON COLUMN users.username IS 'ユーザー名（表示用、重複不可）';
COMMENT ON COLUMN users.password_hash IS 'パスワードハッシュ（Argon2id または BCrypt）';
COMMENT ON COLUMN users.calorie_goal IS '1日の目標カロリー（kcal）';
COMMENT ON COLUMN users.protein_goal_g IS '1日の目標タンパク質量（g）';
COMMENT ON COLUMN users.fat_goal_g IS '1日の目標脂質量（g）';
COMMENT ON COLUMN users.net_carbs_goal_g IS '1日の目標正味炭水化物量（g）';
