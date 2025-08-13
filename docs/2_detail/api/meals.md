# Meals API（MVP）

- ベース: `/api/meals`
- 認証: 必須（Bearer JWT）

## エンドポイント

- GET `/?date=YYYY-MM-DD`（一覧）
- POST `/`（作成）
- PUT `/{id}`（更新）
- DELETE `/{id}`（削除）

## スキーマ

- POST/PUT Request

```json
{
  "date": "2025-08-13",
  "items": [{ "foodId": 1, "amount_g": 150 }],
  "notes": "optional"
}
```

- Response（作成/更新）

```json
{
  "id": 10,
  "total_nutrition": {
    "calories": 600,
    "protein": 45,
    "fat": 35,
    "net_carbs": 2
  }
}
```

## エラーモデル

`VALIDATION_ERROR`, `UNAUTHORIZED`, `NOT_FOUND`, `INTERNAL_ERROR`

## 例

- curl（作成）

```bash
curl -X POST -H "Authorization: Bearer <TOKEN>" -H "Content-Type: application/json" \
  -d '{"date":"2025-08-13","items":[{"foodId":1,"amount_g":150}]}' \
  https://api.example.com/api/meals
```

- TypeScript

```ts
await fetch("/api/meals", {
  method: "POST",
  headers: {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
  },
  body: JSON.stringify(body),
});
```

- Java (Controller スケッチ)

```java
@PostMapping
public MealCreatedResponse create(@RequestBody MealCreateRequest req){ /* ... */ }
```

## 実装メモ

- total_nutrition はサーバ側で再計算・保存
- net_carbs = carbohydrates - fiber
