# Auth API（MVP）

- ベース: `/api/auth`
- 認証: 不要

## エンドポイント

- POST `/register`
- POST `/login`
- POST `/logout`
- POST `/refresh`

## スキーマ

- Register Request

```json
{ "email": "user@example.com", "password": "string", "username": "string" }
```

- Register Response

```json
{ "userId": 1 }
```

- Login Request

```json
{ "email": "user@example.com", "password": "string" }
```

- Login Response

```json
{ "accessToken": "jwt", "expiresIn": 86400 }
```

## エラーモデル

`VALIDATION_ERROR`, `UNAUTHORIZED`, `CONFLICT`, `INTERNAL_ERROR`

## 例

- curl

```bash
curl -X POST https://api.example.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"pass"}'
```

- TypeScript

```ts
await fetch("/api/auth/login", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({ email, password }),
});
```

- Java (Controller スケッチ)

```java
@PostMapping("/login")
public TokenResponse login(@RequestBody LoginRequest req) { /* ... */ }
```

## 実装メモ

- アクセストークンは短命（例: 24h）
- 失敗理由は一般化（アカウント有無/パスワード不一致の詳細は出さない）
