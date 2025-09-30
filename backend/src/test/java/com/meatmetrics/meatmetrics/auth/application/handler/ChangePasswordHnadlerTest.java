package com.meatmetrics.meatmetrics.auth.application.handler;

import com.meatmetrics.meatmetrics.auth.application.command.ChangePasswordCommand;
import com.meatmetrics.meatmetrics.auth.domain.account.Account;
import com.meatmetrics.meatmetrics.auth.domain.account.PasswordHash;
import com.meatmetrics.meatmetrics.auth.domain.exception.AuthenticationException;
import com.meatmetrics.meatmetrics.auth.domain.exception.WeakPasswordException;
import com.meatmetrics.meatmetrics.auth.domain.repository.AccountRepository;
import com.meatmetrics.meatmetrics.sharedkernel.domain.common.Email;
import com.meatmetrics.meatmetrics.sharedkernel.domain.common.Username;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * ChangePasswordHnadlerのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ChangePasswordHnadler")
class ChangePasswordHnadlerTest {

    @Mock
    private AccountRepository accountRepository;

    private ChangePasswordHnadler changePasswordHandler;

    @BeforeEach
    void setUp() {
        changePasswordHandler = new ChangePasswordHnadler(accountRepository);
    }

    @Nested
    @DisplayName("changePassword メソッド")
    class ChangePasswordMethod {

        private ChangePasswordCommand createValidCommand() {
            return new ChangePasswordCommand("currentpass123", "newpassword456");
        }

        @Test
        @DisplayName("正常なパスワード変更が成功する")
        void shouldSuccessfullyChangePassword() {
            // Arrange
            Long accountId = 1L;
            ChangePasswordCommand command = createValidCommand();
            
            // 現在のパスワードでアカウントを作成
            PasswordHash currentPasswordHash = new PasswordHash("currentpass123");
            Account account = new Account(accountId, new Email("test@example.com"), 
                                        new Username("testuser"), currentPasswordHash,
                                        Instant.now(), Instant.now());
            
            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
            when(accountRepository.save(any(Account.class))).thenReturn(account);

            // Act
            changePasswordHandler.changePassword(accountId, command);

            // Assert
            verify(accountRepository).findById(accountId);
            verify(accountRepository).save(argThat(savedAccount -> {
                // 新しいパスワードでログインできることを確認
                return savedAccount.login("newpassword456") && 
                       !savedAccount.login("currentpass123"); // 古いパスワードは使えない
            }));
        }

        @Test
        @DisplayName("存在しないアカウントIDで例外が発生する")
        void shouldThrowExceptionForNonExistentAccount() {
            // Arrange
            Long nonExistentAccountId = 999L;
            ChangePasswordCommand command = createValidCommand();
            
            when(accountRepository.findById(nonExistentAccountId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> changePasswordHandler.changePassword(nonExistentAccountId, command))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("アカウントが見つかりません");

            // アカウント検索は実行されるが、保存は実行されない
            verify(accountRepository).findById(nonExistentAccountId);
            verify(accountRepository, never()).save(any(Account.class));
        }

        @Test
        @DisplayName("間違った現在パスワードで認証例外が発生する")
        void shouldThrowAuthenticationExceptionForIncorrectCurrentPassword() {
            // Arrange
            Long accountId = 1L;
            ChangePasswordCommand command = new ChangePasswordCommand("wrongpassword123", "newpassword456");
            
            // 正しいパスワードとは異なるパスワードでアカウントを作成
            PasswordHash correctPasswordHash = new PasswordHash("correctpass123");
            Account account = new Account(accountId, new Email("test@example.com"), 
                                        new Username("testuser"), correctPasswordHash,
                                        Instant.now(), Instant.now());
            
            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

            // Act & Assert
            assertThatThrownBy(() -> changePasswordHandler.changePassword(accountId, command))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("現在のパスワードが正しくありません");

            // アカウント検索は実行されるが、保存は実行されない
            verify(accountRepository).findById(accountId);
            verify(accountRepository, never()).save(any(Account.class));
        }

        @Test
        @DisplayName("弱い新パスワードで例外が発生する")
        void shouldThrowExceptionForWeakNewPassword() {
            // Arrange
            Long accountId = 1L;
            ChangePasswordCommand command = new ChangePasswordCommand("currentpass123", "weak"); // 弱いパスワード
            
            PasswordHash currentPasswordHash = new PasswordHash("currentpass123");
            Account account = new Account(accountId, new Email("test@example.com"), 
                                        new Username("testuser"), currentPasswordHash,
                                        Instant.now(), Instant.now());
            
            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

            // Act & Assert
            assertThatThrownBy(() -> changePasswordHandler.changePassword(accountId, command))
                .isInstanceOf(WeakPasswordException.class);

            // アカウント検索は実行されるが、保存は実行されない
            verify(accountRepository).findById(accountId);
            verify(accountRepository, never()).save(any(Account.class));
        }

        @Test
        @DisplayName("nullアカウントIDで例外が発生する")
        void shouldThrowExceptionForNullAccountId() {
            // Arrange
            ChangePasswordCommand command = createValidCommand();

            // Act & Assert
            assertThatThrownBy(() -> changePasswordHandler.changePassword(null, command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("アカウントIDがnullです");

            // リポジトリは呼ばれない
            verify(accountRepository, never()).findById(any());
            verify(accountRepository, never()).save(any(Account.class));
        }

        @Test
        @DisplayName("パスワード変更後にupdatedAtが更新される")
        void shouldUpdateTimestampAfterPasswordChange() {
            // Arrange
            Long accountId = 1L;
            ChangePasswordCommand command = createValidCommand();
            
            Instant originalUpdatedAt = Instant.now().minusSeconds(3600);
            PasswordHash currentPasswordHash = new PasswordHash("currentpass123");
            Account account = new Account(accountId, new Email("test@example.com"), 
                                        new Username("testuser"), currentPasswordHash,
                                        Instant.now().minusSeconds(7200), originalUpdatedAt);
            
            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
            when(accountRepository.save(any(Account.class))).thenReturn(account);

            // Act
            changePasswordHandler.changePassword(accountId, command);

            // Assert
            verify(accountRepository).save(argThat(savedAccount -> 
                savedAccount.getUpdatedAt().isAfter(originalUpdatedAt)));
        }

        @Test
        @DisplayName("同じパスワードでの変更も許可される")
        void shouldAllowChangingToSamePassword() {
            // Arrange
            Long accountId = 1L;
            String samePassword = "samepass123";
            ChangePasswordCommand command = new ChangePasswordCommand(samePassword, samePassword);
            
            PasswordHash passwordHash = new PasswordHash(samePassword);
            Account account = new Account(accountId, new Email("test@example.com"), 
                                        new Username("testuser"), passwordHash,
                                        Instant.now(), Instant.now());
            
            when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));
            when(accountRepository.save(any(Account.class))).thenReturn(account);

            // Act & Assert - 例外が発生しないことを確認
            assertThatCode(() -> changePasswordHandler.changePassword(accountId, command))
                .doesNotThrowAnyException();

            verify(accountRepository).findById(accountId);
            verify(accountRepository).save(any(Account.class));
        }
    }

    @Nested
    @DisplayName("コンストラクタ")
    class Constructor {

        @Test
        @DisplayName("正常にインスタンスが作成される")
        void shouldCreateInstanceSuccessfully() {
            // Arrange & Act
            ChangePasswordHnadler handler = new ChangePasswordHnadler(accountRepository);

            // Assert
            assertThat(handler).isNotNull();
        }
    }
}
