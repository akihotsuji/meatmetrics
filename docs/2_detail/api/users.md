# Users API（MVP）

User コンテキストのユーザー情報管理機能を提供する API。ユーザーのプロフィール情報と設定管理を担う。

- ベース: `/api/users`
- 認証: 必要（JWT ベース）

## エンドポイント

- GET `/goals` - ユーザー目標取得
- PUT `/goals` - ユーザー目標更新

**注意**: パスワード変更は `/api/auth/change-password` （Auth コンテキスト）で提供

## スキーマ

- ユーザー目標（取得・更新共通）

```json
{ "calorie": 2000, "protein_g": 150, "fat_g": 120, "net_carbs_g": 20 }
```

## エラーモデル

`VALIDATION_ERROR`, `UNAUTHORIZED`, `INTERNAL_ERROR`

## 例

- curl

```bash
curl -X GET https://api.example.com/api/users/goals \
  -H "Authorization: Bearer TOKEN"

curl -X PUT https://api.example.com/api/users/goals \
  -H "Authorization: Bearer TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"calorie":2000,"protein_g":150,"fat_g":120,"net_carbs_g":20}'
```

- TypeScript

```ts
await fetch("/api/users/goals", {
  method: "GET",
  headers: { Authorization: `Bearer ${token}` },
});

await fetch("/api/users/goals", {
  method: "PUT",
  headers: {
    Authorization: `Bearer ${token}`,
    "Content-Type": "application/json",
  },
  body: JSON.stringify({
    calorie: 2000,
    protein_g: 150,
    fat_g: 120,
    net_carbs_g: 20,
  }),
});
```

- Java (Controller スケッチ)

```java
@GetMapping("/goals")
public UserGoalsResponse getGoals(Authentication auth) { /* ... */ }

@PutMapping("/goals")
public ResponseEntity<Void> updateGoals(@RequestBody UserGoalsRequest req, Authentication auth) { /* ... */ }
```

## 実装メモ

- 目標値は正の値のみ許可（バリデーション）
- 未設定時はデフォルト値を返却
- パスワード変更は Auth API `/api/auth/change-password` を使用
