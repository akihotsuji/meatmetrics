# 本日のタスク統合管理 - User ドメイン認証インフラ層実装

## 📋 現在のタスク状況

### メインタスク: **auth-infra-001** - UserRepository インターフェース定義と基本実装

**目的**: 認証機能のためのデータアクセス層実装  
**対象**: findByEmail、findByUsername、save メソッドの実装  
**進捗**: **2/9 ステップ完了 (22%)**

---

## 🎯 実装ステップとチェックリスト

### ✅ **完了済み**

#### Step 1: UserRepository インターフェース定義

- [x] ✅ **完了** - `com.meatmetrics.meatmetrics.domain.user.repository.UserRepository`
- [x] ドメイン層のパッケージに配置
- [x] 技術的な依存関係なし
- [x] Javadoc で仕様を明確化
- [x] 戻り値に Optional を使用

#### Step 2: UserEntity (JPA エンティティ) 作成

- [x] ✅ **完了** - `com.meatmetrics.meatmetrics.domain.user.entity.UserEntity`
- [x] UNIQUE 制約（email, username）
- [x] インデックス設定
- [x] JPA 要件準拠

---

## 🔄 **進行中・未着手**

### Step 3: UserMapper (変換ロジック) 作成 - **次のタスク**

**ファイル**: `backend/src/main/java/com/meatmetrics/meatmetrics/infrastructure/persistence/user/mapper/UserMapper.java`

**実装内容**:

- [ ] `@Component` アノテーション
- [ ] `toDomain(UserEntity)` メソッド
- [ ] `toEntity(User)` メソッド
- [ ] `updateEntity(UserEntity, User)` メソッド
- [ ] null 安全性の確保

### Step 4: UserJpaRepository (Spring Data JPA) 作成

**ファイル**: `backend/src/main/java/com/meatmetrics/meatmetrics/infrastructure/persistence/user/repository/UserJpaRepository.java`

**実装内容**:

- [ ] `JpaRepository<UserEntity, Long>` を継承
- [ ] `@Query` で findByEmail 実装
- [ ] `@Query` で findByUsername 実装
- [ ] existsByEmail/existsByUsername メソッド

### Step 5: UserRepositoryJpaImpl (実装クラス) 作成

**ファイル**: `backend/src/main/java/com/meatmetrics/meatmetrics/infrastructure/persistence/user/UserRepositoryJpaImpl.java`

**実装内容**:

- [ ] `@Repository` `@Transactional` アノテーション
- [ ] UserRepository インターフェースの実装
- [ ] UserJpaRepository、UserMapper の依存性注入
- [ ] 新規作成・更新ロジックの実装

### Step 6: 単体テスト作成

**ファイル**: `backend/src/test/java/com/meatmetrics/meatmetrics/domain/user/UserRepositoryTest.java`

**テストケース**:

- [ ] findByEmail: 存在する場合・しない場合
- [ ] findByUsername: 存在する場合・しない場合
- [ ] save: 新規作成・更新の場合
- [ ] 不正な引数に対する例外処理

### Step 7: 統合テスト作成

**ファイル**: `backend/src/test/java/com/meatmetrics/meatmetrics/infrastructure/persistence/user/UserRepositoryJpaImplIntegrationTest.java`

**テスト内容**:

- [ ] Testcontainers を使用した PostgreSQL 環境
- [ ] 実際のデータベースとの連携テスト
- [ ] データの永続化・取得の確認
- [ ] 制約違反（重複メール・ユーザー名）のテスト

### Step 8: テスト設定ファイル

**ファイル**: `backend/src/test/resources/application-integration.properties`

**設定内容**:

- [ ] Testcontainers 用のデータベース設定
- [ ] JPA/Hibernate 設定
- [ ] Flyway 無効化

### Step 9: Docker 環境でのテスト実行

**動作確認**:

- [ ] Docker 環境でのテスト実行成功
- [ ] 単体テスト全パス
- [ ] 統合テスト全パス

---

## 🔧 技術的考慮事項

### DDD アーキテクチャ準拠

- **ドメイン層**: インターフェースのみ、技術的依存なし ✅
- **インフラ層**: JPA 実装、技術的詳細を含む
- **依存方向**: インフラ層 → ドメイン層

### 既存実装との整合性

- [x] User 集約の既存実装確認済み
- [x] 値オブジェクト（Email, Username, PasswordHash）確認済み
- [x] ドメイン例外の整合性確認済み

### テスト戦略

- **単体テスト**: モック使用、ドメインロジックに集中
- **統合テスト**: 実際の DB、Testcontainers 使用
- **CI 対応**: GitHub Actions での自動テスト実行

---

## 📊 進捗状況

**全体進捗**: 3/9 ステップ完了 (33%)

### 完了済み

1. ✅ UserRepository インターフェース定義
2. ✅ UserEntity (JPA エンティティ) 作成

### 今日の予定

3. 🔄 UserMapper 作成 ← **次のタスク**
4. ⏳ UserJpaRepository 作成
5. ⏳ UserRepositoryJpaImpl 作成
6. ⏳ 単体テスト作成
7. ⏳ 統合テスト作成

### 明日以降

8. ⏳ テスト設定ファイル
9. ⏳ Docker 環境でのテスト実行

---

## ⚠️ 注意事項

### パッケージ構造

- ドメイン層: `com.meatmetrics.meatmetrics.domain.user.*`
- インフラ層: `com.meatmetrics.meatmetrics.infrastructure.persistence.user.*`

### 既存コードとの依存関係

- User 集約: `com.meatmetrics.meatmetrics.domain.user.aggregate.User`
- 値オブジェクト: Email, Username, PasswordHash
- UserEntity: `com.meatmetrics.meatmetrics.domain.user.entity.UserEntity`

### Docker 開発環境設定

[[memory:6416796]] Docker 環境での開発を前提とし、ローカルでの開発コマンド実行は避ける

---

**開始時刻**: 記録用  
**現在のフォーカス**: Step 4 - UserJpaRepository 作成  
**今日の目標**: Step 5 まで完了（UserRepositoryJpaImpl 実装まで）
