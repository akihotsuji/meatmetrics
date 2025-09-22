# 認証アプリケーション層実装 - 今日の詳細タスクリスト

## 🎯 全体概要

認証コンテキストのアプリケーション層を実装する。DDD アーキテクチャに従い、Command（DTO）と Service（@Service）の組み合わせでビジネスロジックを整理。

**実装対象**: 6 つのクラス（Command×3, Service×3）  
**作業時間見積**: 4-6 時間  
**完了目標**: 今日中にアプリケーション層の基盤完成

---

## 📋 詳細タスクリスト

### 🔧 **Phase 1: 環境準備・パッケージ構造作成** (30 分)

#### ✅ Task 1-1: プロジェクト構造の確認

- [×] 既存のドメイン層実装状況を確認
  - [×] `User`集約ルートの存在確認
  - [×] `Email`, `Username`, `PasswordHash`値オブジェクトの確認
  - [×] `UserRepository`インターフェースの確認
- [×] 現在のパッケージ構造の把握
- [×] Spring Security 設定の確認

#### ✅ Task 1-2: アプリケーション層パッケージ作成

- [×] `com.meatmetrics.meatmetrics.application.auth.command`パッケージ作成
- [×] `com.meatmetrics.meatmetrics.application.auth.service`パッケージ作成
- [×] `com.meatmetrics.meatmetrics.application.auth.dto`パッケージ作成（結果 DTO 用）

### 🏗️ **Phase 2: Command（DTO）実装** (90 分)

#### ✅ Task 2-1: RegisterUserCommand 実装 (30 分)

- [×] **ファイル作成**: `RegisterUserCommand.java`
- [×] **フィールド定義**:
  ```java
  private String email;
  private String password;
  private String username;
  ```
- [×] **バリデーションアノテーション追加**:
  - [×] `@NotNull`, `@Email`, `@Size(max=255)` for email
  - [×] `@NotNull`, `@Size(min=8, max=100)` for password
  - [×] `@NotNull`, `@Size(min=3, max=50)`, `@Pattern` for username
- [×] **ドメイン変換メソッド実装**:
  - [×] `public Email toEmail()`
  - [×] `public Username toUsername()`
  - [×] プレーンパスワードは変換せずそのまま使用
- [×] **コンストラクタ・getter・toString 実装**
- [×] **Javadoc コメント記述**

#### ✅ Task 2-2: LoginCommand 実装 (20 分)

- [×] **ファイル作成**: `LoginCommand.java`
- [×] **フィールド定義**:
  ```java
  private String email;
  private String password;
  ```
- [×] **バリデーションアノテーション追加**:
  - [×] `@NotNull`, `@Email` for email
  - [×] `@NotNull`, `@NotBlank` for password
- [×] **ドメイン変換メソッド実装**:
  - [×] `public Email toEmail()`
- [×] **基本メソッド・Javadoc 実装**

#### ✅ Task 2-3: ChangePasswordCommand 実装 (40 分)

- [×] **ファイル作成**: `ChangePasswordCommand.java`
- [×] **フィールド定義**:
  ```java
  private String currentPassword;
  private String newPassword;
  ```
- [×] **基本バリデーション追加**:
  - [×] `@NotNull`, `@NotBlank` for currentPassword
  - [×] `@NotNull`, `@Size(min=8, max=100)` for newPassword
- [x] **カスタムバリデーション実装**:
  - [x] `@DifferentPasswords`カスタムアノテーション作成
  - [x] バリデーターロジック実装（新旧パスワード相違チェック）
- [x] **基本メソッド・Javadoc 実装**

### 🚀 **Phase 3: Service 実装** (150 分)

#### ✅ Task 3-1: RegisterUserService 実装 (60 分)

- [x] **ファイル作成**: `RegisterUserService.java`
- [x] **クラス基本構造**:
  - [x] `@Service`アノテーション追加
  - [x] `@Transactional`アノテーション追加
- [x] **依存関係注入**:
  ```java
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  ```
- [x] **register メソッド実装**:
  - [x] メソッドシグネチャ定義
  - [x] email 重複チェック（`userRepository.findByEmail()`）
  - [x] username 重複チェック（`userRepository.findByUsername()`）
  - [x] `User.register()`ファクトリメソッド呼び出し
  - [x] `userRepository.save()`で永続化
  - [x] 戻り値 DTO 作成（`UserRegisteredResult`）
- [x] **例外ハンドリング**:
  - [x] 重複 email → `ConflictException`
  - [x] 重複 username → `ConflictException`
- [x] **Javadoc コメント記述**

#### ✅ Task 3-2: LoginService 実装 (60 分)

- [x] **ファイル作成**: `LoginService.java`
- [x] **クラス基本構造**:
  - [x] `@Service`アノテーション追加
- [x] **依存関係注入**:
  ```java
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtTokenProvider jwtTokenProvider;
  ```
- [x] **login メソッド実装**:
  - [x] メソッドシグネチャ定義
  - [x] `userRepository.findByEmail()`でユーザー検索
  - [x] パスワード照合（`passwordEncoder.matches()`）
  - [x] JWT トークン生成（`jwtTokenProvider.generateToken()`）
  - [x] 戻り値 DTO 作成（`LoginResult`）
- [x] **例外ハンドリング**:
  - [x] ユーザー未存在 → `UnauthorizedException`
  - [x] パスワード不一致 → `UnauthorizedException`
- [x] **Javadoc コメント記述**

#### ✅ Task 3-3: ChangePasswordService 実装 (30 分)

- [x] **ファイル作成**: `ChangePasswordService.java`
- [x] **クラス基本構造**:
  - [x] `@Service`アノテーション追加
  - [x] `@Transactional`アノテーション追加
- [x] **依存関係注入**:
  ```java
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  ```
- [x] **changePassword メソッド実装**:
  - [x] Security Context からユーザー ID 取得
  - [x] ユーザー検索・存在確認
  - [x] 現在パスワード照合
  - [x] `User.changePassword()`呼び出し
  - [x] `userRepository.save()`で変更永続化
- [x] **例外ハンドリング**:
  - [x] ユーザー未存在 → `NotFoundException`
  - [x] 現在パスワード不一致 → `UnauthorizedException`
- [x] **Javadoc コメント記述**

### 📄 **Phase 4: 結果 DTO 作成** (30 分)

#### ✅ Task 4-1: 戻り値 DTO 実装

- [x] **UserRegisteredResult.java**:
  ```java
  private Long userId;
  private String email;
  private String username;
  private LocalDateTime createdAt;
  ```
- [x] **LoginResult.java**:
  ```java
  private String accessToken;
  private String tokenType = "Bearer";
  private Long expiresIn;
  private String refreshToken; // 将来拡張用
  ```
- [x] 各 DTO の基本メソッド・Javadoc 実装

### 🧪 **Phase 5: 単体テスト実装** (120 分)

#### ✅ Task 5-1: Command バリデーションテスト (40 分)

- [ ] **RegisterUserCommandTest.java**:
  - [ ] 正常ケーステスト
  - [ ] email 形式エラーテスト
  - [ ] パスワード長さエラーテスト
  - [ ] username 形式エラーテスト
- [ ] **LoginCommandTest.java**:
  - [ ] 正常ケーステスト
  - [ ] email/password 必須チェックテスト
- [ ] **ChangePasswordCommandTest.java**:
  - [ ] 正常ケーステスト
  - [ ] 新旧パスワード同一エラーテスト

#### ✅ Task 5-2: Service ロジックテスト (80 分)

- [ ] **RegisterUserServiceTest.java**:
  - [ ] 正常登録ケース
  - [ ] email 重複エラーケース
  - [ ] username 重複エラーケース
  - [ ] ドメインオブジェクト生成確認
- [ ] **LoginServiceTest.java**:
  - [ ] 正常ログインケース
  - [ ] ユーザー未存在エラーケース
  - [ ] パスワード不一致エラーケース
  - [ ] JWT 生成確認
- [ ] **ChangePasswordServiceTest.java**:
  - [ ] 正常パスワード変更ケース
  - [ ] 現在パスワード不一致エラーケース
  - [ ] ユーザー未存在エラーケース

### ✅ **Phase 6: 統合確認・最終調整** (30 分)

#### ✅ Task 6-1: 動作確認

- [ ] 全テストの実行・Pass 確認
- [ ] コンパイルエラーの解消
- [ ] lint エラーの解消
- [ ] Javadoc 生成確認

#### ✅ Task 6-2: 設計書との整合性確認

- [ ] API 仕様書（`docs/2_detail/03_api.md`）との照合
- [ ] ドメインモデル設計書との整合性確認
- [ ] 既存のセキュリティ設定との連携確認

---

## 🔗 技術仕様・前提条件

### 📚 使用技術

- **Spring Boot**: 3.x
- **Spring Security**: JWT 認証
- **Spring Data JPA**: データアクセス
- **Bean Validation**: JSR-303 バリデーション
- **PostgreSQL**: データベース
- **JUnit 5**: テストフレームワーク

### 🏗️ 既存実装との連携

- `User`集約ルート（ドメイン層）
- `Email`, `Username`, `PasswordHash`値オブジェクト
- `UserRepository`インターフェース
- `SecurityConfig`設定

### 📐 DDD アーキテクチャ遵守

- Application 層は Domain 層に依存、Infrastructure 層に依存しない
- ドメインロジックは`User`集約内に集約
- リポジトリパターンでデータアクセス抽象化
- 例外処理はアプリケーション層で適切にハンドリング

---

## ✅ 完了基準

- [ ] **6 つのクラス実装完了**（Command×3, Service×3）
- [ ] **単体テスト実装・Pass**（最低 70%カバレッジ）
- [ ] **結果 DTO 実装完了**（2 つのクラス）
- [ ] **API 仕様書との整合性確認**
- [ ] **lint エラー・コンパイルエラー解消**
- [ ] **Javadoc コメント記述完了**
- [ ] **DDD アーキテクチャ原則遵守確認**

---

## 🚨 注意事項

1. **JWT 設定**: `JwtTokenProvider`の実装状況を事前確認
2. **例外クラス**: `ConflictException`, `UnauthorizedException`等の存在確認
3. **テストデータ**: テスト用のユビキタス言語に従ったデータ作成
4. **セキュリティ**: パスワードは必ずエンコードして保存
5. **トランザクション**: データ変更操作には`@Transactional`必須

---

**最終更新**: 2025 年 8 月 17 日  
**担当者**: 開発チーム  
**次回フォローアップ**: 明日（Phase 2 認証 Web 層実装開始）
