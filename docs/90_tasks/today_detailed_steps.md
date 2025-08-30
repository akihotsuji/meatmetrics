# 認証インフラ層実装 - 詳細ステップガイド

## Step 3: UserMapper (変換ロジック) 作成

### 📂 `backend/src/main/java/com/meatmetrics/meatmetrics/infrastructure/persistence/user/mapper/UserMapper.java`

```java
package com.meatmetrics.meatmetrics.infrastructure.persistence.user.mapper;

import com.meatmetrics.meatmetrics.domain.user.aggregate.User;
import com.meatmetrics.meatmetrics.domain.user.valueobject.Email;
import com.meatmetrics.meatmetrics.domain.user.valueobject.Username;
import com.meatmetrics.meatmetrics.domain.user.valueobject.PasswordHash;
import com.meatmetrics.meatmetrics.infrastructure.persistence.user.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class UserMapper {

    public User toDomain(UserEntity entity) {
        if (entity == null) return null;

        return new User(
            entity.getId(),
            new Email(entity.getEmail()),
            new Username(entity.getUsername()),
            new PasswordHash(entity.getPasswordHash()),
            new ArrayList<>(),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    public UserEntity toEntity(User user) {
        if (user == null) return null;

        UserEntity entity = new UserEntity(
            user.getEmail().getValue(),
            user.getUsername().getValue(),
            user.getPasswordHash().getValue()
        );

        if (user.getId() != null) {
            entity.setId(user.getId());
        }

        return entity;
    }

    public void updateEntity(UserEntity entity, User user) {
        entity.setEmail(user.getEmail().getValue());
        entity.setUsername(user.getUsername().getValue());
        entity.setPasswordHash(user.getPasswordHash().getValue());
    }
}
```

## Step 4: UserJpaRepository (Spring Data JPA) 作成

### 📂 `backend/src/main/java/com/meatmetrics/meatmetrics/infrastructure/persistence/user/repository/UserJpaRepository.java`

```java
package com.meatmetrics.meatmetrics.infrastructure.persistence.user.repository;

import com.meatmetrics.meatmetrics.infrastructure.persistence.user.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    @Query("SELECT u FROM UserEntity u WHERE u.email = :email")
    Optional<UserEntity> findByEmail(@Param("email") String email);

    @Query("SELECT u FROM UserEntity u WHERE u.username = :username")
    Optional<UserEntity> findByUsername(@Param("username") String username);

    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
```

## Step 5: UserRepositoryJpaImpl (実装クラス) 作成

### 📂 `backend/src/main/java/com/meatmetrics/meatmetrics/infrastructure/persistence/user/UserRepositoryJpaImpl.java`

```java
package com.meatmetrics.meatmetrics.infrastructure.persistence.user;

import com.meatmetrics.meatmetrics.domain.user.aggregate.User;
import com.meatmetrics.meatmetrics.domain.user.repository.UserRepository;
import com.meatmetrics.meatmetrics.infrastructure.persistence.user.entity.UserEntity;
import com.meatmetrics.meatmetrics.infrastructure.persistence.user.mapper.UserMapper;
import com.meatmetrics.meatmetrics.infrastructure.persistence.user.repository.UserJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public class UserRepositoryJpaImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;

    public UserRepositoryJpaImpl(UserJpaRepository userJpaRepository, UserMapper userMapper) {
        this.userJpaRepository = userJpaRepository;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return Optional.empty();
        }

        return userJpaRepository.findByEmail(email.trim().toLowerCase())
                .map(userMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return Optional.empty();
        }

        return userJpaRepository.findByUsername(username.trim())
                .map(userMapper::toDomain);
    }

    @Override
    @Transactional
    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        UserEntity entity;

        if (user.getId() == null) {
            // 新規作成
            entity = userMapper.toEntity(user);
        } else {
            // 更新
            entity = userJpaRepository.findById(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + user.getId()));
            userMapper.updateEntity(entity, user);
        }

        UserEntity savedEntity = userJpaRepository.save(entity);
        return userMapper.toDomain(savedEntity);
    }
}
```

## Step 6: 単体テスト作成

### 📂 `backend/src/test/java/com/meatmetrics/meatmetrics/domain/user/UserRepositoryTest.java`

```java
package com.meatmetrics.meatmetrics.domain.user;

import com.meatmetrics.meatmetrics.domain.user.aggregate.User;
import com.meatmetrics.meatmetrics.domain.user.repository.UserRepository;
import com.meatmetrics.meatmetrics.domain.user.valueobject.Email;
import com.meatmetrics.meatmetrics.domain.user.valueobject.Username;
import com.meatmetrics.meatmetrics.domain.user.valueobject.PasswordHash;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserRepository 単体テスト")
class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("findByEmail: 存在するメールアドレスでユーザーが見つかる")
    void findByEmail_UserExists_ReturnsUser() {
        // Given
        String email = "test@example.com";
        User expectedUser = createTestUser();
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(expectedUser));

        // When
        Optional<User> result = userRepository.findByEmail(email);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail().getValue()).isEqualTo(email);
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("findByEmail: 存在しないメールアドレスで空のOptionalが返る")
    void findByEmail_UserNotExists_ReturnsEmpty() {
        // Given
        String email = "notfound@example.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userRepository.findByEmail(email);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findByEmail(email);
    }

    @Test
    @DisplayName("save: 新規ユーザーの保存が成功する")
    void save_NewUser_ReturnsUserWithId() {
        // Given
        User newUser = createTestUser();
        User savedUser = createTestUserWithId(1L);
        when(userRepository.save(newUser)).thenReturn(savedUser);

        // When
        User result = userRepository.save(newUser);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        verify(userRepository).save(newUser);
    }

    private User createTestUser() {
        return User.register(
            new Email("test@example.com"),
            new Username("testuser"),
            new PasswordHash("hashedpassword")
        );
    }

    private User createTestUserWithId(Long id) {
        return new User(
            id,
            new Email("test@example.com"),
            new Username("testuser"),
            new PasswordHash("hashedpassword"),
            new ArrayList<>(),
            Instant.now(),
            Instant.now()
        );
    }
}
```

## Step 7: 統合テスト作成

### 📂 `backend/src/test/java/com/meatmetrics/meatmetrics/infrastructure/persistence/user/UserRepositoryJpaImplIntegrationTest.java`

```java
package com.meatmetrics.meatmetrics.infrastructure.persistence.user;

import com.meatmetrics.meatmetrics.domain.user.aggregate.User;
import com.meatmetrics.meatmetrics.domain.user.valueobject.Email;
import com.meatmetrics.meatmetrics.domain.user.valueobject.Username;
import com.meatmetrics.meatmetrics.domain.user.valueobject.PasswordHash;
import com.meatmetrics.meatmetrics.infrastructure.persistence.user.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(locations = "classpath:application-integration.properties")
@Import({UserRepositoryJpaImpl.class, UserMapper.class})
@DisplayName("UserRepositoryJpaImpl 統合テスト")
class UserRepositoryJpaImplIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("meatmetrics_test")
            .withUsername("meatmetrics")
            .withPassword("meatmetrics123");

    @Autowired
    private UserRepositoryJpaImpl userRepository;

    @Test
    @DisplayName("save: 新規ユーザーがデータベースに保存される")
    void save_NewUser_SavedSuccessfully() {
        // Given
        User newUser = User.register(
            new Email("integration@test.com"),
            new Username("integrationuser"),
            new PasswordHash("hashedpassword123")
        );

        // When
        User savedUser = userRepository.save(newUser);

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getEmail().getValue()).isEqualTo("integration@test.com");
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("findByEmail: 保存したユーザーがメールアドレスで検索できる")
    void findByEmail_SavedUser_FoundSuccessfully() {
        // Given
        User user = User.register(
            new Email("findby@test.com"),
            new Username("findbyuser"),
            new PasswordHash("hashedpassword123")
        );
        userRepository.save(user);

        // When
        Optional<User> found = userRepository.findByEmail("findby@test.com");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail().getValue()).isEqualTo("findby@test.com");
    }
}
```

## Step 8: テスト設定ファイル

### 📂 `backend/src/test/resources/application-integration.properties`

```properties
# Testcontainers用のデータベース設定
spring.datasource.driver-class-name=org.testcontainers.jdbc.ContainerDatabaseDriver
spring.datasource.url=jdbc:tc:postgresql:16:///meatmetrics_test
spring.datasource.username=meatmetrics
spring.datasource.password=meatmetrics123

# JPA/Hibernate設定
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# Flyway無効化
spring.flyway.enabled=false

# ログレベル設定
logging.level.org.springframework.jdbc.core=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

## Step 9: Docker 環境での実行

### テスト実行コマンド

```bash
# 開発環境起動
cd infrastructure/docker/dev
docker compose up -d

# 特定テストの実行
docker compose exec backend mvn test -Dtest="UserRepositoryTest"
docker compose exec backend mvn test -Dtest="UserRepositoryJpaImplIntegrationTest"

# UserRepository関連の全テスト
docker compose exec backend mvn test -Dtest="*UserRepository*"

# テスト結果確認
docker compose exec backend find target/surefire-reports -name "*.xml"
```

## 実装チェックリスト

### 必須実装

- [ ] UserRepository インターフェース
- [ ] UserEntity (JPA エンティティ)
- [ ] UserMapper (変換ロジック)
- [ ] UserJpaRepository (Spring Data JPA)
- [ ] UserRepositoryJpaImpl (実装クラス)

### テスト

- [ ] UserRepositoryTest (単体テスト)
- [ ] UserRepositoryJpaImplIntegrationTest (統合テスト)
- [ ] application-integration.properties

### 動作確認

- [ ] Docker 環境でのテスト実行成功
- [ ] 単体テスト全パス
- [ ] 統合テスト全パス

---

**作業順序**: Step 1 → Step 2 → Step 3 → Step 4 → Step 5 → Step 6 → Step 7 → Step 8 → Step 9
