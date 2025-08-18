# データベースマイグレーション手順

## 概要

ユーザーテーブル（users）、食材テーブル（foods）、カテゴリテーブル（categories）、食事記録テーブル（meals）、食事詳細テーブル（meal_items）のデータベーススキーマを作成するマイグレーション手順です。

## 前提条件

- Docker 環境で PostgreSQL が起動している
- データベース接続情報：
  - Host: localhost
  - Port: 15432
  - Database: meatmetrics
  - User: meatmetrics
  - Password: meatmetrics123

## マイグレーション実行手順

### 1. データベースへの接続

```bash
psql -h localhost -p 15432 -U meatmetrics -d meatmetrics
```

### 2. マイグレーション実行

```sql
\i backend/scripts/db/run_migration.sql
```

### 3. スキーマ検証

```sql
\i backend/src/main/resources/db/test/test_users_schema.sql
\i backend/src/main/resources/db/test/test_foods_schema.sql
\i backend/src/main/resources/db/test/test_categories_schema.sql
\i backend/src/main/resources/db/test/test_meals_schema.sql
```

## 作成されるテーブル

### users テーブル

| カラム名         | データ型                 | 制約                      | 説明               |
| ---------------- | ------------------------ | ------------------------- | ------------------ |
| id               | BIGSERIAL                | PRIMARY KEY               | ユーザー ID        |
| email            | VARCHAR(255)             | UNIQUE, NOT NULL          | メールアドレス     |
| username         | VARCHAR(100)             | UNIQUE, NOT NULL          | ユーザー名         |
| password_hash    | VARCHAR(255)             | NOT NULL                  | パスワードハッシュ |
| created_at       | TIMESTAMP WITH TIME ZONE | DEFAULT CURRENT_TIMESTAMP | 作成日時           |
| updated_at       | TIMESTAMP WITH TIME ZONE | DEFAULT CURRENT_TIMESTAMP | 更新日時           |
| calorie_goal     | INTEGER                  | DEFAULT 2000              | 目標カロリー       |
| protein_goal_g   | DECIMAL(6,2)             | DEFAULT 150.00            | 目標タンパク質量   |
| fat_goal_g       | DECIMAL(6,2)             | DEFAULT 120.00            | 目標脂質量         |
| net_carbs_goal_g | DECIMAL(6,2)             | DEFAULT 20.00             | 目標正味炭水化物量 |

## インデックス

- `users_pkey`: 主キー（id）
- `users_email_key`: email の UNIQUE インデックス
- `users_username_key`: username の UNIQUE インデックス
- `idx_users_email`: email 検索用
- `idx_users_username`: username 検索用
- `idx_users_created_at`: 作成日時ソート用

## バリデーション制約

- `users_email_format`: メールアドレス形式チェック
- `users_username_length`: ユーザー名長さチェック（3-30 文字）
- `users_calorie_goal_positive`: カロリー目標範囲チェック（1-10000）
- `users_protein_goal_positive`: タンパク質目標範囲チェック（0-1000g）
- `users_fat_goal_positive`: 脂質目標範囲チェック（0-1000g）
- `users_net_carbs_goal_positive`: 正味炭水化物目標範囲チェック（0-1000g）

### foods テーブル

| カラム名                 | データ型                 | 制約                      | 説明                      |
| ------------------------ | ------------------------ | ------------------------- | ------------------------- |
| id                       | BIGSERIAL                | PRIMARY KEY               | 食材 ID                   |
| name                     | VARCHAR(255)             | NOT NULL                  | 食材名                    |
| category_id              | BIGINT                   | NULL 許可                 | カテゴリ ID（将来拡張用） |
| created_at               | TIMESTAMP WITH TIME ZONE | DEFAULT CURRENT_TIMESTAMP | 作成日時                  |
| updated_at               | TIMESTAMP WITH TIME ZONE | DEFAULT CURRENT_TIMESTAMP | 更新日時                  |
| calories_per_100g        | INTEGER                  | NOT NULL                  | 100g あたりカロリー       |
| protein_g_per_100g       | DECIMAL(6,2)             | NOT NULL                  | 100g あたりタンパク質量   |
| fat_g_per_100g           | DECIMAL(6,2)             | NOT NULL                  | 100g あたり脂質量         |
| carbohydrates_g_per_100g | DECIMAL(6,2)             | NOT NULL                  | 100g あたり炭水化物量     |
| fiber_g_per_100g         | DECIMAL(6,2)             | DEFAULT 0.00              | 100g あたり食物繊維量     |
| tags                     | TEXT[]                   | DEFAULT '{}'              | タグ配列                  |
| description              | TEXT                     | NULL 許可                 | 食材の説明                |
| is_active                | BOOLEAN                  | DEFAULT true              | 有効フラグ                |

### categories テーブル

| カラム名     | データ型                 | 制約                      | 説明                     |
| ------------ | ------------------------ | ------------------------- | ------------------------ |
| id           | BIGSERIAL                | PRIMARY KEY               | カテゴリ ID              |
| name         | VARCHAR(100)             | NOT NULL                  | カテゴリ名（システム用） |
| parent_id    | BIGINT                   | FOREIGN KEY               | 親カテゴリ ID            |
| level        | INTEGER                  | NOT NULL, DEFAULT 1       | 階層レベル               |
| created_at   | TIMESTAMP WITH TIME ZONE | DEFAULT CURRENT_TIMESTAMP | 作成日時                 |
| display_name | VARCHAR(100)             | NOT NULL                  | 表示名（日本語）         |
| description  | TEXT                     | NULL 許可                 | カテゴリの説明           |
| sort_order   | INTEGER                  | DEFAULT 0                 | 表示順序                 |
| is_active    | BOOLEAN                  | DEFAULT true              | 有効フラグ               |
| icon_name    | VARCHAR(50)              | NULL 許可                 | アイコン名               |
| updated_at   | TIMESTAMP WITH TIME ZONE | DEFAULT CURRENT_TIMESTAMP | 更新日時                 |

### meals テーブル

| カラム名          | データ型                 | 制約                      | 説明                         |
| ----------------- | ------------------------ | ------------------------- | ---------------------------- |
| id                | BIGSERIAL                | PRIMARY KEY               | 食事記録 ID                  |
| user_id           | BIGINT                   | FOREIGN KEY, NOT NULL     | ユーザー ID                  |
| meal_date         | DATE                     | NOT NULL                  | 食事日付                     |
| meal_type         | VARCHAR(20)              | NOT NULL                  | 食事タイプ（朝昼夕間食）     |
| created_at        | TIMESTAMP WITH TIME ZONE | DEFAULT CURRENT_TIMESTAMP | 作成日時                     |
| updated_at        | TIMESTAMP WITH TIME ZONE | DEFAULT CURRENT_TIMESTAMP | 更新日時                     |
| total_calories    | INTEGER                  | DEFAULT 0                 | 合計カロリー（キャッシュ値） |
| total_protein_g   | DECIMAL(8,2)             | DEFAULT 0.00              | 合計タンパク質量（g）        |
| total_fat_g       | DECIMAL(8,2)             | DEFAULT 0.00              | 合計脂質量（g）              |
| total_net_carbs_g | DECIMAL(8,2)             | DEFAULT 0.00              | 合計正味炭水化物量（g）      |
| notes             | TEXT                     | NULL 許可                 | 食事メモ・備考               |
| is_deleted        | BOOLEAN                  | DEFAULT false             | 論理削除フラグ               |

### meal_items テーブル

| カラム名         | データ型                 | 制約                      | 説明                           |
| ---------------- | ------------------------ | ------------------------- | ------------------------------ |
| id               | BIGSERIAL                | PRIMARY KEY               | 食事詳細 ID                    |
| meal_id          | BIGINT                   | FOREIGN KEY, NOT NULL     | 食事記録 ID                    |
| food_id          | BIGINT                   | FOREIGN KEY, NOT NULL     | 食材 ID                        |
| quantity_g       | DECIMAL(8,2)             | NOT NULL                  | 摂取量（グラム）               |
| created_at       | TIMESTAMP WITH TIME ZONE | DEFAULT CURRENT_TIMESTAMP | 作成日時                       |
| updated_at       | TIMESTAMP WITH TIME ZONE | DEFAULT CURRENT_TIMESTAMP | 更新日時                       |
| item_calories    | INTEGER                  | DEFAULT 0                 | 食材分カロリー（キャッシュ値） |
| item_protein_g   | DECIMAL(8,2)             | DEFAULT 0.00              | 食材分タンパク質量（g）        |
| item_fat_g       | DECIMAL(8,2)             | DEFAULT 0.00              | 食材分脂質量（g）              |
| item_net_carbs_g | DECIMAL(8,2)             | DEFAULT 0.00              | 食材分正味炭水化物量（g）      |
| notes            | TEXT                     | NULL 許可                 | 食材メモ・備考                 |
| is_deleted       | BOOLEAN                  | DEFAULT false             | 論理削除フラグ                 |

## インデックス

### users テーブル

- `users_pkey`: 主キー（id）
- `users_email_key`: email の UNIQUE インデックス
- `users_username_key`: username の UNIQUE インデックス
- `idx_users_email`: email 検索用
- `idx_users_username`: username 検索用
- `idx_users_created_at`: 作成日時ソート用

### foods テーブル

- `foods_pkey`: 主キー（id）
- `idx_foods_name_search`: name の全文検索用（GIN インデックス）
- `idx_foods_category_id`: category_id 検索用
- `idx_foods_tags`: tags 配列検索用（GIN インデックス）
- `idx_foods_name`: name 検索用
- `idx_foods_is_active`: 有効フラグ検索用
- `idx_foods_nutrition_carnivore`: カーニボア向け栄養成分複合インデックス

### categories テーブル

- `categories_pkey`: 主キー（id）
- `idx_categories_parent_id`: parent_id 検索用（階層クエリ）
- `idx_categories_level`: level 検索用
- `idx_categories_name`: name 検索用
- `idx_categories_is_active`: 有効フラグ検索用
- `idx_categories_parent_sort`: 親カテゴリ内ソート用複合インデックス
- `idx_categories_level_sort`: レベル別ソート用複合インデックス

### meals テーブル

- `meals_pkey`: 主キー（id）
- `unique_user_meal_date_type`: ユーザー・日付・食事タイプの複合 UNIQUE 制約
- `idx_meals_user_date_type`: ユーザー・日付・食事タイプ検索用複合インデックス
- `idx_meals_user_date`: ユーザー・日付検索用インデックス
- `idx_meals_date`: 日付検索用インデックス
- `idx_meals_user_recent`: ユーザー別最新食事検索用インデックス
- `idx_meals_nutrition_totals`: 栄養集計値検索用複合インデックス

### meal_items テーブル

- `meal_items_pkey`: 主キー（id）
- `unique_meal_food`: 食事・食材の複合 UNIQUE 制約
- `idx_meal_items_meal_id`: meal_id 検索用インデックス
- `idx_meal_items_food_id`: food_id 検索用インデックス
- `idx_meal_items_meal_food`: 食事・食材複合インデックス
- `idx_meal_items_nutrition`: 栄養成分検索用複合インデックス
- `idx_meal_items_created_at`: 作成日時ソート用インデックス

## バリデーション制約

### users テーブル

- `users_email_format`: メールアドレス形式チェック
- `users_username_length`: ユーザー名長さチェック（3-30 文字）
- `users_calorie_goal_positive`: カロリー目標範囲チェック（1-10000）
- `users_protein_goal_positive`: タンパク質目標範囲チェック（0-1000g）
- `users_fat_goal_positive`: 脂質目標範囲チェック（0-1000g）
- `users_net_carbs_goal_positive`: 正味炭水化物目標範囲チェック（0-1000g）

### foods テーブル

- `foods_name_not_empty`: 食材名の空文字チェック
- `foods_calories_positive`: カロリー範囲チェック（0-2000kcal）
- `foods_protein_positive`: タンパク質範囲チェック（0-200g）
- `foods_fat_positive`: 脂質範囲チェック（0-200g）
- `foods_carbs_positive`: 炭水化物範囲チェック（0-200g）
- `foods_fiber_positive`: 食物繊維範囲チェック（0g 以上、炭水化物以下）

### categories テーブル

- `categories_name_not_empty`: カテゴリ名の空文字チェック
- `categories_display_name_not_empty`: 表示名の空文字チェック
- `categories_level_positive`: レベル範囲チェック（1-10）
- `categories_sort_order_positive`: ソート順序正数チェック
- `categories_no_self_parent`: 自己参照防止チェック
- `categories_unique_name_per_parent`: 同一親内でのカテゴリ名重複防止

### meals テーブル

- `meals_meal_type_valid`: 食事タイプの値制約（BREAKFAST/LUNCH/DINNER/SNACK）
- `meals_meal_date_valid`: 食事日付の範囲制約（2020-01-01〜未来 7 日まで）
- `meals_total_calories_positive`: 合計カロリーの範囲制約（0-20000kcal）
- `meals_total_protein_positive`: 合計タンパク質の範囲制約（0-2000g）
- `meals_total_fat_positive`: 合計脂質の範囲制約（0-2000g）
- `meals_total_net_carbs_positive`: 合計正味炭水化物の範囲制約（0-2000g）

### meal_items テーブル

- `meal_items_quantity_positive`: 摂取量の範囲制約（0.01-10000g）
- `meal_items_item_calories_positive`: 食材分カロリーの範囲制約（0-10000kcal）
- `meal_items_item_protein_positive`: 食材分タンパク質の範囲制約（0-1000g）
- `meal_items_item_fat_positive`: 食材分脂質の範囲制約（0-1000g）
- `meal_items_item_net_carbs_positive`: 食材分正味炭水化物の範囲制約（0-1000g）

## 初期データ

### users テーブル

以下の 3 つのテストユーザーが作成されます：

1. **test1@example.com** (testuser1) - 一般的な栄養目標
2. **test2@example.com** (carnivore_master) - 高タンパク・低糖質目標
3. **test3@example.com** (light_eater) - 軽量目標

パスワードはすべて `password123` です。

### foods テーブル

以下のカーニボア向け食材が登録されます：

**肉類（25 品目）**

- 牛肉：サーロイン、ヒレ、リブロース、もも、挽肉
- 豚肉：ロース、ヒレ、バラ、挽肉
- 鶏肉：もも肉、むね肉、ささみ、皮

**魚類（5 品目）**

- サケ、マグロ（赤身・中トロ）、サバ、イワシ

**卵・乳製品（8 品目）**

- 鶏卵（全卵・卵黄・卵白）、牛乳、チーズ、バター、生クリーム、ヨーグルト

各食材には適切なタグ（肉類、高タンパク、低糖質等）が設定されています。

### categories テーブル

以下の階層構造でカテゴリが作成されます：

**ルートカテゴリ（レベル 1）**

- 肉類、魚介類、卵類、乳製品、調味料・香辛料、内臓

**サブカテゴリ（レベル 2）**

- 肉類：牛肉、豚肉、鶏肉、羊肉、ジビエ、加工肉
- 魚介類：サケ類、マグロ類、白身魚、青魚、貝類、甲殻類、缶詰魚
- 卵類：鶏卵、うずら卵、アヒル卵
- 乳製品：牛乳、チーズ、バター、クリーム、ヨーグルト
- 調味料：塩、胡椒、ハーブ、スパイス、動物性油脂
- 内臓：レバー、ハート、タン、キドニー、ブレイン

**詳細カテゴリ（レベル 3）**

- 牛肉：ステーキ用、ローストビーフ用、挽肉、ショートリブ
- 豚肉：ポークチョップ、ポークベリー、ポークショルダー、豚挽肉

各食材は適切なカテゴリに自動分類され、外部キー制約により整合性が保証されています。

### meals・meal_items テーブル

以下のサンプル食事データが投入されます：

**user_id=1 の食事データ（過去 1 週間分）**

- 朝食・昼食・夕食・間食の詳細な記録
- カーニボア食材を中心とした実際的な食事内容
- 栄養成分の自動計算による正確な数値
- 日別・食事タイプ別の栄養摂取パターン

**食事詳細データ（meal_items）**

- 最新の朝食分に食材詳細を登録
- 牛ステーキ 150g、卵 100g、バター 10g の組み合わせ
- 栄養成分の自動計算と meal への集計反映

**特徴**

- 実際の使用を想定したリアルなデータ量
- 栄養計算の精度検証が可能
- パフォーマンステスト用の適切なデータボリューム

## トラブルシューティング

### 接続エラーの場合

```bash
# Dockerコンテナが起動しているか確認
docker ps | grep postgres

# データベースサービスの再起動
docker-compose restart db
```

### 権限エラーの場合

```sql
-- データベース権限確認
\l
\du
```

### マイグレーション失敗の場合

```sql
-- テーブル存在確認
\dt

-- 手動でテーブル削除（必要に応じて）
DROP TABLE IF EXISTS meal_items CASCADE;
DROP TABLE IF EXISTS meals CASCADE;
DROP TABLE IF EXISTS foods CASCADE;
DROP TABLE IF EXISTS categories CASCADE;
DROP TABLE IF EXISTS users CASCADE;
```
