# ディレクトリ構成

## 概要

Spring Boot（バックエンド）と React（フロントエンド）を統合したプロジェクトのディレクトリ構成です。
技術スタックに基づいて、開発効率と保守性を最優先とした構成となっています。
**ドメイン駆動設計（DDD）**の設計思想に基づいて、ビジネスロジックの明確化と保守性の向上を図ります。

---

## ドメイン駆動設計（DDD）の設計思想

### 1. DDD の基本概念

#### **ドメイン（Domain）**

- **定義**: ビジネスの核心となる概念・ルール・プロセス
- **例**: ユーザー管理、認証、データ分析、レポート生成
- **目的**: ビジネス要件を技術実装から独立して理解・設計

#### **境界付けられたコンテキスト（Bounded Context）**

- **定義**: 特定のドメイン内での一貫したモデルとルール
- **例**:
  - `UserManagement`: ユーザー登録・更新・削除
  - `Authentication`: ログイン・認証・認可
  - `DataAnalysis`: データ処理・分析・可視化
- **目的**: ドメイン間の境界を明確化し、モデルの一貫性を保つ

#### **ユビキタス言語（Ubiquitous Language）**

- **定義**: 開発者とビジネス関係者が共有する共通言語
- **例**:
  - `User`（ユーザー）
  - `Authentication`（認証）
  - `DataAnalysis`（データ分析）
- **目的**: コミュニケーションの齟齬を防ぎ、要件の正確な理解を促進

### 2. DDD のレイヤー構成

#### **プレゼンテーション層（Presentation Layer）**

- **役割**: ユーザーインターフェース、API エンドポイント
- **技術**: Spring Boot Controller、React Components
- **責任**: リクエストの受信、レスポンスの返却

#### **アプリケーション層（Application Layer）**

- **役割**: ユースケースの実装、トランザクション管理
- **技術**: Spring Boot Service
- **責任**: ビジネスルールの調整、ドメインオブジェクトの操作

#### **ドメイン層（Domain Layer）**

- **役割**: ビジネスルール、エンティティ、値オブジェクト
- **技術**: Java Classes、TypeScript Interfaces
- **責任**: ビジネスロジックの実装、ドメインルールの管理

#### **インフラストラクチャ層（Infrastructure Layer）**

- **役割**: データベース、外部サービス、設定
- **技術**: Spring Data JPA、PostgreSQL、AWS
- **責任**: データの永続化、外部システムとの連携

---

## 実装時ルール

### 1. ディレクトリ構造のルール

#### **ドメイン別のパッケージ分割**

```
backend/src/main/java/com/meatmetrics/
├── user/                       # ユーザー管理ドメイン
│   ├── domain/                 # ドメイン層
│   │   ├── entity/            # エンティティ
│   │   ├── valueobject/       # 値オブジェクト
│   │   ├── repository/        # リポジトリインターフェース
│   │   └── service/           # ドメインサービス
│   ├── application/            # アプリケーション層
│   │   ├── service/           # アプリケーションサービス
│   │   ├── dto/               # データ転送オブジェクト
│   │   └── command/           # コマンドオブジェクト
│   ├── infrastructure/         # インフラストラクチャ層
│   │   ├── repository/        # リポジトリ実装
│   │   └── persistence/       # 永続化関連
│   └── presentation/           # プレゼンテーション層
│       └── controller/        # REST コントローラー
```

#### **フロントエンドのドメイン別分割**

```
frontend/src/
├── features/                   # 機能別（ドメイン別）
│   ├── user/                  # ユーザー管理機能
│   │   ├── components/        # ユーザー関連コンポーネント
│   │   ├── hooks/             # ユーザー関連フック
│   │   ├── services/          # ユーザー関連サービス
│   │   ├── stores/            # ユーザー関連ストア
│   │   └── types/             # ユーザー関連型定義
│   ├── auth/                  # 認証機能
│   │   ├── components/        # 認証関連コンポーネント
│   │   ├── hooks/             # 認証関連フック
│   │   ├── services/          # 認証関連サービス
│   │   ├── stores/            # 認証関連ストア
│   │   └── types/             # 認証関連型定義
│   └── data-analysis/         # データ分析機能
│       ├── components/        # データ分析関連コンポーネント
│       ├── hooks/             # データ分析関連フック
│       ├── services/          # データ分析関連サービス
│       ├── stores/            # データ分析関連ストア
│       └── types/             # データ分析関連型定義
├── shared/                    # 共有コンポーネント・ユーティリティ
│   ├── components/            # 共通コンポーネント
│   ├── hooks/                 # 共通フック
│   ├── services/              # 共通サービス
│   ├── stores/                # 共通ストア
│   ├── types/                 # 共通型定義
│   └── utils/                 # ユーティリティ関数
```

### 2. 命名規則

#### **Java（バックエンド）**

```java
// エンティティ
public class User { ... }
public class UserProfile { ... }

// 値オブジェクト
public class EmailAddress { ... }
public class UserId { ... }

// リポジトリインターフェース
public interface UserRepository { ... }

// ドメインサービス
public class UserDomainService { ... }

// アプリケーションサービス
public class UserApplicationService { ... }

// コントローラー
public class UserController { ... }
```

#### **TypeScript（フロントエンド）**

```typescript
// 型定義
export interface User { ... }
export interface UserProfile { ... }

// コンポーネント
export const UserList: React.FC<UserListProps> = { ... }
export const UserForm: React.FC<UserFormProps> = { ... }

// フック
export const useUser = () => { ... }
export const useUserList = () => { ... }

// サービス
export class UserService { ... }
export class AuthService { ... }

// ストア
export const useUserStore = create<UserStore>((set) => ({ ... }))
```

### 3. 依存関係のルール

#### **依存関係の方向**

```
プレゼンテーション層 → アプリケーション層 → ドメイン層 ← インフラストラクチャ層
```

#### **禁止事項**

- ドメイン層から他の層への依存
- アプリケーション層からインフラストラクチャ層への直接依存
- プレゼンテーション層からドメイン層への直接依存

#### **推奨事項**

- 依存性注入（DI）の活用
- インターフェースによる抽象化
- ファクトリーパターンの使用

### 4. データフローのルール

#### **バックエンド（Spring Boot）**

```
HTTP Request → Controller → ApplicationService → DomainService → Repository → Database
HTTP Response ← Controller ← ApplicationService ← DomainService ← Repository ← Database
```

#### **フロントエンド（React）**

```
User Action → Component → Hook → Service → Store → API → Backend
UI Update ← Component ← Hook ← Service ← Store ← API ← Backend
```

---

## ルートディレクトリ構成

```
meatmetrics/
├── .github/                    # GitHub Actions 設定
├── .vscode/                    # VS Code 設定
├── .cursor/                    # Cursor 設定
├── docs/                       # プロジェクトドキュメント
├── backend/                    # Spring Boot アプリケーション
├── frontend/                   # React アプリケーション
├── infrastructure/             # インフラ設定（Terraform等）
├── scripts/                    # 開発・デプロイ用スクリプト
├── docker-compose.yml          # ローカル開発用
├── .gitignore
├── .gitattributes
├── README.md
├── technologystack.md
└── directorystructure.md
```

---

## バックエンド（Spring Boot）構成

```
backend/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── meatmetrics/
│   │   │           ├── MeatmetricsApplication.java
│   │   │           ├── shared/                     # 共有設定・ユーティリティ
│   │   │           │   ├── config/                 # 共通設定
│   │   │           │   │   ├── SecurityConfig.java # Spring Security設定
│   │   │           │   │   ├── DatabaseConfig.java # データベース設定
│   │   │           │   │   └── SwaggerConfig.java  # OpenAPI設定
│   │   │           │   ├── exception/              # 共通例外
│   │   │           │   │   ├── GlobalExceptionHandler.java
│   │   │           │   │   ├── BusinessException.java
│   │   │           │   │   └── ValidationException.java
│   │   │           │   └── util/                   # 共通ユーティリティ
│   │   │           │       ├── DateUtils.java
│   │   │           │       ├── StringUtils.java
│   │   │           │       └── ValidationUtils.java
│   │   │           ├── user/                        # ユーザー管理ドメイン
│   │   │           │   ├── domain/                  # ドメイン層
│   │   │           │   │   ├── entity/              # エンティティ
│   │   │           │   │   │   ├── User.java
│   │   │           │   │   │   └── UserProfile.java
│   │   │           │   │   ├── valueobject/         # 値オブジェクト
│   │   │           │   │   │   ├── EmailAddress.java
│   │   │           │   │   │   └── UserId.java
│   │   │           │   │   ├── repository/          # リポジトリインターフェース
│   │   │           │   │   │   └── UserRepository.java
│   │   │           │   │   └── service/             # ドメインサービス
│   │   │           │   │       └── UserDomainService.java
│   │   │           │   ├── application/             # アプリケーション層
│   │   │           │   │   ├── service/             # アプリケーションサービス
│   │   │           │   │   │   └── UserApplicationService.java
│   │   │           │   │   ├── dto/                 # データ転送オブジェクト
│   │   │           │   │   │   ├── CreateUserRequest.java
│   │   │           │   │   │   ├── UpdateUserRequest.java
│   │   │           │   │   │   └── UserResponse.java
│   │   │           │   │   └── command/             # コマンドオブジェクト
│   │   │           │   │       ├── CreateUserCommand.java
│   │   │           │   │       └── UpdateUserCommand.java
│   │   │           │   ├── infrastructure/          # インフラストラクチャ層
│   │   │           │   │   ├── repository/          # リポジトリ実装
│   │   │           │   │   │   └── UserRepositoryImpl.java
│   │   │           │   │   └── persistence/         # 永続化関連
│   │   │           │   │       └── UserJpaEntity.java
│   │   │           │   └── presentation/            # プレゼンテーション層
│   │   │           │       └── controller/          # REST コントローラー
│   │   │           │           └── UserController.java
│   │   │           ├── auth/                         # 認証ドメイン
│   │   │           │   ├── domain/                  # ドメイン層
│   │   │           │   │   ├── entity/              # エンティティ
│   │   │           │   │   │   ├── UserSession.java
│   │   │           │   │   │   └── RefreshToken.java
│   │   │           │   │   ├── valueobject/         # 値オブジェクト
│   │   │           │   │   │   ├── JwtToken.java
│   │   │           │   │   │   └── Password.java
│   │   │           │   │   ├── repository/          # リポジトリインターフェース
│   │   │           │   │   │   ├── UserSessionRepository.java
│   │   │           │   │   │   └── RefreshTokenRepository.java
│   │   │           │   │   └── service/             # ドメインサービス
│   │   │           │   │       ├── AuthenticationService.java
│   │   │           │   │       └── PasswordService.java
│   │   │           │   ├── application/             # アプリケーション層
│   │   │           │   │   ├── service/             # アプリケーションサービス
│   │   │           │   │   │   └── AuthApplicationService.java
│   │   │           │   │   ├── dto/                 # データ転送オブジェクト
│   │   │           │   │   │   ├── LoginRequest.java
│   │   │           │   │   │   ├── LoginResponse.java
│   │   │           │   │   │   └── RefreshTokenRequest.java
│   │   │           │   │   └── command/             # コマンドオブジェクト
│   │   │           │   │       ├── LoginCommand.java
│   │   │           │   │       └── RefreshTokenCommand.java
│   │   │           │   ├── infrastructure/          # インフラストラクチャ層
│   │   │           │   │   ├── repository/          # リポジトリ実装
│   │   │           │   │   │   ├── UserSessionRepositoryImpl.java
│   │   │           │   │   │   └── RefreshTokenRepositoryImpl.java
│   │   │           │   │   └── persistence/         # 永続化関連
│   │   │           │   │       ├── UserSessionJpaEntity.java
│   │   │           │   │       └── RefreshTokenJpaEntity.java
│   │   │           │   └── presentation/            # プレゼンテーション層
│   │   │           │       └── controller/          # REST コントローラー
│   │   │           │           └── AuthController.java
│   │   │           ├── data-analysis/               # データ分析ドメイン
│   │   │           │   ├── domain/                  # ドメイン層
│   │   │           │   │   ├── entity/              # エンティティ
│   │   │           │   │   │   ├── AnalysisJob.java
│   │   │           │   │   │   └── AnalysisResult.java
│   │   │           │   │   ├── valueobject/         # 値オブジェクト
│   │   │           │   │   │   ├── AnalysisType.java
│   │   │           │   │   │   └── JobStatus.java
│   │   │           │   │   ├── repository/          # リポジトリインターフェース
│   │   │           │   │   │   ├── AnalysisJobRepository.java
│   │   │           │   │   │   └── AnalysisResultRepository.java
│   │   │           │   │   └── service/             # ドメインサービス
│   │   │           │   │       ├── DataAnalysisService.java
│   │   │           │   │       └── ReportGenerationService.java
│   │   │           │   ├── application/             # アプリケーション層
│   │   │           │   │   ├── service/             # アプリケーションサービス
│   │   │           │   │   │   └── DataAnalysisApplicationService.java
│   │   │           │   │   ├── dto/                 # データ転送オブジェクト
│   │   │           │   │   │   ├── CreateAnalysisJobRequest.java
│   │   │           │   │   │   ├── AnalysisJobResponse.java
│   │   │           │   │   │   └── AnalysisResultResponse.java
│   │   │           │   │   └── command/             # コマンドオブジェクト
│   │   │           │   │       ├── CreateAnalysisJobCommand.java
│   │   │           │   │       └── GenerateReportCommand.java
│   │   │           │   ├── infrastructure/          # インフラストラクチャ層
│   │   │           │   │   ├── repository/          # リポジトリ実装
│   │   │           │   │   │   ├── AnalysisJobRepositoryImpl.java
│   │   │           │   │   │   └── AnalysisResultRepositoryImpl.java
│   │   │           │   │   └── persistence/         # 永続化関連
│   │   │           │   │       ├── AnalysisJobJpaEntity.java
│   │   │           │   │       └── AnalysisResultJpaEntity.java
│   │   │           │   └── presentation/            # プレゼンテーション層
│   │   │           │       └── controller/          # REST コントローラー
│   │   │           │           └── DataAnalysisController.java
│   │   │           └── security/                     # セキュリティ設定
│   │   │               ├── jwt/                      # JWT関連
│   │   │               │   ├── JwtTokenProvider.java
│   │   │               │   └── JwtAuthenticationFilter.java
│   │   │               ├── oauth2/                   # OAuth2関連
│   │   │               │   └── OAuth2AuthenticationProvider.java
│   │   │               └── filter/                   # セキュリティフィルター
│   │   │                   └── SecurityFilterChain.java
│   │   └── resources/
│   │       ├── application.properties                # 基本設定
│   │       ├── application-local.properties          # ローカル環境
│   │       ├── application-dev.properties            # 開発環境
│   │       ├── application-prod.properties           # 本番環境
│   │       ├── db/
│   │       │   └── migration/                        # Flyway マイグレーション
│   │       │       ├── V1__Create_initial_tables.sql
│   │       │       ├── V2__Add_user_authentication.sql
│   │       │       └── V3__Add_data_analysis_tables.sql
│   │       └── static/                               # 静的ファイル（開発時）
│   └── test/
│       ├── java/
│       │   └── com/
│       │       └── meatmetrics/
│       │           ├── shared/                       # 共通テスト
│       │           │   ├── TestDataBuilder.java
│       │           │   └── TestUtils.java
│       │           ├── user/                         # ユーザー管理テスト
│       │           │   ├── domain/                   # ドメイン層テスト
│       │           │   ├── application/              # アプリケーション層テスト
│       │           │   ├── infrastructure/           # インフラストラクチャ層テスト
│       │           │   └── presentation/             # プレゼンテーション層テスト
│       │           ├── auth/                         # 認証テスト
│       │           │   ├── domain/                   # ドメイン層テスト
│       │           │   ├── application/              # アプリケーション層テスト
│       │           │   ├── infrastructure/           # インフラストラクチャ層テスト
│       │           │   └── presentation/             # プレゼンテーション層テスト
│       │           ├── data-analysis/                # データ分析テスト
│       │           │   ├── domain/                   # ドメイン層テスト
│       │           │   ├── application/              # アプリケーション層テスト
│       │           │   ├── infrastructure/           # インフラストラクチャ層テスト
│       │           │   └── presentation/             # プレゼンテーション層テスト
│       │           └── integration/                  # 統合テスト
│       │               ├── UserManagementIntegrationTest.java
│       │               ├── AuthenticationIntegrationTest.java
│       │               └── DataAnalysisIntegrationTest.java
│       └── resources/
│           ├── application-test.properties            # テスト環境設定
│           └── test-data/                            # テストデータ
│               ├── user/                             # ユーザー関連テストデータ
│               ├── auth/                             # 認証関連テストデータ
│               └── data-analysis/                    # データ分析関連テストデータ
├── target/                        # ビルド成果物
├── pom.xml                        # Maven 設定
├── Dockerfile                     # コンテナ化用
└── .mvn/                          # Maven Wrapper設定
```

---

## フロントエンド（React）構成

```
frontend/
├── public/
│   ├── index.html
│   ├── favicon.ico
│   ├── manifest.json
│   └── robots.txt
├── src/
│   ├── features/                 # 機能別（ドメイン別）
│   │   ├── user/                # ユーザー管理機能
│   │   │   ├── components/      # ユーザー関連コンポーネント
│   │   │   │   ├── UserList.tsx
│   │   │   │   ├── UserForm.tsx
│   │   │   │   ├── UserDetail.tsx
│   │   │   │   └── UserCard.tsx
│   │   │   ├── hooks/           # ユーザー関連フック
│   │   │   │   ├── useUser.ts
│   │   │   │   ├── useUserList.ts
│   │   │   │   └── useUserForm.ts
│   │   │   ├── services/        # ユーザー関連サービス
│   │   │   │   ├── userApi.ts
│   │   │   │   └── userValidation.ts
│   │   │   ├── stores/          # ユーザー関連ストア
│   │   │   │   ├── userStore.ts
│   │   │   │   └── userListStore.ts
│   │   │   └── types/           # ユーザー関連型定義
│   │   │       ├── user.ts
│   │   │       ├── userForm.ts
│   │   │       └── userApi.ts
│   │   ├── auth/                # 認証機能
│   │   │   ├── components/      # 認証関連コンポーネント
│   │   │   │   ├── LoginForm.tsx
│   │   │   │   ├── RegisterForm.tsx
│   │   │   │   ├── PasswordReset.tsx
│   │   │   │   └── AuthGuard.tsx
│   │   │   ├── hooks/           # 認証関連フック
│   │   │   │   ├── useAuth.ts
│   │   │   │   ├── useLogin.ts
│   │   │   │   └── useRegister.ts
│   │   │   ├── services/        # 認証関連サービス
│   │   │   │   ├── authApi.ts
│   │   │   │   └── tokenService.ts
│   │   │   ├── stores/          # 認証関連ストア
│   │   │   │   ├── authStore.ts
│   │   │   │   └── userSessionStore.ts
│   │   │   └── types/           # 認証関連型定義
│   │   │       ├── auth.ts
│   │   │       ├── login.ts
│   │   │       └── register.ts
│   │   └── data-analysis/       # データ分析機能
│   │       ├── components/      # データ分析関連コンポーネント
│   │       │   ├── AnalysisForm.tsx
│   │       │   ├── AnalysisResult.tsx
│   │       │   ├── DataChart.tsx
│   │       │   └── ReportViewer.tsx
│   │       ├── hooks/           # データ分析関連フック
│   │       │   ├── useAnalysis.ts
│   │   │   │   ├── useAnalysisResult.ts
│   │   │   │   └── useDataChart.ts
│   │       ├── services/        # データ分析関連サービス
│   │       │   ├── analysisApi.ts
│   │       │   ├── chartService.ts
│   │       │   └── reportService.ts
│   │       ├── stores/          # データ分析関連ストア
│   │       │   ├── analysisStore.ts
│   │       │   ├── resultStore.ts
│   │       │   └── chartStore.ts
│   │       └── types/           # データ分析関連型定義
│   │           ├── analysis.ts
│   │           ├── chart.ts
│   │           └── report.ts
│   ├── shared/                  # 共有コンポーネント・ユーティリティ
│   │   ├── components/          # 共通コンポーネント
│   │   │   ├── ui/              # shadcn/ui コンポーネント
│   │   │   │   ├── button.tsx
│   │   │   │   ├── input.tsx
│   │   │   │   ├── card.tsx
│   │   │   │   └── index.ts     # エクスポート用
│   │   │   ├── common/          # 共通コンポーネント
│   │   │   │   ├── Header.tsx
│   │   │   │   ├── Footer.tsx
│   │   │   │   ├── Loading.tsx
│   │   │   │   ├── ErrorBoundary.tsx
│   │   │   │   └── Modal.tsx
│   │   │   └── layout/          # レイアウト関連
│   │   │       ├── MainLayout.tsx
│   │   │       ├── AuthLayout.tsx
│   │   │       └── DashboardLayout.tsx
│   │   ├── hooks/               # 共通フック
│   │   │   ├── useApi.ts        # API呼び出しフック
│   │   │   ├── useLocalStorage.ts # ローカルストレージフック
│   │   │   ├── useDebounce.ts   # デバウンスフック
│   │   │   └── useForm.ts       # フォームフック
│   │   ├── services/            # 共通サービス
│   │   │   ├── api/             # APIクライアント
│   │   │   │   ├── client.ts    # Axios設定
│   │   │   │   ├── types.ts     # API型定義
│   │   │   │   └── interceptors.ts # インターセプター
│   │   │   └── utils/           # サービスユーティリティ
│   │   │       ├── validation.ts # バリデーション
│   │   │       ├── dateUtils.ts  # 日付ユーティリティ
│   │   │       └── stringUtils.ts # 文字列ユーティリティ
│   │   ├── stores/              # 共通ストア
│   │   │   ├── appStore.ts      # アプリケーション状態
│   │   │   ├── uiStore.ts       # UI状態
│   │   │   └── notificationStore.ts # 通知状態
│   │   ├── types/               # 共通型定義
│   │   │   ├── common.ts        # 共通型
│   │   │   ├── api.ts           # API関連型
│   │   │   └── ui.ts            # UI関連型
│   │   ├── utils/               # ユーティリティ関数
│   │   │   ├── constants.ts     # 定数
│   │   │   ├── helpers.ts       # ヘルパー関数
│   │   │   └── formatters.ts    # フォーマッター
│   │   └── styles/              # グローバルスタイル
│   │       ├── globals.css      # グローバルCSS
│   │       ├── components.css   # コンポーネントCSS
│   │       └── utilities.css    # ユーティリティCSS
│   ├── pages/                   # ページコンポーネント
│   │   ├── auth/                # 認証ページ
│   │   │   ├── LoginPage.tsx
│   │   │   └── RegisterPage.tsx
│   │   ├── dashboard/           # ダッシュボード
│   │   │   ├── DashboardPage.tsx
│   │   │   └── ProfilePage.tsx
│   │   ├── user/                # ユーザー管理ページ
│   │   │   ├── UserListPage.tsx
│   │   │   ├── UserCreatePage.tsx
│   │   │   └── UserEditPage.tsx
│   │   ├── data-analysis/       # データ分析ページ
│   │   │   ├── AnalysisPage.tsx
│   │   │   ├── ResultPage.tsx
│   │   │   └── ReportPage.tsx
│   │   └── common/              # 共通ページ
│   │       ├── HomePage.tsx
│   │       ├── NotFoundPage.tsx
│   │       └── ErrorPage.tsx
│   ├── App.tsx                  # メインアプリケーション
│   ├── main.tsx                 # エントリーポイント
│   ├── vite-env.d.ts            # Vite型定義
│   └── router.tsx               # ルーティング設定
├── dist/                         # ビルド成果物
├── package.json                  # npm設定
├── package-lock.json             # 依存関係ロック
├── vite.config.ts                # Vite設定
├── tsconfig.json                 # TypeScript設定
├── tailwind.config.js            # Tailwind CSS設定
├── postcss.config.js             # PostCSS設定
├── .eslintrc.js                  # ESLint設定
├── .prettierrc                   # Prettier設定
├── .eslintignore                 # ESLint除外設定
├── .prettierignore               # Prettier除外設定
├── .env.local                    # ローカル環境変数
├── .env.development              # 開発環境変数
├── .env.prod                     # 本番環境変数
├── Dockerfile                    # コンテナ化用
└── .dockerignore                 # Docker除外設定
```

---

## ドキュメント構成

```
docs/
├── README.md                   # プロジェクト概要
├── basic_design.md             # 基本設計書
├── detail/                     # 詳細設計書
│   ├── api_design.md           # API 設計
│   │   ├── authentication.md   # 認証API設計
│   │   ├── user_management.md  # ユーザー管理API設計
│   │   └── common_api.md       # 共通API設計
│   ├── database_design.md      # データベース設計
│   │   ├── schema.md           # スキーマ設計
│   │   ├── migration.md        # マイグレーション設計
│   │   └── indexes.md          # インデックス設計
│   ├── security_design.md      # セキュリティ設計
│   │   ├── authentication.md   # 認証設計
│   │   ├── authorization.md    # 認可設計
│   │   └── data_protection.md  # データ保護設計
│   └── ui_design.md            # UI 設計
│       ├── component_library.md # コンポーネントライブラリ
│       ├── page_layouts.md     # ページレイアウト
│       └── responsive_design.md # レスポンシブデザイン
├── deployment/                 # デプロイ関連
│   ├── aws_setup.md            # AWS 環境構築
│   │   ├── ecs_setup.md        # ECS設定
│   │   ├── rds_setup.md        # RDS設定
│   │   └── s3_cloudfront.md    # S3+CloudFront設定
│   ├── docker_setup.md         # Docker 環境構築
│   └── ci_cd_setup.md          # CI/CD設定
├── development/                 # 開発ガイド
│   ├── setup.md                # 開発環境セットアップ
│   │   ├── backend_setup.md    # バックエンド環境構築
│   │   ├── frontend_setup.md   # フロントエンド環境構築
│   │   └── database_setup.md   # データベース環境構築
│   ├── coding_standards.md     # コーディング規約
│   │   ├── java_standards.md   # Javaコーディング規約
│   │   ├── typescript_standards.md # TypeScriptコーディング規約
│   │   └── naming_conventions.md   # 命名規約
│   ├── testing.md              # テスト方針
│   │   ├── backend_testing.md  # バックエンドテスト
│   │   ├── frontend_testing.md # フロントエンドテスト
│   │   └── e2e_testing.md      # E2Eテスト
│   └── troubleshooting.md      # トラブルシューティング
├── tasks.md                    # タスク管理
└── knowledge.md                # 技術知識・学び
```

---

## インフラ構成

```
infrastructure/
├── terraform/                  # Terraform 設定
│   ├── environments/           # 環境別設定
│   │   ├── dev/               # 開発環境
│   │   │   ├── main.tf
│   │   │   ├── variables.tf
│   │   │   └── terraform.tfvars
│   │   ├── prod/              # 本番環境
│   │   │   ├── main.tf
│   │   │   ├── variables.tf
│   │   │   └── terraform.tfvars
│   │   └── prod/              # 本番環境
│   │       ├── main.tf
│   │       ├── variables.tf
│   │       └── terraform.tfvars
│   ├── modules/                # 再利用可能なモジュール
│   │   ├── vpc/               # VPC設定
│   │   │   ├── main.tf
│   │   │   ├── variables.tf
│   │   │   └── outputs.tf
│   │   ├── ecs/               # ECS設定
│   │   │   ├── main.tf
│   │   │   ├── variables.tf
│   │   │   └── outputs.tf
│   │   ├── rds/               # RDS設定
│   │   │   ├── main.tf
│   │   │   ├── variables.tf
│   │   │   └── outputs.tf
│   │   ├── s3/                # S3設定
│   │   │   ├── main.tf
│   │   │   ├── variables.tf
│   │   │   └── outputs.tf
│   │   └── cloudfront/        # CloudFront設定
│   │       ├── main.tf
│   │       ├── variables.tf
│   │       └── outputs.tf
│   ├── shared/                 # 共有設定
│   │   ├── providers.tf        # プロバイダー設定
│   │   ├── backend.tf          # バックエンド設定
│   │   └── versions.tf         # バージョン設定
│   └── README.md               # インフラ構成説明
├── docker/                     # Docker 関連設定
│   ├── nginx/                  # Nginx 設定
│   │   ├── nginx.conf          # Nginx設定ファイル
│   │   └── Dockerfile          # Nginxコンテナ
│   ├── postgres/               # PostgreSQL 設定
│   │   ├── init.sql            # 初期化SQL
│   │   └── postgresql.conf     # PostgreSQL設定
│   └── scripts/                # Docker用スクリプト
│       ├── setup-dev.sh        # 開発環境セットアップ
│       └── cleanup.sh          # クリーンアップ
└── scripts/                    # インフラ用スクリプト
    ├── deploy-infra.sh         # インフラデプロイ
    ├── backup-db.sh            # データベースバックアップ
    └── monitor-resources.sh    # リソース監視
```

---

## 開発・デプロイスクリプト

```
scripts/
├── dev/                        # 開発用スクリプト
│   ├── start-backend.sh        # バックエンド起動
│   ├── start-frontend.sh       # フロントエンド起動
│   ├── start-all.sh            # 全体起動
│   ├── start-db.sh             # データベース起動
│   └── watch-logs.sh           # ログ監視
├── build/                      # ビルド用スクリプト
│   ├── build-backend.sh        # バックエンドビルド
│   ├── build-frontend.sh       # フロントエンドビルド
│   ├── build-all.sh            # 全体ビルド
│   └── build-docker.sh         # Dockerイメージビルド
├── deploy/                     # デプロイ用スクリプト
│   ├── deploy-dev.sh           # 開発環境デプロイ
│   ├── deploy-prod.sh          # 本番環境デプロイ
│   └── rollback.sh             # ロールバック
├── test/                       # テスト用スクリプト
│   ├── run-backend-tests.sh    # バックエンドテスト実行
│   ├── run-frontend-tests.sh   # フロントエンドテスト実行
│   ├── run-e2e-tests.sh        # E2Eテスト実行
│   └── generate-test-report.sh # テストレポート生成
└── utils/                      # ユーティリティスクリプト
    ├── setup-dev-env.sh        # 開発環境セットアップ
    ├── cleanup.sh              # クリーンアップ
    ├── backup-data.sh          # データバックアップ
    ├── check-dependencies.sh   # 依存関係チェック
    └── update-versions.sh      # バージョン更新
```

---

## 環境別設定ファイル

```
├── .env.local                  # ローカル環境変数
├── .env.development            # 開発環境変数
├── .env.prod                   # 本番環境変数
├── docker-compose.yml          # ローカル開発用
├── docker-compose.override.yml # 環境別オーバーライド
├── docker-compose.dev.yml      # 開発環境用
├── docker-compose.test.yml     # テスト環境用
└── .dockerignore               # Docker除外設定
```

---

## 重要なポイント

### 1. 技術スタックに最適化された構成

- **Spring Boot**: 標準的な Maven プロジェクト構造
- **React**: モダンな開発ツール（Vite, TypeScript, Tailwind）
- **開発効率**: ホットリロード、型安全性、自動フォーマット

### 2. 段階的な開発・デプロイ

- **Phase 1**: 基本的な CRUD 機能
- **Phase 2**: 認証・認可機能
- **Phase 3**: 高度な機能・監視・ログ

### 3. 環境別設定の柔軟性

- ローカル開発: Docker Compose + PostgreSQL
- 開発環境: AWS ECS + RDS
- 本番環境: 本格的な AWS 構成

### 4. 開発者体験の向上

- 統一された開発環境
- 自動化されたビルド・テスト・デプロイ
- 明確なコーディング規約

---

## 次のステップ

1. **基本設計書の作成**（`docs/basic_design.md`）
2. **開発環境のセットアップ**（`scripts/setup-dev-env.sh`）
3. **プロジェクト構造の作成**（各ディレクトリ・ファイルの作成）
4. **CI/CD パイプラインの構築**（GitHub Actions）

---

**最終更新**: 2024 年 12 月
**更新者**: 開発チーム
