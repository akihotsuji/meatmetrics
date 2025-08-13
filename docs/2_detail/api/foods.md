# Foods API（MVP）

- ベース: `/api/foods`
- 認証: 必須（Bearer JWT）

## エンドポイント

- GET `/` （検索）: `q`, `category`, `tags`（カンマ区切り）
- GET `/{id}` （詳細）

## スキーマ

- Response (一覧要素)

```json
{
  "id": 1,
  "name": "Cheddar",
  "category": "dairy",
  "tags": ["乳製品", "低糖質"],
  "nutrition_per_100g": {
    "calories": 400,
    "protein": 25,
    "fat": 33,
    "carbohydrates": 1.3,
    "fiber": 0
  }
}
```

## 検索クエリの意味

- `q`: 部分一致（name ILIKE）
- `category`: 親カテゴリ名/ID（子カテゴリを包含）
- `tags`: すべて含む（AND）

## エラーモデル

`UNAUTHORIZED`, `NOT_FOUND`, `INTERNAL_ERROR`

## 例

- curl

```bash
curl -H "Authorization: Bearer <TOKEN>" \
  "https://api.example.com/api/foods?q=cheese&tags=乳製品,低糖質"
```

- TypeScript

```ts
await fetch("/api/foods?q=chicken", {
  headers: { Authorization: `Bearer ${token}` },
});
```

- Java (Controller スケッチ)

```java
@GetMapping
public List<FoodDto> search(@RequestParam Optional<String> q, @RequestParam Optional<String> category, @RequestParam Optional<String> tags){ /* ... */ }
```

## 実装メモ

- foods.tags は TEXT[] + GIN を利用
- タグの正規化は拡張で対応
