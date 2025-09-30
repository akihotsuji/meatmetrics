package com.meatmetrics.meatmetrics.auth.infrastructure.persistence;

import com.meatmetrics.meatmetrics.PostgreSQLTestBase;
import com.meatmetrics.meatmetrics.auth.domain.account.Account;
import com.meatmetrics.meatmetrics.auth.domain.account.PasswordHash;
import com.meatmetrics.meatmetrics.auth.domain.repository.AccountRepository;
import com.meatmetrics.meatmetrics.sharedkernel.domain.common.Email;
import com.meatmetrics.meatmetrics.sharedkernel.domain.common.Username;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import org.springframework.dao.InvalidDataAccessApiUsageException;

/**
 * AccountRepositoryJpaImplの統合テスト
 * 
 * <p>PostgreSQLとの実際の接続を使用してテストします。</p>
 */
@SpringBootTest
@ActiveProfiles("integration")
@Transactional
@DisplayName("AccountRepositoryJpaImpl")
class AccountRepositoryJpaImplTest extends PostgreSQLTestBase {

    @Autowired
    private AccountRepository accountRepository;

    @Nested
    @DisplayName("save メソッド")
    class SaveMethod {

        @Test
        @DisplayName("新規アカウントが正常に保存される")
        void shouldSaveNewAccount() {
            // Arrange
            Email email = new Email("test@example.com");
            Username username = new Username("testuser");
            PasswordHash passwordHash = new PasswordHash("password123");
            Account account = Account.register(email, username, passwordHash);

            // Act
            Account savedAccount = accountRepository.save(account);

            // Assert
            assertThat(savedAccount).isNotNull();
            assertThat(savedAccount.getId()).isNotNull();
            assertThat(savedAccount.getEmail()).isEqualTo(email);
            assertThat(savedAccount.getUsername()).isEqualTo(username);
            assertThat(savedAccount.getPasswordHash().getValue()).isEqualTo(passwordHash.getValue());
            assertThat(savedAccount.getCreatedAt()).isNotNull();
            assertThat(savedAccount.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("既存アカウントが正常に更新される")
        void shouldUpdateExistingAccount() {
            // Arrange - 最初にアカウントを保存
            Email originalEmail = new Email("original@example.com");
            Username originalUsername = new Username("originaluser");
            PasswordHash originalPasswordHash = new PasswordHash("original123");
            Account originalAccount = Account.register(originalEmail, originalUsername, originalPasswordHash);
            Account savedAccount = accountRepository.save(originalAccount);

            // パスワードを変更
            PasswordHash newPasswordHash = new PasswordHash("newpassword456");
            savedAccount.changePassword("original123", newPasswordHash);

            // Act
            Account updatedAccount = accountRepository.save(savedAccount);

            // Assert
            assertThat(updatedAccount.getId()).isEqualTo(savedAccount.getId());
            assertThat(updatedAccount.getPasswordHash().getValue()).isEqualTo(newPasswordHash.getValue());
            assertThat(updatedAccount.getUpdatedAt()).isAfter(savedAccount.getCreatedAt());
        }

        @Test
        @DisplayName("nullアカウントで例外が発生する")
        void shouldThrowExceptionForNullAccount() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> accountRepository.save(null))
                .isInstanceOf(InvalidDataAccessApiUsageException.class)
                .hasMessageContaining("User cannot be null");
        }
    }

    @Nested
    @DisplayName("findByEmail メソッド")
    class FindByEmailMethod {

        @Test
        @DisplayName("存在するメールアドレスでアカウントが見つかる")
        void shouldFindAccountByExistingEmail() {
            // Arrange - テストデータを保存
            Email email = new Email("findme@example.com");
            Username username = new Username("findmeuser");
            PasswordHash passwordHash = new PasswordHash("password123");
            Account account = Account.register(email, username, passwordHash);
            Account savedAccount = accountRepository.save(account);

            // Act
            Optional<Account> foundAccount = accountRepository.findByEmail(email);

            // Assert
            assertThat(foundAccount).isPresent();
            assertThat(foundAccount.get().getId()).isEqualTo(savedAccount.getId());
            assertThat(foundAccount.get().getEmail()).isEqualTo(email);
            assertThat(foundAccount.get().getUsername()).isEqualTo(username);
        }

        @Test
        @DisplayName("存在しないメールアドレスで空のOptionalが返される")
        void shouldReturnEmptyForNonExistentEmail() {
            // Arrange
            Email nonExistentEmail = new Email("nonexistent@example.com");

            // Act
            Optional<Account> foundAccount = accountRepository.findByEmail(nonExistentEmail);

            // Assert
            assertThat(foundAccount).isEmpty();
        }

        @Test
        @DisplayName("nullメールアドレスで例外が発生する")
        void shouldThrowExceptionForNullEmail() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> accountRepository.findByEmail(null))
                .isInstanceOf(InvalidDataAccessApiUsageException.class)
                .hasMessageContaining("Email cannot be null");
        }
    }

    @Nested
    @DisplayName("findByUsername メソッド")
    class FindByUsernameMethod {

        @Test
        @DisplayName("存在するユーザー名でアカウントが見つかる")
        void shouldFindAccountByExistingUsername() {
            // Arrange - テストデータを保存
            Email email = new Email("user@example.com");
            Username username = new Username("uniqueuser");
            PasswordHash passwordHash = new PasswordHash("password123");
            Account account = Account.register(email, username, passwordHash);
            Account savedAccount = accountRepository.save(account);

            // Act
            Optional<Account> foundAccount = accountRepository.findByUsername(username);

            // Assert
            assertThat(foundAccount).isPresent();
            assertThat(foundAccount.get().getId()).isEqualTo(savedAccount.getId());
            assertThat(foundAccount.get().getEmail()).isEqualTo(email);
            assertThat(foundAccount.get().getUsername()).isEqualTo(username);
        }

        @Test
        @DisplayName("存在しないユーザー名で空のOptionalが返される")
        void shouldReturnEmptyForNonExistentUsername() {
            // Arrange
            Username nonExistentUsername = new Username("nonexistentuser");

            // Act
            Optional<Account> foundAccount = accountRepository.findByUsername(nonExistentUsername);

            // Assert
            assertThat(foundAccount).isEmpty();
        }

        @Test
        @DisplayName("nullユーザー名で例外が発生する")
        void shouldThrowExceptionForNullUsername() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> accountRepository.findByUsername(null))
                .isInstanceOf(InvalidDataAccessApiUsageException.class)
                .hasMessageContaining("Username cannot be null");
        }
    }

    @Nested
    @DisplayName("findById メソッド")
    class FindByIdMethod {

        @Test
        @DisplayName("存在するIDでアカウントが見つかる")
        void shouldFindAccountByExistingId() {
            // Arrange - テストデータを保存
            Email email = new Email("id@example.com");
            Username username = new Username("iduser");
            PasswordHash passwordHash = new PasswordHash("password123");
            Account account = Account.register(email, username, passwordHash);
            Account savedAccount = accountRepository.save(account);

            // Act
            Optional<Account> foundAccount = accountRepository.findById(savedAccount.getId());

            // Assert
            assertThat(foundAccount).isPresent();
            assertThat(foundAccount.get().getId()).isEqualTo(savedAccount.getId());
            assertThat(foundAccount.get().getEmail()).isEqualTo(email);
            assertThat(foundAccount.get().getUsername()).isEqualTo(username);
        }

        @Test
        @DisplayName("存在しないIDで空のOptionalが返される")
        void shouldReturnEmptyForNonExistentId() {
            // Arrange
            Long nonExistentId = 999999L;

            // Act
            Optional<Account> foundAccount = accountRepository.findById(nonExistentId);

            // Assert
            assertThat(foundAccount).isEmpty();
        }

        @Test
        @DisplayName("nullIDで例外が発生する")
        void shouldThrowExceptionForNullId() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> accountRepository.findById(null))
                .isInstanceOf(InvalidDataAccessApiUsageException.class)
                .hasMessageContaining("UserId cannot be null");
        }
    }

    @Nested
    @DisplayName("一意性制約テスト")
    class UniquenessConstraints {

        @Test
        @DisplayName("重複メールアドレスで制約違反例外が発生する")
        void shouldThrowExceptionForDuplicateEmail() {
            // Arrange - 最初のアカウントを保存
            Email duplicateEmail = new Email("duplicate@example.com");
            Username username1 = new Username("user1");
            Username username2 = new Username("user2");
            PasswordHash passwordHash = new PasswordHash("password123");
            
            Account account1 = Account.register(duplicateEmail, username1, passwordHash);
            accountRepository.save(account1);

            Account account2 = Account.register(duplicateEmail, username2, passwordHash);

            // Act & Assert
            assertThatThrownBy(() -> accountRepository.save(account2))
                .isInstanceOf(Exception.class); // DB制約違反例外
        }

        @Test
        @DisplayName("重複ユーザー名で制約違反例外が発生する")
        void shouldThrowExceptionForDuplicateUsername() {
            // Arrange - 最初のアカウントを保存
            Email email1 = new Email("email1@example.com");
            Email email2 = new Email("email2@example.com");
            Username duplicateUsername = new Username("duplicateuser");
            PasswordHash passwordHash = new PasswordHash("password123");
            
            Account account1 = Account.register(email1, duplicateUsername, passwordHash);
            accountRepository.save(account1);

            Account account2 = Account.register(email2, duplicateUsername, passwordHash);

            // Act & Assert
            assertThatThrownBy(() -> accountRepository.save(account2))
                .isInstanceOf(Exception.class); // DB制約違反例外
        }
    }

    @Nested
    @DisplayName("データ整合性テスト")
    class DataIntegrityTests {

        @Test
        @DisplayName("保存→検索で同一データが取得される")
        void shouldMaintainDataIntegrityInSaveAndFind() {
            // Arrange
            Email email = new Email("integrity@example.com");
            Username username = new Username("integrityuser");
            PasswordHash passwordHash = new PasswordHash("password123");
            Account account = Account.register(email, username, passwordHash);

            // Act - 保存して再検索
            Account savedAccount = accountRepository.save(account);
            Optional<Account> foundByEmail = accountRepository.findByEmail(email);
            Optional<Account> foundByUsername = accountRepository.findByUsername(username);
            Optional<Account> foundById = accountRepository.findById(savedAccount.getId());

            // Assert - 全ての検索方法で同一データが取得される
            assertThat(foundByEmail).isPresent();
            assertThat(foundByUsername).isPresent();
            assertThat(foundById).isPresent();

            Account accountByEmail = foundByEmail.get();
            Account accountByUsername = foundByUsername.get();
            Account accountById = foundById.get();

            assertThat(accountByEmail.getId()).isEqualTo(savedAccount.getId());
            assertThat(accountByUsername.getId()).isEqualTo(savedAccount.getId());
            assertThat(accountById.getId()).isEqualTo(savedAccount.getId());

            assertThat(accountByEmail.getEmail()).isEqualTo(email);
            assertThat(accountByUsername.getEmail()).isEqualTo(email);
            assertThat(accountById.getEmail()).isEqualTo(email);
        }
    }
}
