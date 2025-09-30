-- ユーザープロフィールテーブル（user_profiles）の作成
-- DDD設計：User集約内のProfileエンティティに対応
-- 設計書：docs/2_detail/database_design.md 2.1.2 user_profiles 準拠

CREATE TABLE IF NOT EXISTS user_profiles (
    -- 基本情報
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    
    -- 個人情報
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    display_name VARCHAR(50),
    date_of_birth DATE,
    gender VARCHAR(20) CHECK (gender IN ('male', 'female', 'other', 'prefer_not_to_say')),
    
    -- 身体情報
    height_cm DECIMAL(5,2) CHECK (height_cm >= 100 AND height_cm <= 250),
    weight_kg DECIMAL(5,2) CHECK (weight_kg >= 30 AND weight_kg <= 200),
    activity_level VARCHAR(20) CHECK (activity_level IN ('sedentary', 'lightly_active', 'moderately_active', 'very_active', 'extremely_active')),
    
    -- カーニボア設定
    carnivore_start_date DATE,
    carnivore_level VARCHAR(20) DEFAULT 'beginner' CHECK (carnivore_level IN ('beginner', 'intermediate', 'advanced')),
    carnivore_goal VARCHAR(20) CHECK (carnivore_goal IN ('weight_loss', 'weight_gain', 'maintenance', 'health_improvement')),
    
    -- システム設定
    measurement_unit VARCHAR(10) DEFAULT 'metric' CHECK (measurement_unit IN ('metric', 'imperial')),
    timezone VARCHAR(50) DEFAULT 'UTC',
    avatar_url VARCHAR(255),
    
    -- 監査情報
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- 制約
    CONSTRAINT fk_user_profiles_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_date_of_birth_valid CHECK (date_of_birth IS NULL OR date_of_birth <= CURRENT_DATE),
    CONSTRAINT chk_carnivore_start_date_valid CHECK (carnivore_start_date IS NULL OR carnivore_start_date <= CURRENT_DATE),
    CONSTRAINT chk_display_name_length CHECK (display_name IS NULL OR (LENGTH(TRIM(display_name)) >= 3 AND LENGTH(TRIM(display_name)) <= 50)),
    CONSTRAINT chk_avatar_url_format CHECK (avatar_url IS NULL OR avatar_url ~* '^https?://.*'),
    
    -- 1ユーザーにつき1プロフィール
    UNIQUE(user_id)
);

-- インデックス設計
CREATE INDEX IF NOT EXISTS idx_user_profiles_user_id ON user_profiles(user_id);
CREATE INDEX IF NOT EXISTS idx_user_profiles_display_name ON user_profiles(display_name);
CREATE INDEX IF NOT EXISTS idx_user_profiles_carnivore_level ON user_profiles(carnivore_level);
CREATE INDEX IF NOT EXISTS idx_user_profiles_created_at ON user_profiles(created_at);

-- 更新日時の自動更新トリガー
CREATE OR REPLACE FUNCTION update_user_profiles_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_user_profiles_updated_at
    BEFORE UPDATE ON user_profiles
    FOR EACH ROW
    EXECUTE FUNCTION update_user_profiles_updated_at();

-- テーブルコメント
COMMENT ON TABLE user_profiles IS 'ユーザープロフィールテーブル - 個人情報とカーニボア設定を管理';
COMMENT ON COLUMN user_profiles.id IS 'プロフィールID（主キー）';
COMMENT ON COLUMN user_profiles.user_id IS 'ユーザーID（外部キー：users.id）';
COMMENT ON COLUMN user_profiles.first_name IS '姓';
COMMENT ON COLUMN user_profiles.last_name IS '名';
COMMENT ON COLUMN user_profiles.display_name IS '表示名（3-50文字）';
COMMENT ON COLUMN user_profiles.date_of_birth IS '生年月日';
COMMENT ON COLUMN user_profiles.gender IS '性別（male/female/other/prefer_not_to_say）';
COMMENT ON COLUMN user_profiles.height_cm IS '身長（cm）';
COMMENT ON COLUMN user_profiles.weight_kg IS '体重（kg）';
COMMENT ON COLUMN user_profiles.activity_level IS '活動レベル（sedentary〜extremely_active）';
COMMENT ON COLUMN user_profiles.carnivore_start_date IS 'カーニボア開始日';
COMMENT ON COLUMN user_profiles.carnivore_level IS 'カーニボアレベル（beginner/intermediate/advanced）';
COMMENT ON COLUMN user_profiles.carnivore_goal IS 'カーニボア目標（weight_loss/weight_gain/maintenance/health_improvement）';
COMMENT ON COLUMN user_profiles.measurement_unit IS '単位系（metric/imperial）';
COMMENT ON COLUMN user_profiles.timezone IS 'タイムゾーン';
COMMENT ON COLUMN user_profiles.avatar_url IS 'アバター画像URL';
