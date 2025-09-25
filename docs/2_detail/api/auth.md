# Auth API（MVP）

Auth/User コンテキストの認証機能を提供する API。ユーザー登録・ログイン・JWT 管理を担う。

- ベース: `/api/auth`
- 認証: 不要（認証自体を提供する API）※ログアウトのみ認証必要

## エンドポイント

- POST `/register` - User Registration（ユーザー登録）
- POST `/login` - User Login（ユーザーログイン）
- POST `/logout` - User Logout（ユーザーログアウト）※認証必要
- POST `/refresh` - JWT Token 更新
- POST `/change-password` - パスワード変更※認証必要

## スキーマ

- Register Request

```json
{ "email": "user@example.com", "password": "string", "username": "string" }
```

- Register Response

```json
{
  "userId": 1,
  "email": "user@example.com",
  "username": "string",
  "createdAt": "2024-01-01T12:00:00"
}
```

- Login Request

```json
{ "email": "user@example.com", "password": "string" }
```

- Login Response

```json
{
  "accessToken": "jwt",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "refreshToken": "refresh_jwt"
}
```

- Refresh Request

```json
{ "refreshToken": "refresh_jwt" }
```

- Refresh Response

```json
{
  "accessToken": "new_jwt",
  "tokenType": "Bearer",
  "expiresIn": 86400,
  "refreshToken": "new_refresh_jwt"
}
```

- Logout Request

```
POST /api/auth/logout
Authorization: Bearer {token}
```

- Logout Response

```json
{ "success": true, "message": "ログアウト完了", "data": null }
```

- Change Password Request

```json
{ "currentPassword": "string", "newPassword": "string" }
```

- Change Password Response

```json
{ "success": true, "message": "パスワード変更完了", "data": null }
```

## エラーモデル

`VALIDATION_ERROR`, `UNAUTHORIZED`, `CONFLICT`, `INTERNAL_ERROR`

## 例

- curl

```bash
# ログイン
curl -X POST https://api.example.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"pass"}'

# トークン更新
curl -X POST https://api.example.com/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"refreshToken":"refresh_jwt_token"}'

# ログアウト
curl -X POST https://api.example.com/api/auth/logout \
  -H "Authorization: Bearer {access_token}"

# パスワード変更
curl -X POST https://api.example.com/api/auth/change-password \
  -H "Authorization: Bearer {access_token}" \
  -H "Content-Type: application/json" \
  -d '{"currentPassword":"old","newPassword":"new"}'
```

- TypeScript

```ts
// ログイン
await fetch("/api/auth/login", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({ email, password }),
});

// トークン更新
await fetch("/api/auth/refresh", {
  method: "POST",
  headers: { "Content-Type": "application/json" },
  body: JSON.stringify({ refreshToken }),
});

// ログアウト
await fetch("/api/auth/logout", {
  method: "POST",
  headers: { Authorization: `Bearer ${accessToken}` },
});

// パスワード変更
await fetch("/api/auth/change-password", {
  method: "POST",
  headers: {
    Authorization: `Bearer ${accessToken}`,
    "Content-Type": "application/json",
  },
  body: JSON.stringify({ currentPassword, newPassword }),
});
```

- Java (Controller スケッチ)

```java
@PostMapping("/login")
public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest req) { /* ... */ }

@PostMapping("/refresh")
public ResponseEntity<ApiResponse<LoginResponse>> refresh(@RequestBody RefreshRequest req) { /* ... */ }

@PostMapping("/logout")
public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) { /* ... */ }

@PostMapping("/change-password")
public ResponseEntity<ApiResponse<Void>> changePassword(@RequestBody ChangePasswordRequest req, HttpServletRequest request) { /* ... */ }
```

## 実装メモ

- アクセストークンは短命（例: 24h）
- 失敗理由は一般化（アカウント有無/パスワード不一致の詳細は出さない）
- ログアウトは現時点では単純な成功レスポンス（将来的にトークンブラックリスト機能追加予定）
- ログアウト時は Authorization Header の形式とトークンの有効性を検証
- パスワード変更は Auth コンテキスト（認証操作のため現在パスワード認証が必要）
