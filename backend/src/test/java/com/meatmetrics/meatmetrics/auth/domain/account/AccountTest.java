package com.meatmetrics.meatmetrics.auth.domain.account;

import com.meatmetrics.meatmetrics.auth.domain.exception.DuplicateEmailException;
import com.meatmetrics.meatmetrics.auth.domain.exception.DuplicateUsernameException;
import com.meatmetrics.meatmetrics.sharedkernel.domain.common.Email;
import com.meatmetrics.meatmetrics.sharedkernel.domain.common.Username;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

/**
 * Accountのユニットテスト
 * 
 * <p>Account集約の認証関連機能をテストします。</p>
 */
@DisplayName("Account")
class AccountTest {

    private static final Email TEST_EMAIL = new Email("test@example.com");
    private static final Username TEST_USERNAME = new Username("testuser");
    private static final PasswordHash TEST_PASSWORD_HASH = new PasswordHash("password123");

    @Nested
    @DisplayName("コンストラクタ")
    class Constructor {

        @Test
        @DisplayName("新規アカウント作成（IDなし）")
        void shouldCreateNewAccount() {
            // Arrange & Act
            Account account = new Account(TEST_EMAIL, TEST_USERNAME, TEST_PASSWORD_HASH);

            // Assert
            assertThat(account.getId()).isNull();
            assertThat(account.getEmail()).isEqualTo(TEST_EMAIL);
            assertThat(account.getUsername()).isEqualTo(TEST_USERNAME);
            assertThat(account.getPasswordHash()).isEqualTo(TEST_PASSWORD_HASH);
            assertThat(account.getCreatedAt()).isNotNull();
            assertThat(account.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("既存アカウント復元（IDあり）")
        void shouldRestoreExistingAccount() {
            // Arrange
            Long accountId = 1L;
            Instant createdAt = Instant.now().minusSeconds(3600);
            Instant updatedAt = Instant.now().minusSeconds(1800);

            // Act
            Account account = new Account(accountId, TEST_EMAIL, TEST_USERNAME, 
                                        TEST_PASSWORD_HASH, createdAt, updatedAt);

            // Assert
            assertThat(account.getId()).isEqualTo(accountId);
            assertThat(account.getEmail()).isEqualTo(TEST_EMAIL);
            assertThat(account.getUsername()).isEqualTo(TEST_USERNAME);
            assertThat(account.getPasswordHash()).isEqualTo(TEST_PASSWORD_HASH);
            assertThat(account.getCreatedAt()).isEqualTo(createdAt);
            assertThat(account.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @Test
        @DisplayName("nullのEmailで例外が発生する")
        void shouldThrowExceptionForNullEmail() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new Account(null, TEST_USERNAME, TEST_PASSWORD_HASH))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Email cannot be null");
        }

        @Test
        @DisplayName("nullのUsernameで例外が発生する")
        void shouldThrowExceptionForNullUsername() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new Account(TEST_EMAIL, null, TEST_PASSWORD_HASH))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Username cannot be null");
        }

        @Test
        @DisplayName("nullのPasswordHashで例外が発生する")
        void shouldThrowExceptionForNullPasswordHash() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new Account(TEST_EMAIL, TEST_USERNAME, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("PasswordHash cannot be null");
        }
    }

    @Nested
    @DisplayName("registerファクトリメソッド")
    class RegisterFactory {

        @Test
        @DisplayName("アカウント登録用インスタンスが作成される")
        void shouldCreateAccountForRegistration() {
            // Arrange & Act
            Account account = Account.register(TEST_EMAIL, TEST_USERNAME, TEST_PASSWORD_HASH);

            // Assert
            assertThat(account.getId()).isNull();
            assertThat(account.getEmail()).isEqualTo(TEST_EMAIL);
            assertThat(account.getUsername()).isEqualTo(TEST_USERNAME);
            assertThat(account.getPasswordHash()).isEqualTo(TEST_PASSWORD_HASH);
        }
    }

    @Nested
    @DisplayName("loginメソッド（認証）")
    class LoginMethod {

        @Test
        @DisplayName("正しいパスワードでtrueを返す")
        void shouldReturnTrueForCorrectPassword() {
            // Arrange
            String plainPassword = "password123";
            PasswordHash passwordHash = new PasswordHash(plainPassword);
            Account account = new Account(TEST_EMAIL, TEST_USERNAME, passwordHash);

            // Act & Assert
            assertThat(account.login(plainPassword)).isTrue();
        }

        @Test
        @DisplayName("間違ったパスワードでfalseを返す")
        void shouldReturnFalseForIncorrectPassword() {
            // Arrange
            Account account = new Account(TEST_EMAIL, TEST_USERNAME, TEST_PASSWORD_HASH);

            // Act & Assert
            assertThat(account.login("wrongpassword")).isFalse();
        }
    }

    @Nested
    @DisplayName("changePasswordメソッド")
    class ChangePasswordMethod {

        @Test
        @DisplayName("正しい現在パスワードでパスワード変更が成功する")
        void shouldChangePasswordWithCorrectCurrentPassword() {
            // Arrange
            String currentPassword = "password123";
            String newPassword = "newpassword456";
            PasswordHash currentHash = new PasswordHash(currentPassword);
            PasswordHash newHash = new PasswordHash(newPassword);
            Account account = new Account(TEST_EMAIL, TEST_USERNAME, currentHash);
            Instant beforeUpdate = account.getUpdatedAt();

            // Act
            account.changePassword(currentPassword, newHash);

            // Assert
            assertThat(account.getPasswordHash()).isEqualTo(newHash);
            assertThat(account.login(newPassword)).isTrue();
            assertThat(account.login(currentPassword)).isFalse();
            assertThat(account.getUpdatedAt()).isAfter(beforeUpdate);
        }

        @Test
        @DisplayName("間違った現在パスワードで例外が発生する")
        void shouldThrowExceptionForIncorrectCurrentPassword() {
            // Arrange
            Account account = new Account(TEST_EMAIL, TEST_USERNAME, TEST_PASSWORD_HASH);
            PasswordHash newHash = new PasswordHash("newpassword456");

            // Act & Assert
            assertThatThrownBy(() -> account.changePassword("wrongpassword", newHash))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Current password is incorrect");
        }
    }

    @Nested
    @DisplayName("重複チェック")
    class DuplicateValidation {

        @Test
        @DisplayName("Email重複チェック - 重複ありで例外が発生する")
        void shouldThrowExceptionWhenEmailExists() {
            // Arrange
            Account account = new Account(TEST_EMAIL, TEST_USERNAME, TEST_PASSWORD_HASH);

            // Act & Assert
            assertThatThrownBy(() -> account.validateEmailUniqueness(true))
                .isInstanceOf(DuplicateEmailException.class);
        }

        @Test
        @DisplayName("Email重複チェック - 重複なしで例外が発生しない")
        void shouldNotThrowExceptionWhenEmailDoesNotExist() {
            // Arrange
            Account account = new Account(TEST_EMAIL, TEST_USERNAME, TEST_PASSWORD_HASH);

            // Act & Assert
            assertThatCode(() -> account.validateEmailUniqueness(false))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Username重複チェック - 重複ありで例外が発生する")
        void shouldThrowExceptionWhenUsernameExists() {
            // Arrange
            Account account = new Account(TEST_EMAIL, TEST_USERNAME, TEST_PASSWORD_HASH);

            // Act & Assert
            assertThatThrownBy(() -> account.validateUsernameUniqueness(true))
                .isInstanceOf(DuplicateUsernameException.class);
        }

        @Test
        @DisplayName("Username重複チェック - 重複なしで例外が発生しない")
        void shouldNotThrowExceptionWhenUsernameDoesNotExist() {
            // Arrange
            Account account = new Account(TEST_EMAIL, TEST_USERNAME, TEST_PASSWORD_HASH);

            // Act & Assert
            assertThatCode(() -> account.validateUsernameUniqueness(false))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("equalsとhashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("同じIDのアカウントは等価")
        void shouldBeEqualForSameId() {
            // Arrange
            Long accountId = 1L;
            Account account1 = new Account(accountId, TEST_EMAIL, TEST_USERNAME, 
                                         TEST_PASSWORD_HASH, Instant.now(), Instant.now());
            Account account2 = new Account(accountId, new Email("different@example.com"), 
                                         new Username("different"), TEST_PASSWORD_HASH, 
                                         Instant.now(), Instant.now());

            // Act & Assert
            assertThat(account1).isEqualTo(account2);
            assertThat(account1.hashCode()).isEqualTo(account2.hashCode());
        }

        @Test
        @DisplayName("IDがない場合は同じEmailで等価")
        void shouldBeEqualForSameEmailWhenIdIsNull() {
            // Arrange
            Account account1 = new Account(TEST_EMAIL, TEST_USERNAME, TEST_PASSWORD_HASH);
            Account account2 = new Account(TEST_EMAIL, new Username("different"), TEST_PASSWORD_HASH);

            // Act & Assert
            assertThat(account1).isEqualTo(account2);
            assertThat(account1.hashCode()).isEqualTo(account2.hashCode());
        }

        @Test
        @DisplayName("異なるIDのアカウントは非等価")
        void shouldNotBeEqualForDifferentIds() {
            // Arrange
            Account account1 = new Account(1L, TEST_EMAIL, TEST_USERNAME, 
                                         TEST_PASSWORD_HASH, Instant.now(), Instant.now());
            Account account2 = new Account(2L, TEST_EMAIL, TEST_USERNAME, 
                                         TEST_PASSWORD_HASH, Instant.now(), Instant.now());

            // Act & Assert
            assertThat(account1).isNotEqualTo(account2);
        }

        @Test
        @DisplayName("異なるEmailのアカウントは非等価（IDがない場合）")
        void shouldNotBeEqualForDifferentEmailsWhenIdIsNull() {
            // Arrange
            Account account1 = new Account(TEST_EMAIL, TEST_USERNAME, TEST_PASSWORD_HASH);
            Account account2 = new Account(new Email("different@example.com"), TEST_USERNAME, TEST_PASSWORD_HASH);

            // Act & Assert
            assertThat(account1).isNotEqualTo(account2);
        }
    }

    @Nested
    @DisplayName("toStringメソッド")
    class ToStringMethod {

        @Test
        @DisplayName("適切な文字列表現を返す")
        void shouldReturnProperStringRepresentation() {
            // Arrange
            Long accountId = 1L;
            Instant createdAt = Instant.now();
            Instant updatedAt = Instant.now();
            Account account = new Account(accountId, TEST_EMAIL, TEST_USERNAME, 
                                        TEST_PASSWORD_HASH, createdAt, updatedAt);

            // Act
            String result = account.toString();

            // Assert
            assertThat(result).contains("Account{");
            assertThat(result).contains("id=" + accountId);
            assertThat(result).contains("email=" + TEST_EMAIL);
            assertThat(result).contains("username=" + TEST_USERNAME);
            assertThat(result).contains("createdAt=" + createdAt);
            assertThat(result).contains("updatedAt=" + updatedAt);
        }
    }
}
