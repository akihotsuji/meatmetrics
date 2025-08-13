# 06. Domain Model（MVP）

MVP のドメイン境界と主要エンティティ/値オブジェクトを定義する。DDD の 4 層（Presentation, Application, Domain, Infrastructure）に準拠。

## 1. 境界づけられたコンテキスト

- Authentication/User: 認証・ユーザー・目標設定
- Food Catalog: 食材・カテゴリ・タグ
- Meal Tracking: 食事記録・合計栄養
- Summary: 日別サマリー（集計はアプリ層で実行）

## 2. クラス図（MVP）

```mermaid
classDiagram
  class User {
    +Long id
    +String email
    +String passwordHash
    +Instant createdAt
    +Instant updatedAt
  }

  class UserGoal {
    +Long id
    +Long userId
    +Integer dailyCalorieGoal
    +BigDecimal proteinGoalG
    +BigDecimal fatGoalG
    +BigDecimal netCarbsGoalG
    +LocalDate effectiveDate
    +Boolean isActive
  }

  class Food {
    +Long id
    +String name
    +String category
    +String[] tags
    +NutritionPer100g nutrition
  }

  class NutritionPer100g {
    +Integer calories
    +BigDecimal protein
    +BigDecimal fat
    +BigDecimal carbohydrates
    +BigDecimal? fiber
  }

  class MealRecord {
    +Long id
    +Long userId
    +LocalDate mealDate
    +MealItem[] items
    +NutritionTotals total
    +String? notes
  }

  class MealItem {
    +Long foodId
    +BigDecimal amountG
  }

  class NutritionTotals {
    +Integer calories
    +BigDecimal protein
    +BigDecimal fat
    +BigDecimal netCarbs
  }

  User "1" --> "*" MealRecord : has
  User "1" --> "*" UserGoal : has
  MealRecord "*" o-- "*" MealItem : contains
  MealItem "*" --> "1" Food : references
  Food "1" --> "1" NutritionPer100g : embeds
  MealRecord "1" --> "1" NutritionTotals : computes
```

## 3. 集計ロジック（要約）

- net_carbs = carbohydrates - fiber（fiber が無い場合は carbohydrates をそのまま採用）
- total は items の food 栄養値を amount_g/100 倍して合算
- 目標達成率は totals/goal × 100 を小数点 1 桁で丸め

## 4. 集約境界

- User 集約: `User` とアクティブな `UserGoal`
- Meal 集約: `MealRecord`（items と total を内包）
- Food は参照専用（整合性は別コンテキストで管理）

---

参照: `01_system_architecture.md`, `02_database.md`, `03_api.md`
