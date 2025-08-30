# Today's Tasks - 認証インフラ層実装 (auth-infra-001)

## 📋 タスク概要

**auth-infra-001**: UserRepository インターフェース定義と基本実装

- **目的**: 認証機能のためのデータアクセス層実装
- **対象**: findByEmail、findByUsername、save メソッドの実装
- **期限**: 今日中

## 🎯 実装詳細

### 1. 要件分析と設計確認 ✅ (進行中)

#### 既存ドメインモデルの確認

- ✅ User 集約ルート: `com.meatmetrics.meatmetrics.domain.user.User`
- ✅ 値オブジェクト: Email, Username, PasswordHash
- ✅ エンティティ: UserGoal
- ✅ ドメイン例外: DuplicateEmailException, DuplicateUsernameException

#### UserRepository インターフェース要件

```java
public interface UserRepository {
    // 必須メソッド (今回実装)
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    User save(User user);

    // 将来実装予定
    Optional<User> findById(Long id);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
```

### 2. UserRepository インターフェース定義

#### 📁 ファイル: `backend/src/main/java/com/meatmetrics/meatmetrics/domain/user/UserRepository.java`

**実装内容:**

- [x] パッケージ: `com.meatmetrics.meatmetrics.domain.user`
- [x] ドメイン層のインターフェースとして定義
- [x] 必要最小限のメソッド定義
- [x] Javadoc による仕様記述

**メソッド仕様:**

- `findByEmail(String email)`: Email 値オブジェクトではなく文字列で検索
- `findByUsername(String username)`: Username 値オブジェクトではなく文字列で検索
- `save(User user)`: 新規作成・更新の両方に対応

### 3. UserRepositoryJpaImpl 具体実装

#### 📁 ファイル: `backend/src/main/java/com/meatmetrics/meatmetrics/infrastructure/persistence/user/UserRepositoryJpaImpl.java`

**実装内容:**

- [x] `@Repository` アノテーション
- [x] `UserRepository` インターフェースの実装
- [x] JpaRepository または EntityManager を使用
- [x] UserEntity ↔ User ドメインモデルの変換
- [x] トランザクション管理

**依存関係:**

- UserEntity (JPA エンティティ)
- UserMapper (変換ロジック)
- Spring Data JPA

### 4. 単体テスト作成

#### 📁 ファイル: `backend/src/test/java/com/meatmetrics/meatmetrics/domain/user/UserRepositoryTest.java`

**テストケース:**

- [x] findByEmail: 存在する場合・しない場合
- [x] findByUsername: 存在する場合・しない場合
- [x] save: 新規作成の場合
- [x] save: 更新の場合
- [x] 不正な引数に対する例外処理

### 5. 統合テスト作成

#### 📁 ファイル: `backend/src/test/java/com/meatmetrics/meatmetrics/infrastructure/persistence/user/UserRepositoryJpaImplIntegrationTest.java`

**テスト内容:**

- [x] Testcontainers を使用した PostgreSQL 環境
- [x] 実際のデータベースとの連携テスト
- [x] データの永続化・取得の確認
- [x] 制約違反（重複メール・ユーザー名）のテスト

## 📝 詳細実装手順

### Step 1: UserRepository インターフェース定義

#### 📂 `backend/src/main/java/com/meatmetrics/meatmetrics/domain/user/UserRepository.java`

```java
package com.meatmetrics.meatmetrics.domain.user;

import java.util.Optional;

/**
 * Userリポジトリインターフェース
 * ドメイン層のリポジトリ抽象化
 */
public interface UserRepository {

    /**
     * メールアドレスでユーザーを検索
     * @param email メールアドレス（文字列）
     * @return ユーザー（存在しない場合はOptional.empty()）
     */
    Optional<User> findByEmail(String email);

    /**
     * ユーザー名でユーザーを検索
     * @param username ユーザー名（文字列）
     * @return ユーザー（存在しない場合はOptional.empty()）
     */
    Optional<User> findByUsername(String username);

    /**
     * ユーザーを保存（新規作成・更新）
     * @param user 保存するユーザー
     * @return 保存されたユーザー（IDが付与される）
     */
    User save(User user);
}
```

**チェックリスト:**

- [ ] ドメイン層のパッケージに配置
- [ ] 技術的な依存関係なし
- [ ] Javadoc で仕様を明確化
- [ ] 戻り値に Optional を使用

### Step 2: UserEntity (JPA エンティティ) 作成

#### 📂 `backend/src/main/java/com/meatmetrics/meatmetrics/infrastructure/persistence/user/entity/UserEntity.java`

```java
package com.meatmetrics.meatmetrics.infrastructure.persistence.user.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.Instant;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_users_email", columnList = "email"),
    @Index(name = "idx_users_username", columnList = "username")
})
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", unique = true, nullable = false, length = 255)
    private String email;

    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // コンストラクタ、Getters/Setters
    protected UserEntity() {}

    public UserEntity(String email, String username, String passwordHash) {
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    // Getters and Setters...
}
```

**チェックリスト:**

- [ ] UNIQUE 制約（email, username）
- [ ] インデックス設定
- [ ] JPA 要件準拠

## 🔧 技術的考慮事項

### DDD アーキテクチャ準拠

- **ドメイン層**: インターフェースのみ、技術的依存なし
- **インフラ層**: JPA 実装、技術的詳細を含む
- **依存方向**: インフラ層 → ドメイン層

### データベース設計

- `users` テーブルの既存スキーマ活用
- `email` と `username` に UNIQUE 制約
- `password_hash` による安全なパスワード管理

### テスト戦略

- **単体テスト**: モック使用、ドメインロジックに集中
- **統合テスト**: 実際の DB、Testcontainers 使用
- **CI 対応**: GitHub Actions での自動テスト実行

## ⚠️ 注意事項

### 1. 既存コードとの整合性

- User 集約の既存実装を尊重
- 値オブジェクト（Email, Username）の使用方法確認

### 2. パフォーマンス考慮

- インデックス活用（email, username）
- N+1 問題の回避（UserGoal 関連）

### 3. セキュリティ

- パスワードハッシュの適切な処理
- SQL インジェクション対策（JPA 使用で自動対応）

## 📊 完了基準

- [ ] UserRepository インターフェース定義完了
- [ ] UserRepositoryJpaImpl 実装完了
- [ ] 単体テスト全パス
- [ ] 統合テスト全パス
- [ ] CI 環境でのテスト実行成功
- [ ] コードレビュー対応完了

---

**開始時刻**: [記録用]  
**終了予定**: [記録用]  
**実際終了**: [記録用]
