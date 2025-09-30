package com.meatmetrics.meatmetrics.auth.application.handler;

import com.meatmetrics.meatmetrics.api.auth.dto.response.LoginResponse;
import com.meatmetrics.meatmetrics.auth.application.command.LoginCommand;
import com.meatmetrics.meatmetrics.auth.domain.account.Account;
import com.meatmetrics.meatmetrics.auth.domain.account.PasswordHash;
import com.meatmetrics.meatmetrics.auth.domain.exception.AuthenticationException;
import com.meatmetrics.meatmetrics.auth.domain.repository.AccountRepository;
import com.meatmetrics.meatmetrics.auth.infrastructure.security.JwtTokenService;
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
 * LoginHandlerのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("LoginHandler")
class LoginHandlerTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private JwtTokenService jwtTokenService;

    private LoginHandler loginHandler;

    @BeforeEach
    void setUp() {
        loginHandler = new LoginHandler(accountRepository, jwtTokenService);
    }

    @Nested
    @DisplayName("login メソッド")
    class LoginMethod {

        private LoginCommand createValidCommand() {
            return new LoginCommand("test@example.com", "password123");
        }

        @Test
        @DisplayName("正常なログインが成功する")
        void shouldSuccessfullyLogin() {
            // Arrange
            LoginCommand command = createValidCommand();
            String plainPassword = "password123";
            
            // アカウントが存在し、パスワードが一致する
            PasswordHash passwordHash = new PasswordHash(plainPassword);
            Account account = new Account(1L, new Email("test@example.com"), 
                                        new Username("testuser"), passwordHash,
                                        Instant.now(), Instant.now());
            when(accountRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(account));
            
            // JWTトークンを生成
            when(jwtTokenService.generateAccessToken(account)).thenReturn("access.token.here");
            when(jwtTokenService.generateRefreshToken(account)).thenReturn("refresh.token.here");
            when(jwtTokenService.getAccessTokenExpirationSeconds()).thenReturn(3600L);

            // Act
            LoginResponse response = loginHandler.login(command);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("access.token.here");
            assertThat(response.getRefreshToken()).isEqualTo("refresh.token.here");
            assertThat(response.getExpiresIn()).isEqualTo(3600L);
            assertThat(response.getTokenType()).isEqualTo("Bearer");

            // サービスが適切に呼ばれることを確認
            verify(accountRepository).findByEmail(any(Email.class));
            verify(jwtTokenService).generateAccessToken(account);
            verify(jwtTokenService).generateRefreshToken(account);
            verify(jwtTokenService).getAccessTokenExpirationSeconds();
        }

        @Test
        @DisplayName("存在しないメールアドレスで認証失敗")
        void shouldThrowExceptionForNonExistentEmail() {
            // Arrange
            LoginCommand command = createValidCommand();
            when(accountRepository.findByEmail(any(Email.class))).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> loginHandler.login(command))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("メールアドレスまたはパスワードが不正です");

            // アカウント検索までは実行され、トークン生成は実行されない
            verify(accountRepository).findByEmail(any(Email.class));
            verify(jwtTokenService, never()).generateAccessToken(any());
            verify(jwtTokenService, never()).generateRefreshToken(any());
        }

        @Test
        @DisplayName("間違ったパスワードで認証失敗")
        void shouldThrowExceptionForIncorrectPassword() {
            // Arrange
            LoginCommand command = createValidCommand();
            
            // 正しいパスワードとは異なるパスワードでアカウントを作成
            PasswordHash passwordHash = new PasswordHash("correctpass123");
            Account account = new Account(1L, new Email("test@example.com"), 
                                        new Username("testuser"), passwordHash,
                                        Instant.now(), Instant.now());
            when(accountRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(account));

            // Act & Assert
            assertThatThrownBy(() -> loginHandler.login(command))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("メールアドレスまたはパスワードが不正です");

            // アカウント検索は実行され、トークン生成は実行されない
            verify(accountRepository).findByEmail(any(Email.class));
            verify(jwtTokenService, never()).generateAccessToken(any());
            verify(jwtTokenService, never()).generateRefreshToken(any());
        }

        @Test
        @DisplayName("正規化されたEmailでリポジトリが呼ばれる")
        void shouldCallRepositoryWithNormalizedEmail() {
            // Arrange
            LoginCommand command = new LoginCommand("  TEST@EXAMPLE.COM  ", "password123"); // 前後空白、大文字

            PasswordHash passwordHash = new PasswordHash("password123");
            Account account = new Account(1L, new Email("test@example.com"), 
                                        new Username("testuser"), passwordHash,
                                        Instant.now(), Instant.now());
            when(accountRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(account));
            when(jwtTokenService.generateAccessToken(account)).thenReturn("access.token");
            when(jwtTokenService.generateRefreshToken(account)).thenReturn("refresh.token");
            when(jwtTokenService.getAccessTokenExpirationSeconds()).thenReturn(3600L);

            // Act
            loginHandler.login(command);

            // Assert - 正規化された値でリポジトリが呼ばれることを確認
            verify(accountRepository).findByEmail(argThat(email -> 
                email.getValue().equals("test@example.com")));
        }

        @Test
        @DisplayName("JWTトークン生成でアカウント情報が正しく使われる")
        void shouldUseCorrectAccountForTokenGeneration() {
            // Arrange
            LoginCommand command = createValidCommand();
            
            PasswordHash passwordHash = new PasswordHash("password123");
            Account account = new Account(1L, new Email("test@example.com"), 
                                        new Username("testuser"), passwordHash,
                                        Instant.now(), Instant.now());
            when(accountRepository.findByEmail(any(Email.class))).thenReturn(Optional.of(account));
            when(jwtTokenService.generateAccessToken(account)).thenReturn("access.token");
            when(jwtTokenService.generateRefreshToken(account)).thenReturn("refresh.token");
            when(jwtTokenService.getAccessTokenExpirationSeconds()).thenReturn(3600L);

            // Act
            loginHandler.login(command);

            // Assert - 同じアカウントインスタンスがトークン生成に使われることを確認
            verify(jwtTokenService).generateAccessToken(account);
            verify(jwtTokenService).generateRefreshToken(account);
        }
    }

    @Nested
    @DisplayName("コンストラクタ")
    class Constructor {

        @Test
        @DisplayName("正常にインスタンスが作成される")
        void shouldCreateInstanceSuccessfully() {
            // Arrange & Act
            LoginHandler handler = new LoginHandler(accountRepository, jwtTokenService);

            // Assert
            assertThat(handler).isNotNull();
        }
    }
}
