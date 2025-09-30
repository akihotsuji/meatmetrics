package com.meatmetrics.meatmetrics.auth.application.handler;

import com.meatmetrics.meatmetrics.api.auth.dto.response.RegisterResponse;
import com.meatmetrics.meatmetrics.auth.application.command.RegisterAccountCommand;
import com.meatmetrics.meatmetrics.auth.domain.account.Account;
import com.meatmetrics.meatmetrics.auth.domain.account.PasswordHash;
import com.meatmetrics.meatmetrics.auth.domain.exception.DuplicateEmailException;
import com.meatmetrics.meatmetrics.auth.domain.exception.DuplicateUsernameException;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * RegisterAccountHandlerのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegisterAccountHandler")
class RegisterAccountHandlerTest {

    @Mock
    private AccountRepository accountRepository;

    private RegisterAccountHandler registerAccountHandler;

    @BeforeEach
    void setUp() {
        registerAccountHandler = new RegisterAccountHandler(accountRepository);
    }

    @Nested
    @DisplayName("register メソッド")
    class RegisterMethod {

        private RegisterAccountCommand createValidCommand() {
            RegisterAccountCommand command = new RegisterAccountCommand();
            command.setEmail("test@example.com");
            command.setUsername("testuser");
            command.setPassword("password123");
            return command;
        }

        @Test
        @DisplayName("正常なアカウント登録が成功する")
        void shouldSuccessfullyRegisterAccount() {
            // Arrange
            RegisterAccountCommand command = createValidCommand();
            
            // メールアドレスとユーザー名が重複していない
            when(accountRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());
            when(accountRepository.findByUsername(any(Username.class))).thenReturn(Optional.empty());
            
            // 保存時に ID を付与したアカウントを返す
            Account savedAccount = new Account(1L, new Email("test@example.com"), 
                                             new Username("testuser"), 
                                             new PasswordHash("password123"), Instant.now(), Instant.now());
            when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

            // Act
            RegisterResponse response = registerAccountHandler.register(command);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getUserId()).isEqualTo(1L);
            assertThat(response.getEmail()).isEqualTo("test@example.com");
            assertThat(response.getUsername()).isEqualTo("testuser");
            assertThat(response.getCreatedAt()).isNotNull();

            // 重複チェックが実行されたことを確認
            verify(accountRepository).findByEmail(any(Email.class));
            verify(accountRepository).findByUsername(any(Username.class));
            verify(accountRepository).save(any(Account.class));
        }

        @Test
        @DisplayName("メールアドレス重複で例外が発生する")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            // Arrange
            RegisterAccountCommand command = createValidCommand();
            
            // 既存のアカウントが存在する
            Account existingAccount = new Account(new Email("test@example.com"), 
                                                new Username("existing"), 
                                                new PasswordHash("password123"));
            when(accountRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(existingAccount));

            // Act & Assert
            assertThatThrownBy(() -> registerAccountHandler.register(command))
                .isInstanceOf(DuplicateEmailException.class);

            // メールアドレスチェック後に処理が停止することを確認
            verify(accountRepository).findByEmail(any(Email.class));
            verify(accountRepository, never()).findByUsername(any(Username.class));
            verify(accountRepository, never()).save(any(Account.class));
        }

        @Test
        @DisplayName("ユーザー名重複で例外が発生する")
        void shouldThrowExceptionWhenUsernameAlreadyExists() {
            // Arrange
            RegisterAccountCommand command = createValidCommand();
            
            // メールアドレスは重複していないが、ユーザー名は重複している
            when(accountRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());
            
            Account existingAccount = new Account(new Email("different@example.com"), 
                                                new Username("testuser"), 
                                                new PasswordHash("password123"));
            when(accountRepository.findByUsername(any(Username.class))).thenReturn(Optional.of(existingAccount));

            // Act & Assert
            assertThatThrownBy(() -> registerAccountHandler.register(command))
                .isInstanceOf(DuplicateUsernameException.class);

            // ユーザー名チェックまで実行されることを確認
            verify(accountRepository).findByEmail(any(Email.class));
            verify(accountRepository).findByUsername(any(Username.class));
            verify(accountRepository, never()).save(any(Account.class));
        }

        @Test
        @DisplayName("弱いパスワードで例外が発生する")
        void shouldThrowExceptionForWeakPassword() {
            // Arrange
            RegisterAccountCommand command = new RegisterAccountCommand();
            command.setEmail("test@example.com");
            command.setUsername("testuser");
            command.setPassword("weak"); // 弱いパスワード

            when(accountRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());
            when(accountRepository.findByUsername(any(Username.class))).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> registerAccountHandler.register(command))
                .isInstanceOf(WeakPasswordException.class);

            // 重複チェックは実行されるが、PasswordHash作成時点で例外が発生するためsaveは実行されない
            verify(accountRepository).findByEmail(any(Email.class));
            verify(accountRepository).findByUsername(any(Username.class));
            verify(accountRepository, never()).save(any(Account.class));
        }

        @Test
        @DisplayName("正規化されたEmailとUsernameでリポジトリが呼ばれる")
        void shouldCallRepositoryWithNormalizedValues() {
            // Arrange
            RegisterAccountCommand command = new RegisterAccountCommand();
            command.setEmail("  TEST@EXAMPLE.COM  "); // 前後空白、大文字
            command.setUsername("  TestUser  "); // 前後空白
            command.setPassword("password123");

            when(accountRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());
            when(accountRepository.findByUsername(any(Username.class))).thenReturn(Optional.empty());
            
            Account savedAccount = new Account(1L, new Email("test@example.com"), 
                                             new Username("TestUser"), 
                                             new PasswordHash("password123"), Instant.now(), Instant.now());
            when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

            // Act
            registerAccountHandler.register(command);

            // Assert - 正規化された値でリポジトリが呼ばれることを確認
            verify(accountRepository).findByEmail(argThat(email -> 
                email.getValue().equals("test@example.com")));
            verify(accountRepository).findByUsername(argThat(username -> 
                username.getValue().equals("TestUser")));
        }
    }

    @Nested
    @DisplayName("コンストラクタ")
    class Constructor {

        @Test
        @DisplayName("正常にインスタンスが作成される")
        void shouldCreateInstanceSuccessfully() {
            // Arrange & Act
            RegisterAccountHandler handler = new RegisterAccountHandler(accountRepository);

            // Assert
            assertThat(handler).isNotNull();
        }
    }
}
