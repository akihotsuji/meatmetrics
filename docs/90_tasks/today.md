# 認証 Web 層実装 - 今日の詳細タスクリスト

## 🎯 全体概要

認証コンテキストの Web 層（@RestController）を実装する。既存のアプリケーション層（Service/Command/DTO）を活用して RESTful API エンドポイントを構築。

**実装対象**: 7 つのエンドポイント（認証 5 つ + ユーザー目標 2 つ）

- 認証 API: 登録・ログイン・ログアウト・トークン更新・パスワード変更
- ユーザー目標 API: 目標取得・目標更新（MVP 基本機能のみ）
  **作業時間見積**: 6-7 時間  
  **完了目標**: 今日中に Web 層の完全実装

---

## 📋 詳細タスクリスト

### 🔧 **Phase 1: 環境準備・パッケージ構造確認** (30 分)

#### ✅ Task 1-1: 既存実装の確認と分析

- [×] アプリケーション層の実装状況確認
  - [×] `RegisterUserService`, `LoginService`, `ChangePasswordService`
  - [×] `RegisterUserCommand`, `LoginCommand`, `ChangePasswordCommand`
  - [×] `UserRegisteredResult`, `LoginResult`
- [×] 既存 Controller パッケージ構造の確認
  - [×] `GlobalExceptionHandler`の存在確認
  - [×] `HealthController`の参考実装確認

#### ⏭️ Task 1-2: Web 層パッケージ構造の作成

- [×] `com.meatmetrics.meatmetrics.api.auth`パッケージ作成
- [×] REST エンドポイント設計書の最終確認
- [×] セキュリティ設定との連携ポイント確認

### 🏗️ **Phase 2: AuthController 基本構造実装** (45 分)

#### ⏭️ Task 2-1: AuthController 基本クラス作成 (20 分)

- [×] **ファイル作成**: `AuthController.java`
- [×] **クラス基本構造**:
  ```java
  @RestController
  @RequestMapping("/api/auth")
  public class AuthController {
  ```
- [×] **依存関係注入**:
  ```java
  private final RegisterUserService registerUserService;
  private final LoginService loginService;
  private final ChangePasswordService changePasswordService;
  ```
- [×] **コンストラクタインジェクション実装**
- [×] **Javadoc コメント記述**

#### ⏭️ Task 2-2: 共通レスポンス形式の設計 (15 分)

- [×] **共通レスポンス DTO 作成**:
  ```java
  public class ApiResponse<T> {
      private boolean success;
      private String message;
      private T data;
      private LocalDateTime timestamp;
  }
  ```
- [×] **成功・失敗レスポンス用 static メソッド**
- [×] **エラーレスポンス統一フォーマット検討**

#### ⏭️ Task 2-3: バリデーション・例外ハンドリング準備 (10 分)

- [×] Bean Validation アノテーション確認
- [×] 既存 GlobalExceptionHandler との連携確認
- [×] カスタム例外クラスの確認

### 🚀 **Phase 3: エンドポイント個別実装** (120 分)

#### ⏭️ Task 3-1: ユーザー登録エンドポイント実装 (30 分)

- [×] **エンドポイント実装**: `POST /api/auth/register`
  ```java
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<UserRegisteredResult>> register(
      @Valid @RequestBody RegisterUserCommand command) {
  ```
- [×] **実装ポイント**:
  - [×] `@Valid`による Command 自動バリデーション
  - [×] `registerUserService.register(command)`呼び出し
  - [×] 成功時: `201 Created`レスポンス
  - [×] 失敗時: 適切な HTTP ステータス返却
- [×] **レスポンス例**:
  ```json
  {
    "success": true,
    "message": "ユーザー登録が完了しました",
    "data": {
      "userId": 1,
      "email": "test@example.com",
      "username": "testuser",
      "createdAt": "2025-09-22T10:00:00"
    },
    "timestamp": "2025-09-22T10:00:00.123"
  }
  ```
- [×] **Javadoc**と OpenAPI 仕様コメント

#### ⏭️ Task 3-2: ログインエンドポイント実装 (30 分)

- [×] **エンドポイント実装**: `POST /api/auth/login`
  ```java
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<LoginResult>> login(
      @Valid @RequestBody LoginCommand command) {
  ```
- [×] **実装ポイント**:
  - [×] `loginService.login(command)`呼び出し
  - [×] JWT 生成と返却
  - [×] 成功時: `200 OK`レスポンス
  - [×] 認証失敗時: `401 Unauthorized`
- [×] **レスポンス例**:
  ```json
  {
    "success": true,
    "message": "ログインしました",
    "data": {
      "accessToken": "eyJhbGciOiJIUzI1NiIs...",
      "tokenType": "Bearer",
      "expiresIn": 3600,
      "refreshToken": "dGhpcyBpcyBmYWtl..."
    },
    "timestamp": "2025-09-22T10:00:00.123"
  }
  ```

#### ⏭️ Task 3-3: トークン更新エンドポイント実装 (30 分)

- [×] **エンドポイント実装**: `POST /api/auth/refresh`
  ```java
  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<LoginResult>> refreshToken(
      @RequestBody Map<String, String> request) {
  ```
- [×] **実装ポイント**:
  - [×] リクエストボディから refreshToken 取得
  - [×] `loginService.refreshToken(refreshToken)`呼び出し
  - [×] 新しいアクセストークン発行
  - [×] 成功時: `200 OK`、失敗時: `401 Unauthorized`

#### ✅ Task 3-4: ログアウトエンドポイント実装 (30 分) - 完了

- [×] **エンドポイント実装**: `POST /api/auth/logout`
  ```java
  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(
      HttpServletRequest request) {
  ```
- [×] **実装ポイント**:
  - [×] Authorization Header からトークン取得
  - [×] トークン無効化処理（現時点では単純な成功レスポンス）
  - [×] 成功時: `200 OK`レスポンス
  - [×] 未認証時: `401 Unauthorized`
- [×] **将来拡張**: トークンブラックリスト機能

### 🔐 **Phase 3.5: ユーザー目標機能実装** (90 分) - DDD 実装順序

#### ⏭️ Task 3-5: UserGoals ドメインモデル実装 (25 分)

- [ ] **値オブジェクト作成**: `UserGoals.java`
- [ ] **基本構造**:
  ```java
  public class UserGoals {
      private final Integer calorie;
      private final Integer proteinG;
      private final Integer fatG;
      private final Integer netCarbsG;
  ```
- [ ] **バリデーションロジック**:
  - [ ] 正の値のみ許可（calorie > 0, protein_g > 0, etc.）
  - [ ] デフォルト値の定義（未設定時）
- [ ] **不変性保証**: 値オブジェクトとして設計

#### ⏭️ Task 3-6: ユーザー目標アプリケーション層実装 (35 分)

- [ ] **GetUserGoalsQueryService 作成**:
  ```java
  @Service
  public class GetUserGoalsQueryService {
      // ユーザーID → UserGoals取得（デフォルト値対応）
  }
  ```
- [ ] **UpdateUserGoalsService 作成**:
  ```java
  @Service
  public class UpdateUserGoalsService {
      // UserGoals更新、バリデーション実行
  }
  ```
- [ ] **実装ポイント**:
  - [ ] UserRepository との連携
  - [ ] ドメインロジックの適用
  - [ ] 例外処理

#### ⏭️ Task 3-7: ユーザー目標 Web 層実装 (30 分)

- [ ] **UserController 作成**: `UserController.java`
- [ ] **基本構造**:
  ```java
  @RestController
  @RequestMapping("/api/users")
  @PreAuthorize("isAuthenticated()")
  public class UserController {
  ```
- [ ] **エンドポイント実装**:
  - [ ] `GET /api/users/goals` - 目標取得
  - [ ] `PUT /api/users/goals` - 目標更新
- [ ] **レスポンス例**:
  ```json
  {
    "calorie": 2000,
    "protein_g": 150,
    "fat_g": 120,
    "net_carbs_g": 20
  }
  ```

**注記**: パスワード変更は AuthController に実装済み（認証操作のため）

### 🧪 **Phase 4: Web 層統合テスト実装** (90 分)

#### ⏭️ Task 4-1: MockMvc テスト基盤準備 (20 分)

- [ ] **テストクラス作成**: `AuthControllerTest.java`
- [ ] **テスト基本構造**:
  ```java
  @WebMvcTest(AuthController.class)
  @Import(SecurityConfig.class)
  class AuthControllerTest {
      @Autowired private MockMvc mockMvc;
      @MockBean private RegisterUserService registerUserService;
      @MockBean private LoginService loginService;
  ```
- [ ] JSON 変換とモック設定準備

#### ⏭️ Task 4-2: 登録エンドポイントテスト (25 分)

- [ ] **正常ケース**:
  - [ ] 有効なコマンドでユーザー登録成功
  - [ ] `201 Created`とレスポンスボディ検証
- [ ] **異常ケース**:
  - [ ] バリデーションエラー: 無効メール、短いパスワード
  - [ ] 重複エラー: メール・ユーザー名重複
  - [ ] `400 Bad Request`および`409 Conflict`検証

#### ⏭️ Task 4-3: ログインエンドポイントテスト (25 分)

- [ ] **正常ケース**:
  - [ ] 有効な認証情報でログイン成功
  - [ ] JWT トークン返却確認
  - [ ] `200 OK`とレスポンス構造検証
- [ ] **異常ケース**:
  - [ ] 無効な認証情報
  - [ ] 存在しないユーザー
  - [ ] `401 Unauthorized`検証

#### ⏭️ Task 4-4: ログアウト・リフレッシュテスト (20 分)

- [ ] **ログアウトテスト**:
  - [ ] 認証済みユーザーのログアウト成功
  - [ ] 未認証ユーザーのアクセス拒否
- [ ] **トークン更新テスト**:
  - [ ] 有効なリフレッシュトークンでの更新成功
  - [ ] 無効なトークンでの更新失敗

#### ⏭️ Task 4-5: ユーザー目標 API テスト (30 分)

- [ ] **UserControllerTest.java 作成**:
  ```java
  @WebMvcTest(UserController.class)
  class UserControllerTest {
  ```
- [ ] **目標取得 API テスト**:
  - [ ] GET /api/users/goals: 正常ケース（認証済み）
  - [ ] GET /api/users/goals: 未認証アクセス拒否
- [ ] **目標更新 API テスト**:
  - [ ] PUT /api/users/goals: 正常ケース（有効な目標値）
  - [ ] PUT /api/users/goals: バリデーションエラー（負の値等）
  - [ ] PUT /api/users/goals: 未認証アクセス拒否

**注記**: パスワード変更テストは AuthController で実装済み

### ✅ **Phase 5: エラーハンドリング・セキュリティ強化** (45 分)

#### ⏭️ Task 5-1: カスタム例外ハンドラー追加 (20 分)

- [ ] **GlobalExceptionHandler 拡張**:
  - [ ] `DuplicateEmailException` → `409 Conflict`
  - [ ] `AuthenticationException` → `401 Unauthorized`
  - [ ] `ValidationException` → `400 Bad Request`
- [ ] **統一エラーレスポンス形式**:
  ```json
  {
    "success": false,
    "message": "メールアドレスが既に登録されています",
    "error": {
      "code": "DUPLICATE_EMAIL",
      "field": "email",
      "value": "test@example.com"
    },
    "timestamp": "2025-09-22T10:00:00.123"
  }
  ```

#### ⏭️ Task 5-2: セキュリティヘッダー・CORS 設定 (15 分)

- [ ] **セキュリティヘッダー追加**:
  - [ ] `X-Content-Type-Options: nosniff`
  - [ ] `X-Frame-Options: DENY`
  - [ ] `Cache-Control: no-store`（認証関連）
- [ ] **CORS 設定調整**:
  - [ ] 開発環境: `localhost:3000`許可
  - [ ] 本番環境: 適切なオリジン制限

#### ⏭️ Task 5-3: レート制限・ログ出力準備 (10 分)

- [ ] **レート制限検討**（将来実装）:
  - [ ] ログイン試行回数制限
  - [ ] パスワード変更頻度制限
- [ ] **セキュリティログ出力**:
  - [ ] 認証成功・失敗ログ
  - [ ] 異常なアクセスパターン検出準備

### 📋 **Phase 6: 動作確認・最終調整** (30 分)

#### ⏭️ Task 6-1: 統合動作確認 (20 分)

- [ ] **全テスト実行・Pass 確認**
- [ ] **Postman/cURL での手動確認**:
  - [ ] ユーザー登録 → ログイン → トークン更新 → ログアウト
  - [ ] エラーケースの適切なレスポンス確認
- [ ] **フロントエンドとの連携テスト**（可能であれば）

#### ⏭️ Task 6-2: API 仕様書・ドキュメント更新 (10 分)

- [ ] **OpenAPI 仕様の更新**:
  - [ ] エンドポイント定義
  - [ ] リクエスト・レスポンススキーマ
  - [ ] エラーコード一覧
- [ ] **README・設計書の更新**:
  - [ ] API 使用例
  - [ ] 認証フロー図
  - [ ] セキュリティ考慮事項

---

## 🔗 技術仕様・前提条件

### 📚 使用技術

- **Spring Boot**: 3.x
- **Spring Web**: @RestController, @RequestMapping
- **Spring Security**: JWT 認証、@PreAuthorize
- **Bean Validation**: @Valid, @RequestBody
- **Jackson**: JSON シリアライゼーション
- **MockMvc**: Web 層テストフレームワーク

### 🏗️ 既存実装との連携

- **アプリケーション層**: `RegisterUserService`, `LoginService`, `ChangePasswordService`
- **ドメイン層**: `User`集約、値オブジェクト群
- **インフラ層**: `JwtTokenService`, `UserRepository`
- **例外処理**: `GlobalExceptionHandler`

### 📐 RESTful API 設計原則

- **リソース指向 URL**: `/api/auth/{action}`
- **HTTP メソッド**: POST（状態変更操作）
- **ステータスコード**: 201（作成）、200（成功）、400（バリデーション）、401（認証）、409（競合）
- **統一レスポンス形式**: success/message/data/timestamp

---

## ✅ 完了基準

- [ ] **7 つのエンドポイント実装完了**
  - [×] POST /api/auth/register（認証）
  - [×] POST /api/auth/login（認証）
  - [×] POST /api/auth/refresh（認証）
  - [×] POST /api/auth/logout（認証）
  - [×] POST /api/auth/change-password（認証、AuthController に実装）
  - [ ] GET /api/users/goals（ユーザー目標取得）
  - [ ] PUT /api/users/goals（ユーザー目標更新）
- [ ] **Web 層統合テスト実装・Pass**（カバレッジ 80%以上）
- [ ] **エラーハンドリング完備**（統一形式、適切なステータス）
- [ ] **セキュリティ設定適用**（CORS、ヘッダー、認証）
- [ ] **API 仕様書更新**（OpenAPI、エンドポイント定義）
- [ ] **手動動作確認完了**（Postman/cURL）

---

## 🚨 注意事項・リスク対策

1. **セキュリティ**:

   - パスワードは平文でログ出力しない
   - JWT は HTTP-Only Cookie も検討（次期実装）
   - HTTPS 必須（本番環境）

2. **エラーハンドリング**:

   - 機密情報をエラーメッセージに含めない
   - 攻撃者に有利な情報漏洩防止

3. **パフォーマンス**:

   - データベース接続プール設定確認
   - JWT 生成・検証のボトルネック監視

4. **運用**:
   - ログレベル・ログ保持期間の設定
   - モニタリング・アラート設定準備

---

**最終更新**: 2025 年 9 月 22 日  
**担当者**: 開発チーム  
**次回フォローアップ**: 明日（認証機能のフロントエンド実装開始）
