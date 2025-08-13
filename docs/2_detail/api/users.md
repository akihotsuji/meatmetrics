# Users API（MVP）

- ベース: `/api/users`
- 認証: 必須（Bearer JWT）

## エンドポイント

- GET `/goals`
- PUT `/goals`
- PUT `/password`

## スキーマ

- Goals GET/PUT

```json
{ "calorie": 2000, "protein_g": 150, "fat_g": 120, "net_carbs_g": 20 }
```

- Password Change

```json
{ "currentPassword": "string", "newPassword": "string" }
```

## エラーモデル

`VALIDATION_ERROR`, `UNAUTHORIZED`, `INTERNAL_ERROR`

## 例

- curl（目標更新）

```bash
curl -X PUT -H "Authorization: Bearer <TOKEN>" -H "Content-Type: application/json" \
  -d '{"calorie":2000,"protein_g":150,"fat_g":120,"net_carbs_g":20}' \
  https://api.example.com/api/users/goals
```

- TypeScript

```ts
await fetch("/api/users/goals", {
  method: "PUT",
  headers: {
    "Content-Type": "application/json",
    Authorization: `Bearer ${token}`,
  },
  body: JSON.stringify(goals),
});
```

- Java (Controller スケッチ)

```java
@PutMapping("/goals")
public GoalsResponse updateGoals(@RequestBody GoalsRequest req){ /* ... */ }
```

## 実装メモ

- 複数レコード管理（effective_date, is_active）
- 取得時は最新の有効レコードを返す
