# 認証 Web 層実装 - 今日の詳細タスクリスト

## 🎯 全体概要

認証コンテキストの Web 層（@RestController）を実装する。既存のアプリケーション層（Service/Command/DTO）を活用して RESTful API エンドポイントを構築。

**実装対象**: 7 つのエンドポイント（認証 5 つ + ユーザー目標 2 つ）

- 認証 API: 登録・ログイン・ログアウト・トークン更新・パスワード変更（完了済み）
- ユーザー目標 API: 目標取得・目標更新（MVP 基本機能、Auth 構成準拠の詳細設計）
  **作業時間見積**: 8-9 時間（詳細クラス設計込み）  
  **完了目標**: 今日中に User 目標機能の完全実装（Domain→Application→Web 層）

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

### 🔐 **Phase 3.5: ユーザー目標機能実装** (180 分) - DDD 実装順序（Auth 構成準拠）

#### ⏭️ Task 3-5: UserGoals ドメインモデル実装 (45 分)

##### Subtask 3-5-1: UserGoals 値オブジェクトの基本構造作成 (15 分)

- [ ] **ファイル作成**: `com.meatmetrics.meatmetrics.user.domain.UserGoals`
- [ ] **基本クラス構造実装**:
  ```java
  public class UserGoals {
      private final Integer calorie;       // カロリー目標値
      private final Integer proteinG;      // タンパク質目標値(g)
      private final Integer fatG;          // 脂質目標値(g)
      private final Integer netCarbsG;     // 糖質目標値(g)
  ```
- [ ] **不変性保証**: final フィールド、setter なし
- [ ] **解説**: 値オブジェクトパターンでユーザーの栄養目標を表現。DDD の値オブジェクトとして同一性ではなく等価性を重視

##### Subtask 3-5-2: UserGoals コンストラクタとバリデーション実装 (15 分)

- [ ] **バリデーション付きコンストラクタ**:
  ```java
  public UserGoals(Integer calorie, Integer proteinG, Integer fatG, Integer netCarbsG) {
      this.calorie = validatePositive(calorie, "calorie");
      this.proteinG = validatePositive(proteinG, "proteinG");
      // 正の値チェック、null チェック
  ```
- [ ] **private バリデーションメソッド**: `validatePositive()`
- [ ] **例外処理**: `IllegalArgumentException` で不正値を拒否
- [ ] **解説**: ドメインルール（栄養目標は正の値）をコンストラクタで強制。不正な状態のオブジェクト生成を防止

##### Subtask 3-5-3: UserGoals デフォルト値とファクトリーメソッド (15 分)

- [ ] **デフォルト値定数定義**:
  ```java
  private static final Integer DEFAULT_CALORIE = 2000;
  private static final Integer DEFAULT_PROTEIN_G = 120;
  private static final Integer DEFAULT_FAT_G = 60;
  private static final Integer DEFAULT_NET_CARBS_G = 20;  // ケトジェニック前提
  ```
- [ ] **ファクトリーメソッド**: `createDefault()`, `createWithDefaults()`
- [ ] **equals/hashCode/toString メソッド**（値オブジェクトの等価性実装）
- [ ] **解説**: 未設定ユーザーにも適切なデフォルト値を提供。ケトジェニック食事法に適したデフォルト設定

#### ⏭️ Task 3-6: ユーザー目標アプリケーション層実装 (75 分)

##### Subtask 3-6-1: UpdateUserGoalsCommand 作成 (20 分)

- [ ] **ファイル作成**: `com.meatmetrics.meatmetrics.user.command.UpdateUserGoalsCommand`
- [ ] **Command 基本構造**:
  ```java
  public class UpdateUserGoalsCommand {
      private Integer calorie;
      private Integer proteinG;
      private Integer fatG;
      private Integer netCarbsG;
  ```
- [ ] **toUserGoals() メソッド実装**: Command → Domain オブジェクト変換
- [ ] **バリデーションロジック**: ドメインルールとの整合性確保
- [ ] **解説**: アプリケーション層でのコマンドパターン。Web 層からの入力をドメイン操作に変換する責務

##### Subtask 3-6-2: GetUserGoalsQueryService 実装 (25 分)

- [ ] **ファイル作成**: `com.meatmetrics.meatmetrics.user.service.GetUserGoalsQueryService`
- [ ] **Service アノテーション**: `@Service`, `@Transactional(readOnly = true)`
- [ ] **依存関係注入**: `UserRepository userRepository`
- [ ] **getUserGoals メソッド実装**:
  ```java
  public UserGoals getUserGoals(Long userId) {
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new UserNotFoundException(userId));
      // UserエンティティからUserGoals取得、null時はデフォルト値
  ```
- [ ] **デフォルト値ハンドリング**: 未設定時の適切なデフォルト値返却
- [ ] **解説**: クエリ専用サービス（CQRS 軽量版）。読み取り専用トランザクションで性能最適化

##### Subtask 3-6-3: UpdateUserGoalsService 実装 (30 分)

- [ ] **ファイル作成**: `com.meatmetrics.meatmetrics.user.service.UpdateUserGoalsService`
- [ ] **Service アノテーション**: `@Service`, `@Transactional`
- [ ] **依存関係注入**: `UserRepository userRepository`
- [ ] **updateUserGoals メソッド実装**:
  ```java
  public void updateUserGoals(Long userId, UpdateUserGoalsCommand command) {
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new UserNotFoundException(userId));
      UserGoals newGoals = command.toUserGoals();
      user.updateGoals(newGoals);  // ドメインメソッド呼び出し
      userRepository.save(user);
  ```
- [ ] **ドメインイベント対応準備**（将来拡張用）
- [ ] **解説**: アプリケーションサービスパターン。ドメインオブジェクトの操作を編成し、永続化を担当

#### ⏭️ Task 3-7: ユーザー目標 Web 層実装 (60 分)

##### Subtask 3-7-1: リクエスト・レスポンス DTO 作成 (20 分)

- [ ] **UpdateUserGoalsRequest 作成**: `com.meatmetrics.meatmetrics.api.user.dto.request.UpdateUserGoalsRequest`

  ```java
  public class UpdateUserGoalsRequest {
      @NotNull(message = "カロリー目標は必須です")
      @Positive(message = "カロリー目標は正の値である必要があります")
      private Integer calorie;

      @NotNull @Positive private Integer protein_g;    // APIではsnake_case
      @NotNull @Positive private Integer fat_g;
      @NotNull @Positive private Integer net_carbs_g;
  ```

- [ ] **Bean Validation アノテーション**: `@NotNull`, `@Positive`, カスタムメッセージ
- [ ] **toCommand() メソッド**: Request → Command 変換
- [ ] **UserGoalsResponse 作成**: `com.meatmetrics.meatmetrics.api.user.dto.response.UserGoalsResponse`
  ```java
  public class UserGoalsResponse {
      private Integer calorie;
      private Integer protein_g;
      private Integer fat_g;
      private Integer net_carbs_g;
      private LocalDateTime updatedAt;  // 最終更新日時
  ```
- [ ] **fromDomain() メソッド**: Domain → Response 変換
- [ ] **解説**: Web 層での入出力データ変換。ドメインモデルと API 仕様の分離を実現

##### Subtask 3-7-2: UserController 基本構造作成 (20 分)

- [ ] **ファイル作成**: `com.meatmetrics.meatmetrics.api.user.UserController`
- [ ] **Controller アノテーション**:
  ```java
  @RestController
  @RequestMapping("/api/users")
  @PreAuthorize("isAuthenticated()")  // 認証必須
  @Validated  // メソッドレベルバリデーション有効化
  public class UserController {
  ```
- [ ] **依存関係注入**:
  ```java
  private final GetUserGoalsQueryService getUserGoalsQueryService;
  private final UpdateUserGoalsService updateUserGoalsService;
  ```
- [ ] **コンストラクタインジェクション実装**
- [ ] **Javadoc クラスコメント**: API 概要、認証要件、使用例
- [ ] **解説**: ユーザー関連機能の Web 層エントリーポイント。認証済みユーザーのみアクセス可能

##### Subtask 3-7-3: 目標取得エンドポイント実装 (10 分)

- [ ] **GET /api/users/goals エンドポイント**:
  ```java
  @GetMapping("/goals")
  public ResponseEntity<ApiResponse<UserGoalsResponse>> getUserGoals(
      Authentication authentication) {
      Long userId = extractUserIdFromAuth(authentication);
      UserGoals goals = getUserGoalsQueryService.getUserGoals(userId);
      UserGoalsResponse response = UserGoalsResponse.fromDomain(goals);
      return ResponseEntity.ok(ApiResponse.success("目標を取得しました", response));
  ```
- [ ] **認証情報からユーザー ID 抽出**: JWT Principal からユーザー情報取得
- [ ] **レスポンス例**:
  ```json
  {
    "success": true,
    "message": "目標を取得しました",
    "data": {
      "calorie": 2000,
      "protein_g": 120,
      "fat_g": 60,
      "net_carbs_g": 20,
      "updated_at": "2025-09-22T10:00:00"
    }
  }
  ```
- [ ] **解説**: 認証済みユーザーの栄養目標取得。デフォルト値も適切に返却

##### Subtask 3-7-4: 目標更新エンドポイント実装 (10 分)

- [ ] **PUT /api/users/goals エンドポイント**:
  ```java
  @PutMapping("/goals")
  public ResponseEntity<ApiResponse<Void>> updateUserGoals(
      @Valid @RequestBody UpdateUserGoalsRequest request,
      Authentication authentication) {
      Long userId = extractUserIdFromAuth(authentication);
      UpdateUserGoalsCommand command = request.toCommand();
      updateUserGoalsService.updateUserGoals(userId, command);
      return ResponseEntity.ok(ApiResponse.success("目標を更新しました"));
  ```
- [ ] **@Valid による自動バリデーション**: Bean Validation 実行
- [ ] **セキュリティ考慮**: 認証ユーザーのデータのみ更新可能
- [ ] **レスポンス例**:
  ```json
  {
    "success": true,
    "message": "目標を更新しました",
    "data": null,
    "timestamp": "2025-09-22T10:00:00.123"
  }
  ```
- [ ] **解説**: 栄養目標の一括更新。バリデーション失敗時は 400 エラーを自動返却

**注記**: Auth 構成（command/service/dto/exception/validation）に完全準拠した設計

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

#### ⏭️ Task 4-5: ユーザー目標 API テスト (45 分) - Auth 構成準拠

- [ ] **UserGoalsTest.java 作成**（ドメインテスト）:
  ```java
  class UserGoalsTest {
      // 値オブジェクト、バリデーション、不変性テスト
  ```
- [ ] **UpdateUserGoalsRequestTest.java 作成**（Bean Validation テスト）:
  ```java
  class UpdateUserGoalsRequestTest {
      // @NotNull @Positive バリデーションテスト
  ```
- [ ] **UserGoalsResponseTest.java 作成**（変換テスト）:
  ```java
  class UserGoalsResponseTest {
      // fromDomain() メソッドテスト
  ```
- [ ] **UserControllerTest.java 作成**（MockMvc 統合テスト）:
  ```java
  @WebMvcTest(UserController.class)
  class UserControllerTest {
  ```
- [ ] **目標取得 API テスト**:
  - [ ] GET /api/users/goals: 正常ケース（認証済み、デフォルト値）
  - [ ] GET /api/users/goals: 未認証アクセス拒否
- [ ] **目標更新 API テスト**:
  - [ ] PUT /api/users/goals: 正常ケース（有効な目標値）
  - [ ] PUT /api/users/goals: バリデーションエラー（負の値、null 等）
  - [ ] PUT /api/users/goals: 未認証アクセス拒否
  - [ ] PUT /api/users/goals: 他ユーザーデータアクセス拒否

**注記**: Auth テスト構成（Domain/Request/Response/Controller）準拠

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

- [ ] **7 つのエンドポイント + 詳細クラス設計実装完了**
  - [×] POST /api/auth/register（認証）
  - [×] POST /api/auth/login（認証）
  - [×] POST /api/auth/refresh（認証）
  - [×] POST /api/auth/logout（認証）
  - [×] POST /api/auth/change-password（認証、AuthController に実装）
  - [ ] GET /api/users/goals（ユーザー目標取得、詳細クラス設計込み）
  - [ ] PUT /api/users/goals（ユーザー目標更新、詳細クラス設計込み）
- [ ] **ユーザー目標詳細クラス実装完了**（Auth 構成準拠）
  - [ ] UserGoals 値オブジェクト（Domain Layer）
  - [ ] UpdateUserGoalsCommand（Application Layer）
  - [ ] GetUserGoalsQueryService、UpdateUserGoalsService（Application Layer）
  - [ ] UpdateUserGoalsRequest、UserGoalsResponse（Web Layer）
  - [ ] UserController（Web Layer）
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
