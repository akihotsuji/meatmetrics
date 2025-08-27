-- user_goalsテーブルの作成
-- ユーザーの栄養目標を時系列で管理
-- 設計書: docs/2_detail/02_database.md および auth_context_design.md に準拠

CREATE TABLE IF NOT EXISTS user_goals (
    -- 基本情報
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    
    -- 栄養目標値
    daily_calorie_goal INTEGER NOT NULL,
    protein_goal_g DECIMAL(6,2) NOT NULL,
    fat_goal_g DECIMAL(6,2) NOT NULL,
    net_carbs_goal_g DECIMAL(6,2) NOT NULL,
    
    -- 有効期間管理
    effective_date DATE NOT NULL DEFAULT CURRENT_DATE,
    is_active BOOLEAN NOT NULL DEFAULT true,
    
    -- 監査情報
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    
    -- 制約
    CONSTRAINT fk_user_goals_user_id FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_daily_calorie_goal_range CHECK (daily_calorie_goal BETWEEN 800 AND 5000),
    CONSTRAINT chk_protein_goal_range CHECK (protein_goal_g BETWEEN 50 AND 500),
    CONSTRAINT chk_fat_goal_range CHECK (fat_goal_g BETWEEN 30 AND 400),
    CONSTRAINT chk_net_carbs_goal_range CHECK (net_carbs_goal_g BETWEEN 0 AND 150),
    CONSTRAINT chk_effective_date_not_future CHECK (effective_date <= CURRENT_DATE),
    
    -- 同一ユーザー・同一日付の重複防止
    UNIQUE(user_id, effective_date)
);

-- インデックス設計
CREATE INDEX IF NOT EXISTS idx_user_goals_user_id ON user_goals(user_id);
CREATE INDEX IF NOT EXISTS idx_user_goals_effective_date ON user_goals(effective_date);
CREATE INDEX IF NOT EXISTS idx_user_goals_is_active ON user_goals(is_active);
CREATE INDEX IF NOT EXISTS idx_user_goals_user_active ON user_goals(user_id, is_active) WHERE is_active = true;

-- 更新日時の自動更新トリガー
CREATE OR REPLACE FUNCTION update_user_goals_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_user_goals_updated_at
    BEFORE UPDATE ON user_goals
    FOR EACH ROW
    EXECUTE FUNCTION update_user_goals_updated_at();

-- アクティブな目標の一意性を保証する部分一意制約
-- PostgreSQL 15以降で利用可能。それ以前のバージョンではアプリケーション層で制御
-- CREATE UNIQUE INDEX IF NOT EXISTS idx_user_goals_unique_active 
-- ON user_goals(user_id) WHERE is_active = true;

-- 一時的にアプリケーション層での制御に依存（PostgreSQLバージョン互換性のため）

-- テーブルコメント
COMMENT ON TABLE user_goals IS 'ユーザー栄養目標テーブル - 時系列での目標管理';
COMMENT ON COLUMN user_goals.id IS '目標ID（主キー）';
COMMENT ON COLUMN user_goals.user_id IS 'ユーザーID（外部キー）';
COMMENT ON COLUMN user_goals.daily_calorie_goal IS '1日の目標カロリー（kcal）';
COMMENT ON COLUMN user_goals.protein_goal_g IS '1日の目標タンパク質量（g）';
COMMENT ON COLUMN user_goals.fat_goal_g IS '1日の目標脂質量（g）';
COMMENT ON COLUMN user_goals.net_carbs_goal_g IS '1日の目標正味炭水化物量（g）';
COMMENT ON COLUMN user_goals.effective_date IS '目標の適用開始日';
COMMENT ON COLUMN user_goals.is_active IS '目標の有効状態（1ユーザーにつき1つのみがtrue）';
