-- ユーザーテーブルのスキーマ検証とテスト用SQL
-- 開発環境での動作確認用

-- 1. テーブル構造の確認
SELECT 
    column_name, 
    data_type, 
    is_nullable, 
    column_default,
    character_maximum_length
FROM information_schema.columns 
WHERE table_name = 'users' 
  AND table_schema = 'public'
ORDER BY ordinal_position;

-- 2. 制約の確認
SELECT 
    tc.constraint_name,
    tc.constraint_type,
    kcu.column_name,
    cc.check_clause
FROM information_schema.table_constraints tc
JOIN information_schema.key_column_usage kcu 
    ON tc.constraint_name = kcu.constraint_name
LEFT JOIN information_schema.check_constraints cc 
    ON tc.constraint_name = cc.constraint_name
WHERE tc.table_name = 'users' 
  AND tc.table_schema = 'public'
ORDER BY tc.constraint_type, tc.constraint_name;

-- 3. インデックスの確認
SELECT 
    indexname,
    indexdef
FROM pg_indexes 
WHERE tablename = 'users' 
  AND schemaname = 'public'
ORDER BY indexname;

-- 4. バリデーションテスト（正常系）
-- 正常なユーザー作成テスト（基本情報のみ）
BEGIN;
INSERT INTO users (email, username, password_hash)
VALUES ('validation_test@example.com', 'validuser', 'hashedpassword123');

SELECT 'Test 1 PASSED: 正常なユーザー作成' as test_result;
ROLLBACK;

-- 5. バリデーションテスト（異常系）
-- 不正メールアドレステスト
BEGIN;
INSERT INTO users (email, username, password_hash)
VALUES ('invalid-email', 'testuser', 'hashedpassword123');
-- この操作は制約違反でエラーになるはず
ROLLBACK;

-- 6. UNIQUE制約テスト
-- 重複メールアドレステスト
BEGIN;
-- 既存のテストユーザーと同じメールアドレスで登録を試行
INSERT INTO users (email, username, password_hash)
VALUES ('test1@example.com', 'duplicateuser', 'hashedpassword123');
-- この操作は重複エラーになるはず
ROLLBACK;

-- 7. 文字列長制約テスト
-- ユーザー名長さの境界値テスト
BEGIN;
INSERT INTO users (email, username, password_hash)
VALUES ('boundary_test@example.com', 'ab', 'hashedpassword123');  -- 短すぎるユーザー名
-- この操作は制約違反でエラーになるはず
ROLLBACK;

-- 8. 登録済みユーザー数の確認
SELECT 
    COUNT(*) as total_users,
    COUNT(CASE WHEN email LIKE '%example.com' THEN 1 END) as test_users
FROM users;

-- 9. トリガー動作確認（updated_at自動更新）
BEGIN;
UPDATE users 
SET username = 'updated_testuser1' 
WHERE email = 'test1@example.com';

SELECT 
    email,
    username,
    created_at,
    updated_at,
    (updated_at > created_at) as trigger_working
FROM users 
WHERE email = 'test1@example.com';
ROLLBACK;
