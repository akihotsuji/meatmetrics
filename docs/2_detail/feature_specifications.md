# カーニボア専用栄養成分計算アプリ 詳細機能仕様書

## 0. MVP 仕様サマリ

- 認証: メール確認なし。登録/ログイン、ログイン中のパスワード変更のみ（2FA/メール送信は除外）
- 目標: 1 日あたり カロリー(kcal)・タンパク質(g)・脂質(g)・糖質(g)
- 食材/検索: 名前部分一致 + カテゴリ + タグ（複数可、例: 乳製品/低糖質/低価格）
- 記録: g 単位のみで日付・食材・量を入力
- 計算: マクロ 4 種をサーバー側で計算。糖質(g) = 炭水化物(g) - 食物繊維(g)
- サマリー: 日別の合計と目標達成率のみ
- 非対象: 詳細分析、レポート、週/月ダッシュボード、メールでの再設定

## 1. ユーザー管理機能の詳細仕様

### 1.1 ユーザー登録・プロフィール設定

#### 1.1.1 基本情報

- **必須項目**

  - ユーザー名（3-20 文字、英数字・ひらがな・カタカナ）
  - メールアドレス（形式検証、重複チェック）
  - パスワード（8 文字以上、英数字・記号の組み合わせ）

- **プロフィール情報**
  - 年齢（18-100 歳）
  - 性別（男性/女性/その他）
  - 身長（100-250cm）
  - 体重（30-200kg）
  - 活動レベル（低い/普通/高い/非常に高い）

#### 1.1.2 カーニボア設定

- **実践期間**

  - 開始日
  - 現在の段階（初心者/中級者/上級者）
  - 目標（減量/増量/維持/健康維持）

- **栄養目標設定**
  - 1 日のカロリー目標
  - タンパク質目標（体重 1kg あたり 1.6-2.2g 推奨）
  - 脂質目標（総カロリーの 60-80%推奨）
  - 炭水化物制限（50g 以下/日推奨）

### 1.2 認証・セキュリティ

- **ログイン**

  - メールアドレス + パスワード
  - 2 段階認証（オプション）
  - ログイン履歴の記録

- **セッション管理**
  - JWT トークン（有効期限：24 時間）
  - 自動ログアウト（30 分間の操作なし）
  - 複数デバイスでの同時ログイン対応

## 2. 食材管理機能の詳細仕様

### 2.1 食材データベース構造

#### 2.1.1 食材カテゴリ

```
肉類
├── 牛肉
│   ├── 赤身肉（サーロイン、ヒレ、肩ロース）
│   ├── 脂身肉（カルビ、バラ、リブロース）
│   └── 内臓肉（レバー、ハツ、タン）
├── 豚肉
│   ├── 赤身肉（ヒレ、肩ロース、モモ）
│   ├── 脂身肉（バラ、カルビ）
│   └── 内臓肉（レバー、ハツ、タン）
├── 鶏肉
│   ├── 胸肉（皮なし、皮付き）
│   ├── モモ肉（皮なし、皮付き）
│   └── 内臓肉（レバー、ハツ、砂肝）
├── 羊肉
├── 魚介類
│   ├── 青魚（サバ、イワシ、サンマ）
│   ├── 白身魚（タイ、ヒラメ、カレイ）
│   └── 貝類（牡蠣、アサリ、シジミ）
└── その他
    ├── 卵（鶏卵、うずら卵）
    └── 乳製品（チーズ、バター、生クリーム）
```

#### 2.1.2 栄養成分データ構造

```json
{
  "food_id": "beef_sirloin_lean",
  "name": "サーロイン（赤身）",
  "category": "beef",
  "subcategory": "lean",
  "nutrition_per_100g": {
    "calories": 250,
    "protein": 26.0,
    "fat": 15.0,
    "carbohydrates": 0.0,
    "vitamins": {
      "vitamin_b12": 2.1,
      "vitamin_b6": 0.5,
      "niacin": 6.0,
      "riboflavin": 0.2
    },
    "minerals": {
      "iron": 2.5,
      "zinc": 4.0,
      "selenium": 25.0,
      "phosphorus": 200.0
    }
  },
  "cooking_methods": {
    "raw": { "calories": 250, "protein": 26.0, "fat": 15.0 },
    "grilled": { "calories": 280, "protein": 28.0, "fat": 16.0 },
    "fried": { "calories": 320, "protein": 26.0, "fat": 22.0 }
  }
}
```

### 2.2 食材検索機能

#### 2.2.1 検索方法

- **キーワード検索**

  - 食材名（部分一致、ひらがな/カタカナ/英語対応）
  - 栄養成分名（タンパク質、ビタミン B12 など）
  - 調理方法（焼く、煮る、生食など）

- **フィルター検索**

  - カテゴリ別
  - 栄養成分の範囲指定
  - カロリー範囲
  - タンパク質含有量
  - 脂質含有量

- **お気に入り機能**
  - よく使う食材の登録
  - カスタム食材の作成
  - 個人の食材リスト管理

## 3. 栄養計算機能の詳細仕様

### 3.1 食事記録システム

#### 3.1.1 記録方法

- **手動入力**

  - 食材選択（検索・お気に入りから）
  - 量の入力（グラム、個数、大さじなど）
  - 調理方法の選択
  - 食事時間の記録

- **一括入力**
  - レシピからの一括登録
  - よくある組み合わせのテンプレート
  - 前日の食事のコピー

#### 3.1.2 栄養計算ロジック

```typescript
interface NutritionCalculation {
  // 基本栄養素
  totalCalories: number;
  totalProtein: number;
  totalFat: number;
  totalCarbohydrates: number;

  // ビタミン・ミネラル
  vitamins: VitaminProfile;
  minerals: MineralProfile;

  // カーニボア指標
  carnivoreScore: number; // 肉食度（0-100）
  proteinQuality: number; // タンパク質品質（0-100）
  fatBalance: number; // 脂質バランス（0-100）

  // 目標達成率
  calorieGoalAchievement: number; // カロリー目標達成率
  proteinGoalAchievement: number; // タンパク質目標達成率
  fatGoalAchievement: number; // 脂質目標達成率
}
```

### 3.2 カーニボア特化の栄養分析

#### 3.2.1 栄養素の重要度

- **最重要栄養素**

  - タンパク質（必須アミノ酸のバランス）
  - ビタミン B12（神経系の健康）
  - 鉄（貧血予防）
  - オメガ 3 脂肪酸（炎症抑制）

- **カーニボアで不足しがちな栄養素**
  - ビタミン C（抗酸化作用）
  - 食物繊維（腸内環境）
  - カルシウム（骨の健康）

#### 3.2.2 栄養バランス評価

```typescript
interface CarnivoreNutritionAnalysis {
  // 基本評価
  overallScore: number; // 総合評価（0-100）

  // カテゴリ別評価
  proteinScore: number; // タンパク質評価
  fatScore: number; // 脂質評価
  vitaminScore: number; // ビタミン評価
  mineralScore: number; // ミネラル評価

  // 推奨事項
  recommendations: string[]; // 改善提案
  warnings: string[]; // 注意事項

  // 比較分析
  comparisonWithGoals: GoalComparison;
  comparisonWithCarnivoreStandard: StandardComparison;
}
```

## 4. 進捗追跡機能の詳細仕様

### 4.1 ダッシュボード

#### 4.1.1 日別ビュー

- **栄養摂取サマリー**

  - カロリー摂取量と目標値
  - 主要栄養素の摂取状況
  - カーニボアスコアの推移

- **食事タイムライン**
  - 朝食、昼食、夕食、間食の記録
  - 各食事の栄養成分
  - 食事間隔の分析

#### 4.1.2 週別・月別ビュー

- **トレンド分析**

  - 栄養摂取の傾向
  - 体重・体調の変化
  - 食事パターンの分析

- **目標達成率**
  - 週間・月間の目標達成状況
  - 継続日数の記録
  - 改善ポイントの特定

### 4.2 レポート機能

#### 4.2.1 栄養レポート

- **詳細分析**

  - 栄養素別の摂取状況
  - 食材別の貢献度
  - 調理方法の影響

- **健康指標**
  - 体重の変化
  - 体調の記録
  - エネルギーレベル

#### 4.2.2 データエクスポート

- **フォーマット**

  - CSV（栄養データ）
  - PDF（月間レポート）
  - JSON（API 連携用）

- **連携機能**
  - 健康管理アプリとの連携
  - 栄養士への共有
  - 医療機関への提供

## 5. データベース設計

### 5.1 主要テーブル構造

#### 5.1.1 ユーザーテーブル

```sql
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(20) UNIQUE NOT NULL,
  email VARCHAR(255) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL,
  profile JSONB,
  nutrition_goals JSONB,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 5.1.2 食材テーブル

```sql
CREATE TABLE foods (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  category VARCHAR(50) NOT NULL,
  subcategory VARCHAR(50),
  nutrition_data JSONB NOT NULL,
  cooking_methods JSONB,
  is_active BOOLEAN DEFAULT true,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

#### 5.1.3 食事記録テーブル

```sql
CREATE TABLE meal_records (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT REFERENCES users(id),
  meal_date DATE NOT NULL,
  meal_type VARCHAR(20), -- breakfast, lunch, dinner, snack
  foods JSONB, -- [{food_id, amount, cooking_method}]
  total_nutrition JSONB,
  notes TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 5.2 インデックス設計

```sql
-- 検索性能向上
CREATE INDEX idx_foods_category ON foods(category);
CREATE INDEX idx_foods_name ON foods USING gin(to_tsvector('japanese', name));
CREATE INDEX idx_meal_records_user_date ON meal_records(user_id, meal_date);

-- 栄養成分での検索
CREATE INDEX idx_foods_nutrition ON foods USING gin(nutrition_data);
```

## 6. API 設計（MVP）

- 原則: 認証後のエンドポイントは `Authorization: Bearer <JWT>` を必須
- データキー: 糖質は `net_carbs` を使用

### 6.1 認証 API

```
POST /api/auth/register
  body: { "email": string, "password": string, "username": string }
  res: 201 Created { "userId": number }

POST /api/auth/login
  body: { "email": string, "password": string }
  res: 200 OK { "accessToken": string, "expiresIn": number }

POST /api/auth/logout
  res: 204 No Content

POST /api/auth/refresh
  res: 200 OK { "accessToken": string, "expiresIn": number }
```

### 6.2 食材 API

```
GET /api/foods?q=&category=&tags=
  - q: 部分一致キーワード（食材名）
  - category: カテゴリ名 or ID（任意）
  - tags: カンマ区切りの複数タグ（例: tags=乳製品,低糖質）
  res: 200 OK [
    {
      "id": number,
      "name": string,
      "category": string,
      "tags": string[],
      "nutrition_per_100g": {
        "calories": number,
        "protein": number,
        "fat": number,
        "carbohydrates": number,
        "fiber"?: number
      }
    }
  ]

GET /api/foods/{id}
  res: 200 OK {
    "id": number,
    "name": string,
    "category": string,
    "tags": string[],
    "nutrition_per_100g": { "calories": number, "protein": number, "fat": number, "carbohydrates": number, "fiber"?: number }
  }
```

### 6.3 食事記録 API

```
GET /api/meals?date=YYYY-MM-DD
  res: 200 OK [
    {
      "id": number,
      "date": "YYYY-MM-DD",
      "items": [ { "foodId": number, "amount_g": number } ],
      "total_nutrition": { "calories": number, "protein": number, "fat": number, "net_carbs": number },
      "notes"?: string
    }
  ]

POST /api/meals
  body: { "date": "YYYY-MM-DD", "items": [ { "foodId": number, "amount_g": number } ], "notes"?: string }
  res: 201 Created { "id": number, "total_nutrition": { "calories": number, "protein": number, "fat": number, "net_carbs": number } }

PUT /api/meals/{id}
  body: { "items"?: [ { "foodId": number, "amount_g": number } ], "notes"?: string }
  res: 200 OK { "id": number, "total_nutrition": { "calories": number, "protein": number, "fat": number, "net_carbs": number } }

DELETE /api/meals/{id}
  res: 204 No Content
```

### 6.4 サマリー API

```
GET /api/summary?date=YYYY-MM-DD
  res: 200 OK {
    "date": "YYYY-MM-DD",
    "totals": { "calories": number, "protein": number, "fat": number, "net_carbs": number },
    "goals": { "calorie": number, "protein_g": number, "fat_g": number, "net_carbs_g": number },
    "achievement": { "calorie": number, "protein": number, "fat": number, "net_carbs": number } // 0-100(%)
  }
```

### 6.5 ユーザー API

```
GET /api/users/goals
  res: 200 OK { "calorie": number, "protein_g": number, "fat_g": number, "net_carbs_g": number }

PUT /api/users/goals
  body: { "calorie": number, "protein_g": number, "fat_g": number, "net_carbs_g": number }
  res: 200 OK { "calorie": number, "protein_g": number, "fat_g": number, "net_carbs_g": number }

PUT /api/users/password
  body: { "currentPassword": string, "newPassword": string }
  res: 204 No Content
```

### 6.6 エラーモデル（共通）

```
400 Bad Request { "code": "VALIDATION_ERROR", "message": string, "details"?: any }
401 Unauthorized { "code": "UNAUTHORIZED", "message": string }
404 Not Found { "code": "NOT_FOUND", "message": string }
409 Conflict { "code": "CONFLICT", "message": string }
500 Internal Server Error { "code": "INTERNAL_ERROR", "message": string }
```

## 7. フロントエンド画面設計

### 7.1 主要画面構成

- **認証画面**

  - ログイン画面
  - ユーザー登録画面
  - パスワードリセット画面

- **メイン画面**

  - ダッシュボード
  - 食材検索・選択
  - 食事記録入力
  - 栄養分析結果

- **管理画面**
  - プロフィール設定
  - 栄養目標設定
  - 進捗確認
  - レポート表示

### 7.2 UI/UX 設計原則

- **モバイルファースト**

  - タッチ操作に最適化
  - 片手での操作を考慮
  - レスポンシブデザイン

- **直感的な操作性**

  - 食材の視覚的選択
  - ドラッグ&ドロップでの量調整
  - リアルタイムでの栄養計算表示

- **カーニボア特化のデザイン**
  - 肉食を連想させる色使い
  - 栄養成分の分かりやすい表示
  - 目標達成の達成感を演出

---

**作成日**: 2024 年 12 月
**作成者**: 開発チーム
**最終更新**: 2024 年 12 月
