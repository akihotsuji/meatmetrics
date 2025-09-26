# MeatMetrics タスク管理システム 📋

## プロジェクト概要

**アプリケーション名**: MeatMetrics  
**目的**: カーニボア専用の栄養成分計算アプリ  
**主要機能**: ユーザー登録、食材検索、栄養計算、進捗追跡  
**技術スタック**: Spring Boot (Java 17) + React + PostgreSQL + AWS

---

### 注意点・前提条件 ⚠️

- PostgreSQL 16 は dev で `15432:5432` 公開
- Backend: `http://localhost:8080`
- Frontend: `http://localhost:5173`
- CORS 設定後、フロントから `/api/health` をフェッチして確認

---

## 全体タスク一覧

### ��️ **Phase 1: 基盤構築** - 進行中

**優先度**: 高  
**予想完了日**: 2024 年 12 月下旬  
**担当者**: 開発チーム

#### 完了済みタスク ✅

- [×] 技術スタック定義 (`technologystack.md`)
- [×] ディレクトリ構造設計 (`directorystructure.md`)
- [×] DDD 設計思想・実装ルール定義（ルール分離完了）
  - [×] `global.mdc` のスリム化（全体運用ルールへ集約）
  - [×] `ddd.mdc` / `tdd.mdc` の新設
  - [×] API 個別設計ドキュメント雛形の整備（`docs/2_detail/api/*.md`）
  - [×] テスト運用ガイドの整備（`docs/2_detail/testing/README.md`）
  - [×] インフラ設計書の整備（`docs/2_detail/10_infrastructure.md`）
  - [×] CI/CD 設計書の整備（`docs/2_detail/11_ci_cd.md`)
- [×] アプリケーション要件定義 (`docs/requirements.md`)
- [×] 機能仕様書作成 (`docs/detail/feature_specifications.md`)
- [×] データベース設計書作成 (`docs/detail/database_design.md`)
- [×] リリースタスク一覧作成 (`task.md`)

##### インフラ（Docker）

- [×] Docker 構成の dev/prod 分離（`infrastructure/docker/dev|prod/docker-compose.yml`）
- [×] PostgreSQL を Dockerfile 化し dev/prod でビルド切替（`infrastructure/docker/postgres/`）
- [×] 本番では DB ポート非公開、開発のみ `15432:5432` 公開に統一
- [×] ルート `infrastructure/docker/docker-compose.yml` を廃止
- [×] 手順書・記事の更新（`docs/docker/*.md`, `docs/99.Qiita/Dockerによる開発環境コンテナ化.md`）

#### 完了済みタスク ✅

- [×] 開発環境の構築
  - [×] Java 17 + Spring Boot 環境
  - [×] Node.js + React 環境
  - [×] PostgreSQL + Docker 環境
  - [×] IDE・ツール設定

#### 進行中タスク ��

- [ ] プロジェクト構造構築
  - [×] バックエンド基盤
  - [×] 共通エラーハンドリングの実装
  - [×] `@ControllerAdvice` と `@ExceptionHandler` の雛形作成
  - [×] API 共通エラーレスポンスの定義（timestamp, path, message, code）
  - [×] エラーコードの整理（`ApiErrorCode`）
  - [×] 代表例外のマッピング
    - [×] `MethodArgumentNotValidException` → 400 `VALIDATION_ERROR`
    - [×] `ConstraintViolationException` → 400 `VALIDATION_ERROR`
    - [×] `HttpMessageNotReadableException` → 400 `BAD_REQUEST`
    - [×] `MissingServletRequestParameterException` → 400 `BAD_REQUEST`
    - [×] `HttpRequestMethodNotSupportedException` → 405 `METHOD_NOT_ALLOWED`
    - [×] `NoSuchElementException` → 404 `NOT_FOUND`
    - [×] `DataIntegrityViolationException` → 409 `CONFLICT`
    - [×] `SQLException`/`DataAccessException` → 500 `DB_ERROR`
    - [×] `Exception` → 500 `INTERNAL_ERROR`
  - [×] ログ出力方針（WARN/ERROR、スタックトレースはレスポンス非掲載）
  - [×] 簡易動作確認（`GET /api/health?fail=true` で 500 と共通形式を返す）
- [×] CORS（dev）/ セキュリティヘッダー設定
  - [×] Vite プロキシで `/api` をバックエンドへ転送
  - [×] Spring Security によるセキュリティヘッダー管理
- [×] Spring Security 基盤導入
  - [×] Spring Security 依存関係追加
  - [×] 基本セキュリティ設定（SecurityConfig）
  - [×] 開発環境用プロファイル設定（application-dev.properties）
  - [×] パスワードエンコーダー（BCryptPasswordEncoder）
  - [ ] フロントエンド基盤
    - [ ] React アプリケーションの起動確認
      - [×] 事前準備・環境確認
        - [×] Node.js のバージョン確認（`node --version`）
        - [×] npm のバージョン確認（`npm --version`）
        - [×] プロジェクトディレクトリでの作業確認
        - [×] `package.json` の存在確認
        - [×] `node_modules` の存在確認（既にインストール済み）
      - [×] 依存関係の確認・インストール
        - [×] `npm install` の実行（既に完了）
        - [×] インストールエラーの有無確認（エラーなし）
        - [×] 警告メッセージの確認・対応（警告なし）
      - [×] `npm run dev` での起動とホットリロード確認
        - [×] `npm run dev` コマンドの実行
        - [×] 起動メッセージの確認
        - [×] ポート番号の確認（localhost:5173）
        - [×] 起動エラーの有無確認（エラーなし）
      - [×] 開発サーバー（localhost:5173）での動作確認
        - [×] ブラウザで `http://localhost:5173` にアクセス
        - [×] ページの表示確認
        - [×] コンソールエラーの確認
        - [×] ネットワークタブでのリクエスト状況確認
      - [×] ホットリロード機能テスト
        - [×] ソースコードの一部変更（例：タイトルテキスト）
        - [×] ブラウザでの自動更新確認
        - [×] 変更反映の速度確認
      - [×] 問題発生時の対応
        - [×] エラーログの収集・分析
        - [×] 一般的なトラブルシューティング手順の実行
        - [×] 必要に応じた設定ファイルの確認・修正
    - [ ] 基本的なルーティング設定
      - [ ] React Router の導入と設定
      - [ ] ホームページ（`/`）の作成
      - [ ] ナビゲーションコンポーネントの実装
      - [ ] 404 ページの作成
    - [ ] 共通コンポーネントの実装
      - [ ] ヘッダー・フッターの作成
      - [ ] ボタン・フォーム・カード等の基本 UI コンポーネント
      - [ ] レスポンシブデザインの基本対応
    - [×] CSS フレームワークの導入・設定 ✅ **2025-08-17 完了**
      - [×] Tailwind CSS の導入と設定 ✅ **完了**
        - [×] Tailwind CSS パッケージのインストール（tailwindcss, autoprefixer, postcss）
        - [×] Tailwind 設定ファイル（tailwind.config.js）の作成（MeatMetrics 専用テーマ設定済み）
        - [×] PostCSS 設定ファイル（postcss.config.js）の作成
        - [×] Vite 設定での Tailwind 統合確認（postcss.config.js 明示的指定）
        - [×] 既存の index.css の削除・置換（App.css も削除）
        - [×] Tailwind のベースレイヤーの適用（@tailwind base/components/utilities）
        - [×] Docker 環境でのパッケージインストール・動作確認
      - [×] shadcn/ui の導入と設定
        - [×] shadcn/ui CLI のインストール
        - [×] components.json の設定
        - [×] 基本 UI コンポーネントの導入（Button, Input, Card 等）
        - [×] カスタムテーマの設定（MeatMetrics 用カラーパレット）
      - [×] グローバルスタイルの整備
        - [×] Tailwind カスタムスタイルの作成
        - [×] レスポンシブデザインの基本設定
        - [×] ダークモード・ライトモードの設定
        - [×] フォント設定（日本語対応）
    - [×] 開発環境の整備
      - [×] ESLint・Prettier の設定確認
      - [×] TypeScript の型定義ファイル整備
      - [×] 環境変数の設定（API エンドポイント等）
- [ ] データベース設計・構築
  - [×] データベーススキーマ作成
    - [×] ユーザーテーブル（users）の設計
      - [×] 基本情報（id, email, username, password_hash, created_at, updated_at）
      - [×] 栄養目標（calorie_goal, protein_goal_g, fat_goal_g, net_carbs_goal_g）
      - [×] インデックス設計（email の unique, username の unique）
    - [×] 食材テーブル（foods）の設計
      - [×] 基本情報（id, name, category_id, created_at, updated_at）
      - [×] 栄養成分（calories_per_100g, protein_g_per_100g, fat_g_per_100g, carbohydrates_g_per_100g, fiber_g_per_100g）
      - [×] タグ管理（tags TEXT[] 配列、GIN インデックス）
      - [×] インデックス設計（name の全文検索、category_id, tags）
    - [×] カテゴリテーブル（categories）の設計
      - [×] 階層構造（id, name, parent_id, level, created_at）
      - [×] インデックス設計（parent_id, level）
    - [×] 食事記録テーブル（meals）の設計
      - [×] 基本情報（id, user_id, meal_date, meal_type, created_at, updated_at）
      - [×] 栄養集計（total_calories, total_protein_g, total_fat_g, total_net_carbs_g）
      - [×] インデックス設計（user_id, meal_date, meal_type）
    - [×] 食事詳細テーブル（meal_items）の設計
      - [×] 基本情報（id, meal_id, food_id, quantity_g, created_at, updated_at）
      - [×] 栄養成分キャッシュ（item_calories, item_protein_g, item_fat_g, item_net_carbs_g）
      - [×] 自動計算トリガー（食材 × 摂取量 → 栄養成分、meal_items→meals 集計）
      - [×] インデックス設計（meal_id, food_id, 複合インデックス）
  - [×] 初期データ投入
    - [×] カテゴリデータの作成
      - [×] 主要食材カテゴリ（肉類、魚類、卵・乳製品、野菜、調味料等）
      - [×] 階層構造の設定（親子関係）
    - [×] 食材データの作成
      - [×] 代表的な食材 50-100 件の登録
      - [×] 栄養成分データの設定（標準栄養成分表ベース）
      - [×] タグの付与（低糖質、高タンパク、低価格等）
    - [×] テストユーザーの作成
      - [×] 開発用アカウントの作成
      - [×] サンプルデータの設定（食事記録含む）
  - [×] マイグレーション設定
    - [×] Flyway の導入と設定（既存マイグレーションファイル V001-V010 活用）
      - [×] 依存関係の追加（pom.xml）
      - [×] 設定ファイルの更新（application.properties, application-dev.properties）
      - [×] 既存マイグレーションファイルの検証と調整
    - [×] 初期スキーマのマイグレーションファイル作成（完了済み）
      - [×] V001\_\_create_users_table.sql（ユーザーテーブル）
      - [×] V002\_\_insert_test_users.sql（テストユーザー）
      - [×] V003\_\_create_foods_table.sql（食材テーブル）
      - [×] V004\_\_insert_initial_foods.sql（初期食材データ）
      - [×] V005\_\_create_categories_table.sql（カテゴリテーブル）
      - [×] V006\_\_insert_initial_categories.sql（初期カテゴリデータ）
      - [×] V007\_\_add_foods_category_constraint.sql（外部キー制約）
      - [×] V008\_\_create_meals_table.sql（食事テーブル）
      - [×] V009\_\_create_meal_items_table.sql（食事項目テーブル）
      - [×] V010\_\_insert_sample_meals.sql（サンプル食事データ）
    - [×] マイグレーション実行と動作確認
      - [×] 開発環境（Docker）での初回実行確認
      - [×] flyway_schema_history テーブル確認
      - [×] 全テーブル・制約・インデックス作成確認
      - [×] アプリケーション統合テスト実行

##### インフラ（フォローアップ）

- [ ] CI/CD の Compose 参照先を `infrastructure/docker/prod` に統一
- [ ] 監視・バックアップ手順の doc 追記（DB バックアップ、ボリュームバックアップ）
- [ ] `.env` の管理方針明確化（本番は Secrets 管理を前提）

### 🔐 **Phase 2: 認証・ユーザー管理 (MVP)** - 進行中

**優先度**: 高  
**予想完了日**: 2025 年 1 月上旬  
**担当者**: 開発チーム

#### 進行中タスク 🔄

##### 🏗️ DDD 境界づけられたコンテキストの設計（最優先）

- [×] **auth-context-001**: **境界づけられたコンテキスト**の定義 - Auth/User コンテキストの境界とユビキタス言語の策定
- [×] **auth-db-migration**: **UserGoal 分離対応**の DB マイグレーション - V011-V014 のマイグレーションファイル作成完了
- [×] **auth-context-002**: **集約設計** - User 集約の不変条件とビジネスルールの定義（email 重複禁止、パスワード強度等）
- [×] **auth-context-003**: **ドメインモデル設計** - User エンティティとルート集約の責務定義

##### 🧪 認証システムのテスト実装（TDD：テストファースト）

- [×] **auth-test-001**: **[TDD-Red]** Email 値オブジェクトの失敗テスト作成（形式バリデーション）
- [×] **auth-test-002**: **[TDD-Green]** Email 値オブジェクトの実装（形式バリデーション）
- [×] **auth-test-003**: **[TDD-Red]** Password 値オブジェクトの失敗テスト作成（強度チェック、ハッシュ化）
- [×] **auth-test-004**: **[TDD-Green]** Password 値オブジェクトの実装（強度チェック、ハッシュ化ロジック）
- [×] **auth-test-005**: **[TDD-Red]** PasswordService の失敗テスト作成（ハッシュ化、照合） _PasswordHash クラスに統合_
- [×] **auth-test-006**: **[TDD-Green]** PasswordService の実装（ハッシュ化、照合） _PasswordHash クラスに統合_

##### 🏗️ 認証ドメインモデルの実装（Domain Layer）

- [×] **auth-domain-001**: **User 集約ルート**の実装 - ID、email、username、password の管理とビジネスルール
- [×] **auth-domain-002**: **ドメイン例外**の定義 - DuplicateEmailException、InvalidPasswordException、WeakPasswordException
- [×] **auth-domain-003**: **UserService**（ドメインサービス）の実装 - 重複チェックとユーザー生成ロジック _User 集約に統合_

##### 認証インフラ層の実装 (Infrastructure Layer)

- [×] **auth-infra-001**: 認証インフラ層の実装 - UserRepository インターフェース定義（findByEmail、findByUsername、save）
- [×] **auth-infra-002**: 認証インフラ層の実装 - UserEntity（JPA）の実装（@Entity、@Table、制約設定）
- [×] **auth-infra-003**: 認証インフラ層の実装 - UserMapper（Entity ↔ Domain）の実装 🔄 **進行中**
- [×] **auth-infra-004**: 認証インフラ層の実装 - JPA UserRepositoryImpl の実装（@Repository）
- [×] **auth-infra-005**: 認証インフラ層の実装 - 統合テスト（JUnit）

##### 認証アプリケーション層の実装 (Application Layer)

- [×] **auth-app-001**: 認証アプリケーション層の実装 - RegisterUserCommand（DTO）の実装
- [×] **auth-app-002**: 認証アプリケーション層の実装 - RegisterUserService（@Service）の実装
- [×] **auth-app-003**: 認証アプリケーション層の実装 - LoginCommand（DTO）の実装
- [×] **auth-app-004**: 認証アプリケーション層の実装 - LoginService（@Service、JWT 生成）の実装
- [×] **auth-app-005**: 認証アプリケーション層の実装 - ChangePasswordCommand（DTO）の実装
- [×] **auth-app-006**: 認証アプリケーション層の実装 - ChangePasswordService（@Service）の実装

##### 認証 Web 層の実装 (Presentation Layer)

- [×] **auth-web-001**: 認証 Web 層の実装 - AuthController（@RestController）の基本構造作成 ✅ **完了 2025-09-22**
- [×] **auth-web-002**: 認証 Web 層の実装 - ユーザー登録エンドポイント（POST /api/auth/register）の実装 ✅ **完了 2025-09-25**
- [×] **auth-web-003**: 認証 Web 層の実装 - ログインエンドポイント（POST /api/auth/login）の実装 ✅ **完了 2025-09-25**
- [×] **auth-web-004**: 認証 Web 層の実装 - ログアウトエンドポイント（POST /api/auth/logout）の実装 ✅ **完了 2025-09-24**
- [×] **auth-web-005**: 認証 Web 層の実装 - トークン更新エンドポイント（POST /api/auth/refresh）の実装 ✅ **完了 2025-09-25**
- [×] **auth-web-006**: 認証 Web 層の実装 - パスワード変更エンドポイント（POST /api/auth/change-password）の実装 ✅ **完了 2025-09-25**

##### Web 層共通基盤の実装

- [×] **api-common-001**: API 共通レスポンス - ApiResponse<T>クラスの実装 ✅ **完了 2025-09-22**
- [×] **auth-security-001**: SecurityConfig 更新 - 新エンドポイントの許可設定追加 ✅ **完了 2025-09-22**
- [×] **proxy-design-001**: プロキシ設計確認 - Vite/Nginx 設定による CORS 回避確認、バックエンド CORS 無効化 ✅ **完了 2025-09-22**

**注記**: CORS 設定はプロキシで解決（開発: Vite、本番: Nginx）。バックエンドでの CORS 設定は不要

##### ユーザー目標ドメインモデルの実装（Domain Layer）- MVP 基本機能

- [ ] **user-domain-001**: ユーザー目標ドメインの実装 - UserGoals 値オブジェクトの実装
  ```java
  // com.meatmetrics.meatmetrics.user.domain.UserGoals
  public class UserGoals {
      private final Integer calorie;
      private final Integer proteinG;
      private final Integer fatG;
      private final Integer netCarbsG;
  ```
- [ ] **user-domain-002**: ユーザー目標ドメインの実装 - UserGoalsValidator の実装（正の値チェック、デフォルト値）

##### ユーザー目標アプリケーション層の実装（Application Layer）- MVP 基本機能

- [ ] **user-app-001**: ユーザー目標コマンドの実装 - UpdateUserGoalsCommand の実装
  ```java
  // com.meatmetrics.meatmetrics.user.command.UpdateUserGoalsCommand
  public class UpdateUserGoalsCommand {
      private Integer calorie;
      private Integer proteinG;
      private Integer fatG;
      private Integer netCarbsG;
  ```
- [ ] **user-app-002**: ユーザー目標サービスの実装 - GetUserGoalsQueryService の実装（目標取得、デフォルト値対応）
  ```java
  // com.meatmetrics.meatmetrics.user.service.GetUserGoalsQueryService
  @Service
  public class GetUserGoalsQueryService {
      public UserGoals getUserGoals(Long userId);
  ```
- [ ] **user-app-003**: ユーザー目標サービスの実装 - UpdateUserGoalsService の実装（目標更新、バリデーション）
  ```java
  // com.meatmetrics.meatmetrics.user.service.UpdateUserGoalsService
  @Service
  public class UpdateUserGoalsService {
      public void updateUserGoals(Long userId, UpdateUserGoalsCommand command);
  ```
- [ ] **user-app-004**: ユーザー目標例外の実装 - InvalidUserGoalsException の実装（必要に応じて）

##### ユーザー目標 Web 層の実装（Presentation Layer）- MVP 基本機能

- [ ] **user-web-001**: ユーザー目標 DTO の実装 - UpdateUserGoalsRequest の実装
  ```java
  // com.meatmetrics.meatmetrics.api.user.dto.request.UpdateUserGoalsRequest
  public class UpdateUserGoalsRequest {
      @NotNull @Positive private Integer calorie;
      @NotNull @Positive private Integer protein_g;
      @NotNull @Positive private Integer fat_g;
      @NotNull @Positive private Integer net_carbs_g;
  ```
- [ ] **user-web-002**: ユーザー目標 DTO の実装 - UserGoalsResponse の実装
  ```java
  // com.meatmetrics.meatmetrics.api.user.dto.response.UserGoalsResponse
  public class UserGoalsResponse {
      private Integer calorie;
      private Integer protein_g;
      private Integer fat_g;
      private Integer net_carbs_g;
  ```
- [ ] **user-web-003**: ユーザー目標 Web 層の実装 - UserController（@RestController）の基本構造作成
  ```java
  // com.meatmetrics.meatmetrics.api.user.UserController
  @RestController
  @RequestMapping("/api/users")
  @PreAuthorize("isAuthenticated()")
  public class UserController {
  ```
- [ ] **user-web-004**: ユーザー目標 Web 層の実装 - ユーザー目標取得エンドポイント（GET /api/users/goals）の実装
- [ ] **user-web-005**: ユーザー目標 Web 層の実装 - ユーザー目標更新エンドポイント（PUT /api/users/goals）の実装

**注記**: パスワード変更は auth-web-006 で Auth コンテキストに実装済み（認証操作のため）

##### JWT 認証システムの実装（Auth コンテキスト拡張）

- [ ] **jwt-infra-001**: JWT 認証インフラの実装 - JwtService（@Service）の基本実装（生成、検証、クレーム抽出）
- [ ] **jwt-infra-002**: JWT 認証インフラの実装 - JwtAuthenticationFilter（@Component）の実装
- [ ] **jwt-infra-003**: JWT 認証インフラの実装 - TokenBlacklistService（@Service）の実装（Redis 使用想定）
- [ ] **jwt-infra-004**: JWT 認証インフラの実装 - RefreshTokenService（@Service）の実装（7 日間有効期限）

##### 認証システム設定

- [ ] **auth-config-001**: 認証設定の実装 - SecurityConfig の JWT フィルター設定
- [ ] **auth-config-002**: 認証設定の実装 - CORS 設定の更新（認証エンドポイント対応）
- [ ] **auth-config-003**: 認証設定の実装 - application.properties へ JWT 設定追加（秘密鍵、有効期限）

##### 🧪 JUnit 統合テスト・E2E テスト（TDD 後フェーズ）

- [ ] **auth-test-007**: **[JUnit 統合]** UserRepository の統合テスト - @DataJpaTest + Testcontainers PostgreSQL
- [ ] **auth-test-008**: **[JUnit 統合]** RegisterUserService の統合テスト（正常系・重複エラー・バリデーション）
- [ ] **auth-test-009**: **[JUnit 統合]** LoginService の統合テスト（正常系・認証失敗）
- [ ] **auth-test-010**: **[SpringBootTest]** AuthController の MockMvc テスト（ユーザー登録・ログイン・ログアウト・トークン更新・パスワード変更 API）
- [ ] **user-test-001**: **[SpringBootTest]** UserController の MockMvc テスト（ユーザー目標 API - GET/PUT /goals）
- [ ] **auth-test-012**: **[JUnit 統合]** JwtService の統合テスト（生成、検証、有効期限、ブラックリスト）
- [ ] **auth-test-013**: **[E2E]** 認証フロー全体テスト（ユーザー登録 → ログイン → 保護リソースアクセス → ログアウト）

##### ユーザー目標システムのテスト実装（MVP 基本機能）- Auth テスト構成準拠

- [ ] **user-test-002**: ユーザー目標ドメインテスト - UserGoals 値オブジェクトのユニットテスト（バリデーション、デフォルト値、不変性）
- [ ] **user-test-003**: ユーザー目標アプリケーション層テスト - GetUserGoalsQueryService のユニットテスト（デフォルト値対応）
- [ ] **user-test-004**: ユーザー目標アプリケーション層テスト - UpdateUserGoalsService のユニットテスト（バリデーション、例外処理）
- [ ] **user-test-005**: ユーザー目標 Web 層テスト - UpdateUserGoalsRequest の Bean Validation テスト
- [ ] **user-test-006**: ユーザー目標 Web 層テスト - UserGoalsResponse の変換テスト（fromDomain）
- [ ] **user-test-007**: ユーザー目標統合テスト - UserController の MockMvc テスト（GET/PUT /api/users/goals）
- [ ] **user-test-008**: ユーザー目標セキュリティテスト - 認証必須確認、ユーザー分離確認

##### 🧪 JUnit テストインフラ基盤構築（最優先）

- [ ] **test-infra-001**: **JUnit5+Testcontainers 基盤** - PostgreSQL コンテナ設定と AbstractIntegrationTest クラス作成
- [ ] **test-infra-002**: **テストデータ管理** - JUnit での TestDataFactory 作成（ユビキタス言語使用）
- [ ] **test-infra-003**: **MockMvc 統合テスト基盤** - SpringBootTest と MockMvc の共通設定クラス
- [ ] **test-infra-004**: **JUnit 実行設定** - Maven Surefire プラグインとテストプロファイル設定

##### 🚀 段階的 CI/CD 構築（現在進行中）

- [×] **cicd-base-001**: **CI 基盤構築** - GitHub Actions 最小構成でドメインテスト実行（Node.js 22, Java 17）✅
- [×] **cicd-base-002**: **フロントエンド基本 CI** - TypeScript 型チェック + ビルド確認（テスト実行は段階的追加）✅
- [ ] **cicd-001**: **バックエンド統合テスト追加** - PostgreSQL サービスコンテナ + Testcontainers でドメインテスト拡張
- [ ] **cicd-002**: **アプリケーション統合テスト** - @SpringBootTest による認証・API エンドポイントテスト
- [ ] **cicd-003**: **JUnit カバレッジ重視** - JaCoCo 設定とバックエンドカバレッジ 70%以上の確保
- [ ] **cicd-004**: **フロントエンドテスト復活** - Vitest + Testing Library + MSW による UI コンポーネントテスト
- [ ] **cicd-005**: **Pull Request 自動テスト** - PR 時に JUnit ユニット・統合テスト必須実行
- [ ] **cicd-006**: **セキュリティ・品質ゲート** - セキュリティスキャン、依存関係チェック、ビルド成果物管理

### 🥩 **Phase 3: 食材管理システム (MVP)** - 未着手

**優先度**: 高  
**予想完了日**: 2025 年 1 月中旬  
**担当者**: 開発チーム

#### 未着手タスク ⏳

##### 🏗️ 食材管理 DDD コンテキスト設計（最優先）

- [ ] **food-context-001**: **境界づけられたコンテキスト**の定義 - FoodCatalog コンテキストと他コンテキストとの境界
- [ ] **food-context-002**: **集約設計** - Food 集約と Category 集約の境界と不変条件（栄養成分整合性、カテゴリ階層等）
- [ ] **food-context-003**: **ユビキタス言語**の策定 - net_carbs、nutrition_per_100g 等の用語統一

##### 🧪 食材システム TDD テスト（テストファースト）

- [ ] **food-test-001**: **[TDD-Red]** NutritionInfo 値オブジェクトの失敗テスト作成（net_carbs 計算、バリデーション）
- [ ] **food-test-002**: **[TDD-Green]** NutritionInfo 値オブジェクトの実装（net_carbs = carbohydrates - fiber）
- [ ] **food-test-003**: **[TDD-Red]** FoodSearchService の失敗テスト作成（キーワード・タグ検索ロジック）
- [ ] **food-test-004**: **[TDD-Green]** FoodSearchService の実装（検索ロジック）

##### 🏗️ 食材ドメインモデルの実装（Domain Layer）

- [ ] **food-domain-001**: **Food 集約ルート**の実装 - ID、名前、カテゴリ、栄養成分とビジネスルール
- [ ] **food-domain-002**: **FoodCategory 集約**の実装 - 階層構造とカテゴリ管理ロジック
- [ ] **food-domain-003**: **FoodTag 値オブジェクト**の実装 - 複数タグ管理と検索最適化

##### 食材インフラ層の実装 (Infrastructure Layer)

- [ ] **food-infra-001**: 食材インフラ層の実装 - FoodRepository インターフェース定義
- [ ] **food-infra-002**: 食材インフラ層の実装 - JPA FoodRepositoryImpl の実装（キーワード検索、カテゴリ検索）
- [ ] **food-infra-003**: 食材インフラ層の実装 - FoodEntity（JPA）の実装（tags 配列対応、GIN インデックス）
- [ ] **food-infra-004**: 食材インフラ層の実装 - CategoryRepository の実装（階層構造クエリ）

##### 食材アプリケーション層の実装 (Application Layer)

- [ ] **food-app-001**: 食材アプリケーション層の実装 - SearchFoodsQueryService の実装
- [ ] **food-app-002**: 食材アプリケーション層の実装 - GetFoodsByCategoryQueryService の実装
- [ ] **food-app-003**: 食材アプリケーション層の実装 - GetFoodsByTagsQueryService の実装

##### 食材 Web 層の実装 (Presentation Layer)

- [ ] **food-web-001**: 食材 Web 層の実装 - FoodController（@RestController）の実装
- [ ] **food-web-002**: 食材 Web 層の実装 - 食材検索エンドポイント（GET /api/foods?keyword=...）の実装
- [ ] **food-web-003**: 食材 Web 層の実装 - カテゴリ別検索エンドポイント（GET /api/foods?category=...）の実装
- [ ] **food-web-004**: 食材 Web 層の実装 - タグ検索エンドポイント（GET /api/foods?tags=...）の実装

##### 🧪 食材システム JUnit 統合テスト

- [ ] **food-test-005**: **[JUnit 統合]** FoodRepository の統合テスト - @DataJpaTest + PostgreSQL 検索クエリ
- [ ] **food-test-006**: **[JUnit 統合]** FoodSearchService の統合テスト（キーワード・カテゴリ・タグ検索）
- [ ] **food-test-007**: **[SpringBootTest]** FoodController の MockMvc テスト（各種検索 API）

##### 🚀 食材システム CI/CD 拡張

- [ ] **food-cicd-001**: **食材 API 統合テスト追加** - CI 環境で食材検索エンドポイントの統合テスト実行
- [ ] **food-cicd-002**: **PostgreSQL 検索最適化テスト** - GIN インデックス・全文検索のパフォーマンステスト
- [ ] **food-cicd-003**: **API レスポンス検証** - 食材検索結果の形式・件数・パフォーマンス自動検証

### 🧮 **Phase 4: 栄養計算エンジン (MVP)** - 未着手

**優先度**: 高  
**予想完了日**: 2025 年 1 月下旬  
**担当者**: 開発チーム

#### 未着手タスク ⏳

##### 🏗️ 食事記録 DDD コンテキスト設計（最優先）

- [ ] **meal-context-001**: **境界づけられたコンテキスト**の定義 - MealTracking コンテキストと FoodCatalog との境界
- [ ] **meal-context-002**: **集約設計** - Meal 集約の不変条件（daily_totals 整合性、net_carbs 精度等）
- [ ] **meal-context-003**: **ユビキタス言語**の策定 - meal_records、daily_totals、achievement_rate 等の用語統一

##### 🧪 栄養計算 TDD テスト（最重要：テストファースト）

- [ ] **meal-test-001**: **[TDD-Red]** NutritionCalculatorService の失敗テスト作成（net_carbs = carbs - fiber）
- [ ] **meal-test-002**: **[TDD-Green]** NutritionCalculatorService の実装（net_carbs 計算ロジック）
- [ ] **meal-test-003**: **[TDD-Red]** MealAggregationService の失敗テスト作成（食事全体の栄養集計）
- [ ] **meal-test-004**: **[TDD-Green]** MealAggregationService の実装（栄養集計ロジック）
- [ ] **meal-test-005**: **[TDD-Red]** Quantity 値オブジェクトの失敗テスト作成（g 単位、バリデーション）
- [ ] **meal-test-006**: **[TDD-Green]** Quantity 値オブジェクトの実装（g 単位、バリデーション）

##### 🏗️ 食事記録ドメインモデルの実装（Domain Layer）

- [ ] **meal-domain-001**: **Meal 集約ルート**の実装 - 日付、食事タイプ、栄養集計とビジネスルール
- [ ] **meal-domain-002**: **MealItem 集約要素**の実装 - 食材、量、栄養計算の整合性保証
- [ ] **meal-domain-003**: **MealType 列挙型**の実装 - 朝食、昼食、夕食、間食の定義

##### 食事記録インフラ層の実装 (Infrastructure Layer)

- [ ] **meal-infra-001**: 食事記録インフラ層の実装 - MealRepository インターフェース定義
- [ ] **meal-infra-002**: 食事記録インフラ層の実装 - JPA MealRepositoryImpl の実装
- [ ] **meal-infra-003**: 食事記録インフラ層の実装 - MealEntity、MealItemEntity（JPA）の実装

##### 食事記録アプリケーション層の実装 (Application Layer)

- [ ] **meal-app-001**: 食事記録アプリケーション層の実装 - CreateMealService の実装
- [ ] **meal-app-002**: 食事記録アプリケーション層の実装 - UpdateMealService の実装
- [ ] **meal-app-003**: 食事記録アプリケーション層の実装 - DeleteMealService の実装
- [ ] **meal-app-004**: 食事記録アプリケーション層の実装 - GetMealsByDateQueryService の実装
- [ ] **meal-app-005**: 食事記録アプリケーション層の実装 - AddMealItemService の実装
- [ ] **meal-app-006**: 食事記録アプリケーション層の実装 - UpdateMealItemService の実装

##### 食事記録 Web 層の実装 (Presentation Layer)

- [ ] **meal-web-001**: 食事記録 Web 層の実装 - MealController（@RestController）の実装
- [ ] **meal-web-002**: 食事記録 Web 層の実装 - 食事記録作成エンドポイント（POST /api/meals）の実装
- [ ] **meal-web-003**: 食事記録 Web 層の実装 - 食事記録取得エンドポイント（GET /api/meals?date=...）の実装
- [ ] **meal-web-004**: 食事記録 Web 層の実装 - 食事記録更新エンドポイント（PUT /api/meals/{id}）の実装
- [ ] **meal-web-005**: 食事記録 Web 層の実装 - 食事記録削除エンドポイント（DELETE /api/meals/{id}）の実装

##### 食事記録システムのテスト実装

- [ ] **meal-test-001**: 食事記録システムのテスト - NutritionCalculatorService のユニットテスト（net_carbs 計算）
- [ ] **meal-test-002**: 食事記録システムのテスト - MealAggregationService のユニットテスト（栄養集計）
- [ ] **meal-test-003**: 食事記録システムのテスト - Meal 関連 Service のユニットテスト
- [ ] **meal-test-004**: 食事記録システムのテスト - MealController の統合テスト（CRUD 操作）

##### サマリー・分析機能の実装

- [ ] **summary-domain-001**: サマリードメインモデルの設計 - DailySummary エンティティの実装（日別栄養集計）
- [ ] **summary-domain-002**: サマリードメインモデルの設計 - AchievementRate 値オブジェクトの実装（目標達成率計算）
- [ ] **summary-domain-003**: サマリードメインサービスの設計 - SummaryCalculationService の実装

##### サマリーアプリケーション層

- [ ] **summary-app-001**: サマリーアプリケーション層の実装 - GetDailySummaryQueryService の実装
- [ ] **summary-app-002**: サマリーアプリケーション層の実装 - GetWeeklySummaryQueryService の実装（拡張）

##### サマリー Web 層

- [ ] **summary-web-001**: サマリー Web 層の実装 - SummaryController（@RestController）の実装
- [ ] **summary-web-002**: サマリー Web 層の実装 - 日別サマリーエンドポイント（GET /api/summary?date=...）の実装

##### サマリーシステムのテスト実装

- [ ] **summary-test-001**: サマリーシステムのテスト - AchievementRate 値オブジェクトのユニットテスト
- [ ] **summary-test-002**: サマリーシステムのテスト - SummaryCalculationService のユニットテスト
- [ ] **summary-test-003**: サマリーシステムのテスト - SummaryController の統合テスト

##### 🚀 栄養計算システム CI/CD 拡張

- [ ] **meal-cicd-001**: **栄養計算精度テスト** - net_carbs 計算の数値精度・境界値テストの CI 自動実行
- [ ] **meal-cicd-002**: **パフォーマンステスト** - 大量食事データでの集計処理速度・メモリ使用量テスト
- [ ] **meal-cicd-003**: **データ整合性検証** - 食事記録 → サマリー集計の整合性自動検証
- [ ] **meal-cicd-004**: **API 負荷テスト** - 食事記録・サマリーエンドポイントの同時アクセス負荷テスト

### 📊 **Phase 5: 進捗追跡・分析** - 未着手

**優先度**: 中  
**予想完了日**: 2025 年 2 月上旬  
**担当者**: 開発チーム

#### 未着手タスク ⏳

##### ダッシュボード機能の実装

- [ ] **dashboard-web-001**: ダッシュボード Web 層の実装 - DashboardController（@RestController）の実装
- [ ] **dashboard-web-002**: ダッシュボード Web 層の実装 - ダッシュボードデータエンドポイント（GET /api/dashboard）の実装
- [ ] **dashboard-app-001**: ダッシュボードアプリケーション層の実装 - GetDashboardDataQueryService の実装

##### レポート機能の実装

- [ ] **reports-domain-001**: レポートドメインモデルの設計 - NutritionReport エンティティの実装
- [ ] **reports-app-001**: レポートアプリケーション層の実装 - GenerateDailyReportQueryService の実装
- [ ] **reports-app-002**: レポートアプリケーション層の実装 - GenerateWeeklyReportQueryService の実装
- [ ] **reports-app-003**: レポートアプリケーション層の実装 - ExportReportDataQueryService の実装
- [ ] **reports-web-001**: レポート Web 層の実装 - ReportController（@RestController）の実装
- [ ] **reports-web-002**: レポート Web 層の実装 - レポート生成エンドポイント（GET /api/reports?type=...&period=...）の実装
- [ ] **reports-web-003**: レポート Web 層の実装 - データエクスポートエンドポイント（GET /api/reports/export）の実装

### 🎨 **Phase 6: UI/UX 改善** - 未着手

**優先度**: 中  
**予想完了日**: 2025 年 2 月中旬  
**担当者**: 開発チーム

#### 未着手タスク ⏳

- [ ] ユーザビリティ向上
  - [ ] レスポンシブデザイン対応
  - [ ] アクセシビリティ改善
  - [ ] パフォーマンス最適化
- [ ] ユーザー体験向上
  - [ ] 直感的な操作フロー
  - [ ] エラーハンドリング改善
  - [ ] ヘルプ・チュートリアル機能

##### 🚀 フロントエンド CI/CD 完全統合

- [ ] **frontend-cicd-001**: **Vitest 完全復活** - コンポーネント・hooks・ユーティリティのユニットテスト
- [ ] **frontend-cicd-002**: **E2E テスト導入** - Playwright による認証〜食材検索〜食事記録の主要ユーザーフロー
- [ ] **frontend-cicd-003**: **アクセシビリティ自動テスト** - axe-core による WCAG 準拠性チェック
- [ ] **frontend-cicd-004**: **パフォーマンス自動測定** - Lighthouse CI による Core Web Vitals 監視
- [ ] **frontend-cicd-005**: **ビジュアル回帰テスト** - Chromatic/Percy による UI コンポーネント変更検知

### ✅ **Phase 7: テスト・品質保証** - 未着手

**優先度**: 中  
**予想完了日**: 2025 年 2 月下旬  
**担当者**: 開発チーム

#### 未着手タスク ⏳

- [ ] テスト実装
  - [ ] ユニットテスト
  - [ ] 統合テスト
  - [ ] E2E テスト
  - [ ] パフォーマンステスト
- [ ] 品質管理
  - [ ] コードレビュー
  - [ ] セキュリティチェック
  - [ ] バグ修正・改善

##### 🚀 CI/CD 品質ゲート完全構築

- [ ] **quality-cicd-001**: **コードカバレッジ統合** - JaCoCo + c8 によるバックエンド 70%・フロントエンド 60%達成の強制
- [ ] **quality-cicd-002**: **セキュリティ完全統合** - CodeQL + Snyk + OSSAR による脆弱性スキャン強化
- [ ] **quality-cicd-003**: **品質メトリクス監視** - SonarCloud 統合による技術的負債・複雑度・重複コード監視
- [ ] **quality-cicd-004**: **依存関係セキュリティ** - Dependabot + npm audit + OWASP dependency check の自動化
- [ ] **quality-cicd-005**: **デプロイメント品質ゲート** - 全テスト合格 + カバレッジ + セキュリティクリアを必須条件化

### 🚀 **Phase 8: 本番環境構築** - 未着手

**優先度**: 低  
**予想完了日**: 2025 年 3 月上旬  
**担当者**: 開発チーム

#### 未着手タスク ⏳

- [ ] インフラ構築
  - [ ] AWS 環境構築
  - [ ] CI/CD パイプライン構築
  - [ ] 監視・ログ設定
- [ ] デプロイ・運用
  - [ ] 本番環境デプロイ
  - [ ] 運用監視設定
  - [ ] バックアップ・復旧設定

##### 🚀 本番環境 CI/CD パイプライン

- [ ] **deploy-cicd-001**: **Staging 環境 CD** - ステージング環境への自動デプロイ（main ブランチマージ時）
- [ ] **deploy-cicd-002**: **Production 環境 CD** - 本番環境への手動承認デプロイ（リリースタグ作成時）
- [ ] **deploy-cicd-003**: **Blue-Green デプロイ** - ゼロダウンタイムデプロイメント実装
- [ ] **deploy-cicd-004**: **ロールバック自動化** - デプロイ失敗時の自動ロールバック機能
- [ ] **deploy-cicd-005**: **ヘルスチェック統合** - デプロイ後の自動ヘルスチェック・スモークテスト
- [ ] **deploy-cicd-006**: **インフラ as Code** - Terraform によるインフラ変更の CI/CD 統合

### 📦 **Phase 9: リリース準備** - 未着手

**優先度**: 低  
**予想完了日**: 2025 年 3 月中旬  
**担当者**: 開発チーム

#### 未着手タスク ⏳

- [ ] 最終調整
  - [ ] 最終テスト・動作確認
  - [ ] ドキュメント整備
  - [ ] ユーザーガイド作成
- [ ] リリース
  - [ ] 本番リリース
  - [ ] ユーザーサポート準備
  - [ ] フィードバック収集体制構築

##### 🚀 継続的運用・監視 CI/CD

- [ ] **monitor-cicd-001**: **運用監視自動化** - Prometheus + Grafana によるアプリケーションメトリクス監視
- [ ] **monitor-cicd-002**: **ログ集約・分析** - ELK Stack によるログ集約・異常検知アラート
- [ ] **monitor-cicd-003**: **パフォーマンス継続監視** - APM ツールによるレスポンス時間・スループット監視
- [ ] **monitor-cicd-004**: **セキュリティ継続監視** - 脆弱性スキャン・不正アクセス検知の定期実行
- [ ] **monitor-cicd-005**: **ユーザーフィードバック自動化** - エラー発生時の自動通知・GitHub Issue 自動作成

---

### 注意点・前提条件 ⚠️

- PostgreSQL 16 は dev で `15432:5432` 公開、本番は非公開
- Spring Boot は dev で 8080 公開、prod は Nginx 経由
- データベース接続は Compose のサービス名 `postgres` を使用
- `infrastructure/docker` は dev/prod 分離済み（ルート Compose は撤去済み）

---

## 技術的課題・依存関係

### 現在の課題

- なし（基盤構築フェーズ完了）

### 今後の課題予想

- **Phase 2**: Spring Security + JWT 実装、パスワード変更フロー
- **Phase 3**: foods.tags 設計/付与運用、親子カテゴリの包含検索
- **Phase 4**: net_carbs 算出精度、サマリーのキャッシュ戦略（必要時）
- **Phase 5**: 週/月集計のパフォーマンス最適化

### 外部依存

- 食材栄養成分データベース（標準栄養成分表等）
- AWS サービス（本番環境構築時）

---

## リスク管理

### 高リスク項目

- 食材データの品質と正確性
- 栄養計算アルゴリズムの精度
- セキュリティ（認証・認可）

### 中リスク項目

- パフォーマンス（大量データ処理）
- ユーザビリティ（直感的な操作）
- データ整合性

### 低リスク項目

- インフラ構築（AWS 標準サービス）
- CI/CD（GitHub Actions 標準機能）

---

## 進捗サマリー

### 全体進捗

- **完了**: 8/9 フェーズ（88.9%）
- **進行中**: 1/9 フェーズ（11.1%）
- **未着手**: 0/9 フェーズ（0%）

### 主要マイルストーン

- ✅ **2024 年 12 月**: 基盤構築（設計・ドキュメント）
- 🎯 **2025 年 1 月**: MVP 機能完成（Phase 2-4: 認証・検索・食事記録・サマリー・目標）
- �� **2025 年 2 月**: 機能拡張・品質向上（Phase 5-7）
- �� **2025 年 3 月**: 本番リリース（Phase 8-9）

---

**最終更新**: 2025 年 9 月 22 日  
**更新者**: 開発チーム  
**次回更新予定**: 2025 年 9 月 23 日（Phase 2 認証・ユーザー Web 層実装完了）

## 📋 **細分化されたタスク概要**

### 🔍 **DDD・TDD・CI/CD 強化タスク総数**

- **Phase 2 (認証・ユーザー管理)**: 65 タスク（DDD+TDD+CI/CD 重視、プロキシ設計確認済み）
- **Phase 3 (食材管理)**: 27 タスク（DDD+TDD+検索 CI 重視）
- **Phase 4 (栄養計算・食事記録・サマリー)**: 34 タスク（TDD+精度 CI 最重要）
- **Phase 5 (進捗追跡・分析)**: 7 タスク
- **Phase 6 (UI/UX 改善)**: +5 タスク（フロントエンド CI/CD）
- **Phase 7 (品質保証)**: +5 タスク（品質ゲート CI/CD）
- **Phase 8 (本番環境)**: +6 タスク（デプロイメント CI/CD）
- **Phase 9 (リリース)**: +5 タスク（運用監視 CI/CD）
- **総計**: **149 タスク** (DDD・TDD・CI/CD 統合完了、プロキシ設計確認済み)

### 📊 **DDD・TDD・CI/CD 統合分類**

- **🏗️ DDD コンテキスト設計**: 9 タスク（最優先）
- **🧪 TDD ユニットテスト**: 18 タスク（テストファースト）
- **🧪 JUnit 統合テスト**: 21 タスク（JUnit 重視）
- **🏗️ ドメイン層**: 15 タスク（ビジネスロジック中心）
- **🔧 インフラ層**: 15 タスク
- **⚙️ アプリケーション層（Service）**: 24 タスク
- **🌐 Web 層**: 27 タスク（認証 6+ユーザー 3+共通基盤 3+その他）
- **🚀 CI/CD 基盤**: 8 タスク（段階的構築）
- **🚀 専門 CI/CD**: 26 タスク（機能別品質・デプロイ・監視）

### 🎯 **DDD・TDD・CI/CD 統合実装方針**

- **🥇 DDD 最優先**: 境界づけられたコンテキスト → 集約設計 → ドメインモデル
- **🧪 TDD 必須**: Red-Green-Refactor サイクル（特に栄養計算）
- **🔧 JUnit 重視**: バックエンド JUnit5+Testcontainers、フロントエンド段階的拡張
- **📏 テスト粒度**: ユニットテスト 70%以上、統合テスト主要シナリオ網羅
- **🗣️ ユビキタス言語**: net_carbs、meal_records、achievement_rate 等統一
- **🚀 段階的 CI/CD**: 基盤構築 → 機能別拡張 → 品質ゲート → 本番運用の 4 段階
- **🔒 品質ファースト**: 全段階でテスト・セキュリティ・パフォーマンスの 3 要素を重視
- **📊 継続改善**: CI/CD メトリクス収集によるプロセス最適化とボトルネック解決
