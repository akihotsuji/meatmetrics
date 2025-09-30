package com.meatmetrics.meatmetrics.auth.infrastructure.persistence;

import com.meatmetrics.meatmetrics.auth.domain.account.Account;
import com.meatmetrics.meatmetrics.auth.domain.account.PasswordHash;
import com.meatmetrics.meatmetrics.sharedkernel.domain.common.Email;
import com.meatmetrics.meatmetrics.sharedkernel.domain.common.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * AccountMapperのユニットテスト
 */
@DisplayName("AccountMapper")
class AccountMapperTest {

    private AccountMapper accountMapper;

    @BeforeEach
    void setUp() {
        accountMapper = new AccountMapper();
    }

    @Nested
    @DisplayName("toDomain メソッド")
    class ToDomainMethod {

        @Test
        @DisplayName("JPAエンティティからドメインモデルに正常変換される")
        void shouldConvertEntityToDomain() {
            // Arrange
            AccountEntity entity = new AccountEntity("test@example.com", "testuser", "$2a$10$hashedpassword");
            entity.setId(1L);
            // JPA が自動的に設定するタイムスタンプをシミュレートするための変数（使用しない）
            @SuppressWarnings("unused")
            Instant now = Instant.now();

            // Act
            Account domain = accountMapper.toDomain(entity);

            // Assert
            assertThat(domain).isNotNull();
            assertThat(domain.getId()).isEqualTo(1L);
            assertThat(domain.getEmail().getValue()).isEqualTo("test@example.com");
            assertThat(domain.getUsername().getValue()).isEqualTo("testuser");
            assertThat(domain.getPasswordHash().getValue()).isEqualTo("$2a$10$hashedpassword");
            assertThat(domain.getCreatedAt()).isNotNull();
            assertThat(domain.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("nullエンティティでnullが返される")
        void shouldReturnNullForNullEntity() {
            // Arrange & Act
            Account domain = accountMapper.toDomain(null);

            // Assert
            assertThat(domain).isNull();
        }

        @Test
        @DisplayName("IDがnullのエンティティでも正常変換される")
        void shouldConvertEntityWithNullId() {
            // Arrange
            AccountEntity entity = new AccountEntity("test@example.com", "testuser", "$2a$10$hashedpassword");
            // ID は設定しない（null のまま）

            // Act
            Account domain = accountMapper.toDomain(entity);

            // Assert
            assertThat(domain).isNotNull();
            assertThat(domain.getId()).isNull();
            assertThat(domain.getEmail().getValue()).isEqualTo("test@example.com");
            assertThat(domain.getUsername().getValue()).isEqualTo("testuser");
        }
    }

    @Nested
    @DisplayName("toEntity メソッド")
    class ToEntityMethod {

        @Test
        @DisplayName("ドメインモデルからJPAエンティティに正常変換される")
        void shouldConvertDomainToEntity() {
            // Arrange
            Email email = new Email("test@example.com");
            Username username = new Username("testuser");
            PasswordHash passwordHash = PasswordHash.fromHash("$2a$10$hashedpassword");
            Account domain = new Account(email, username, passwordHash);

            // Act
            AccountEntity entity = accountMapper.toEntity(domain);

            // Assert
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isNull(); // 新規作成時はIDなし
            assertThat(entity.getEmail()).isEqualTo("test@example.com");
            assertThat(entity.getUsername()).isEqualTo("testuser");
            assertThat(entity.getPasswordHash()).isEqualTo("$2a$10$hashedpassword");
        }

        @Test
        @DisplayName("IDありドメインモデルからエンティティに変換される")
        void shouldConvertDomainWithIdToEntity() {
            // Arrange
            Email email = new Email("test@example.com");
            Username username = new Username("testuser");
            PasswordHash passwordHash = PasswordHash.fromHash("$2a$10$hashedpassword");
            Instant now = Instant.now();
            Account domain = new Account(1L, email, username, passwordHash, now, now);

            // Act
            AccountEntity entity = accountMapper.toEntity(domain);

            // Assert
            assertThat(entity).isNotNull();
            assertThat(entity.getId()).isEqualTo(1L);
            assertThat(entity.getEmail()).isEqualTo("test@example.com");
            assertThat(entity.getUsername()).isEqualTo("testuser");
            assertThat(entity.getPasswordHash()).isEqualTo("$2a$10$hashedpassword");
        }

        @Test
        @DisplayName("nullドメインでnullが返される")
        void shouldReturnNullForNullDomain() {
            // Arrange & Act
            AccountEntity entity = accountMapper.toEntity(null);

            // Assert
            assertThat(entity).isNull();
        }
    }

    @Nested
    @DisplayName("updateEntity メソッド")
    class UpdateEntityMethod {

        @Test
        @DisplayName("エンティティがドメインの値で正常更新される")
        void shouldUpdateEntityWithDomainValues() {
            // Arrange
            AccountEntity entity = new AccountEntity("old@example.com", "olduser", "$2a$10$oldhash");
            entity.setId(1L);

            Email newEmail = new Email("new@example.com");
            Username newUsername = new Username("newuser");
            PasswordHash newPasswordHash = PasswordHash.fromHash("$2a$10$newhash");
            Account domain = new Account(1L, newEmail, newUsername, newPasswordHash, 
                                       Instant.now(), Instant.now());

            // Act
            accountMapper.updateEntity(entity, domain);

            // Assert
            assertThat(entity.getId()).isEqualTo(1L); // IDは変更されない
            assertThat(entity.getEmail()).isEqualTo("new@example.com");
            assertThat(entity.getUsername()).isEqualTo("newuser");
            assertThat(entity.getPasswordHash()).isEqualTo("$2a$10$newhash");
        }

        @Test
        @DisplayName("nullエンティティで例外が発生する")
        void shouldThrowExceptionForNullEntity() {
            // Arrange
            Account domain = new Account(new Email("test@example.com"), 
                                       new Username("testuser"), 
                                       PasswordHash.fromHash("$2a$10$hash"));

            // Act & Assert
            assertThatThrownBy(() -> accountMapper.updateEntity(null, domain))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Entity cannot be null");
        }

        @Test
        @DisplayName("nullドメインで例外が発生する")
        void shouldThrowExceptionForNullDomain() {
            // Arrange
            AccountEntity entity = new AccountEntity("test@example.com", "testuser", "$2a$10$hash");

            // Act & Assert
            assertThatThrownBy(() -> accountMapper.updateEntity(entity, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Account cannot be null");
        }
    }

    @Nested
    @DisplayName("往復変換テスト")
    class RoundTripConversion {

        @Test
        @DisplayName("ドメイン → エンティティ → ドメインで同一性が保たれる")
        void shouldMaintainDataIntegrityInRoundTripConversion() {
            // Arrange
            Email originalEmail = new Email("test@example.com");
            Username originalUsername = new Username("testuser");
            PasswordHash originalPasswordHash = PasswordHash.fromHash("$2a$10$hashedpassword");
            Account originalDomain = new Account(originalEmail, originalUsername, originalPasswordHash);

            // Act - ドメイン → エンティティ → ドメイン
            AccountEntity entity = accountMapper.toEntity(originalDomain);
            Account convertedDomain = accountMapper.toDomain(entity);

            // Assert
            assertThat(convertedDomain.getEmail().getValue()).isEqualTo(originalEmail.getValue());
            assertThat(convertedDomain.getUsername().getValue()).isEqualTo(originalUsername.getValue());
            assertThat(convertedDomain.getPasswordHash().getValue()).isEqualTo(originalPasswordHash.getValue());
            // ID は null のまま保持される
            assertThat(convertedDomain.getId()).isNull();
        }

        @Test
        @DisplayName("IDありドメインの往復変換でIDが保持される")
        void shouldMaintainIdInRoundTripConversionWithId() {
            // Arrange
            Email email = new Email("test@example.com");
            Username username = new Username("testuser");
            PasswordHash passwordHash = PasswordHash.fromHash("$2a$10$hashedpassword");
            Instant now = Instant.now();
            Account originalDomain = new Account(1L, email, username, passwordHash, now, now);

            // Act - ドメイン → エンティティ → ドメイン
            AccountEntity entity = accountMapper.toEntity(originalDomain);
            Account convertedDomain = accountMapper.toDomain(entity);

            // Assert
            assertThat(convertedDomain.getId()).isEqualTo(1L);
            assertThat(convertedDomain.getEmail().getValue()).isEqualTo(email.getValue());
            assertThat(convertedDomain.getUsername().getValue()).isEqualTo(username.getValue());
            assertThat(convertedDomain.getPasswordHash().getValue()).isEqualTo(passwordHash.getValue());
        }
    }
}
