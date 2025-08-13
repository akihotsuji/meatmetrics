# Summary API（MVP）

- ベース: `/api/summary`
- 認証: 必須（Bearer JWT）

## エンドポイント

- GET `/?date=YYYY-MM-DD`

## レスポンス

```json
{
  "date": "2025-08-13",
  "totals": { "calories": 1800, "protein": 130, "fat": 100, "net_carbs": 15 },
  "goals": {
    "calorie": 2000,
    "protein_g": 150,
    "fat_g": 120,
    "net_carbs_g": 20
  },
  "achievement": {
    "calorie": 90.0,
    "protein": 86.7,
    "fat": 83.3,
    "net_carbs": 75.0
  }
}
```

## エラーモデル

`UNAUTHORIZED`, `INTERNAL_ERROR`

## 例

- curl

```bash
curl -H "Authorization: Bearer <TOKEN>" "https://api.example.com/api/summary?date=2025-08-13"
```

- TypeScript

```ts
await fetch(`/api/summary?date=${date}`, {
  headers: { Authorization: `Bearer ${token}` },
});
```

## 実装メモ

- サーバ側で当日/指定日の meal_records を集計
- 目標の有効期間は is_active + effective_date を採用
