-- データベースマイグレーション実行スクリプト
-- 手動実行用（開発環境）
-- 
-- 実行方法:
-- 1. PostgreSQLに接続: psql -h localhost -p 15432 -U meatmetrics -d meatmetrics
-- 2. このファイルを実行: \i backend/scripts/db/run_migration.sql

-- 開始メッセージ
\echo '=== MeatMetrics Database Migration Start ==='
\echo 'Creating users and foods tables with initial data...'

-- V001: ユーザーテーブル作成
\echo 'Executing V001: Create users table...'
\i backend/src/main/resources/db/migration/V001__create_users_table.sql

-- V002: ユーザー初期データ投入
\echo 'Executing V002: Insert test users...'
\i backend/src/main/resources/db/migration/V002__insert_test_users.sql

-- V003: 食材テーブル作成
\echo 'Executing V003: Create foods table...'
\i backend/src/main/resources/db/migration/V003__create_foods_table.sql

-- V004: 食材初期データ投入
\echo 'Executing V004: Insert initial foods...'
\i backend/src/main/resources/db/migration/V004__insert_initial_foods.sql

-- V005: カテゴリテーブル作成
\echo 'Executing V005: Create categories table...'
\i backend/src/main/resources/db/migration/V005__create_categories_table.sql

-- V006: カテゴリ初期データ投入
\echo 'Executing V006: Insert initial categories...'
\i backend/src/main/resources/db/migration/V006__insert_initial_categories.sql

-- V007: 食材カテゴリ関連付け
\echo 'Executing V007: Add foods-categories relationship...'
\i backend/src/main/resources/db/migration/V007__add_foods_category_constraint.sql

-- 完了メッセージ
\echo '=== Migration Completed ==='
\echo 'Verifying results...'

-- 結果確認
SELECT 
    'users' as table_name,
    COUNT(*) as record_count
FROM users
UNION ALL
SELECT 
    'foods' as table_name,
    COUNT(*) as record_count
FROM foods
UNION ALL
SELECT 
    'categories' as table_name,
    COUNT(*) as record_count
FROM categories
ORDER BY table_name;

\echo 'All tables created successfully!'
\echo 'You can now run tests with:'
\echo '  - Users: \\i backend/src/main/resources/db/test/test_users_schema.sql'
\echo '  - Foods: \\i backend/src/main/resources/db/test/test_foods_schema.sql'
\echo '  - Categories: \\i backend/src/main/resources/db/test/test_categories_schema.sql'
