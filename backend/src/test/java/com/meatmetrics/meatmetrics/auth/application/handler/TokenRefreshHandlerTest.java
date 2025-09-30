package com.meatmetrics.meatmetrics.auth.application.handler;

import com.meatmetrics.meatmetrics.api.auth.dto.response.RefreshResponse;
import com.meatmetrics.meatmetrics.auth.application.command.RefreshCommand;
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
 * TokenRefreshHandlerのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TokenRefreshHandler")
class TokenRefreshHandlerTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private JwtTokenService jwtTokenService;

    private TokenRefreshHandler tokenRefreshHandler;

    @BeforeEach
    void setUp() {
        tokenRefreshHandler = new TokenRefreshHandler(accountRepository, jwtTokenService);
    }

    @Nested
    @DisplayName("refresh メソッド")
    class RefreshMethod {

        private RefreshCommand createValidCommand() {
            return new RefreshCommand("valid.refresh.token");
        }

        private Account createTestAccount() {
            return new Account(1L, new Email("test@example.com"), 
                             new Username("testuser"), 
                             new PasswordHash("password123"),
                             Instant.now(), Instant.now());
        }

        @Test
        @DisplayName("有効なリフレッシュトークンで新しいトークンが発行される")
        void shouldIssueNewTokensWithValidRefreshToken() {
            // Arrange
            RefreshCommand command = createValidCommand();
            Account account = createTestAccount();
            
            // リフレッシュトークンが有効でユーザーIDを取得できる
            when(jwtTokenService.validateToken("valid.refresh.token")).thenReturn(true);
            when(jwtTokenService.extractUserId("valid.refresh.token")).thenReturn(1L);
            when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
            
            // 新しいトークンを生成
            when(jwtTokenService.generateAccessToken(account)).thenReturn("new.access.token");
            when(jwtTokenService.generateRefreshToken(account)).thenReturn("new.refresh.token");
            when(jwtTokenService.getAccessTokenExpirationSeconds()).thenReturn(3600L);

            // Act
            RefreshResponse response = tokenRefreshHandler.refresh(command);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.getAccessToken()).isEqualTo("new.access.token");
            assertThat(response.getRefreshToken()).isEqualTo("new.refresh.token");
            assertThat(response.getExpiresIn()).isEqualTo(3600L);
            assertThat(response.getTokenType()).isEqualTo("Bearer");

            // サービスが適切に呼ばれることを確認
            verify(jwtTokenService).validateToken("valid.refresh.token");
            verify(jwtTokenService).extractUserId("valid.refresh.token");
            verify(accountRepository).findById(1L);
            verify(jwtTokenService).generateAccessToken(account);
            verify(jwtTokenService).generateRefreshToken(account);
            verify(jwtTokenService).getAccessTokenExpirationSeconds();
        }

        @Test
        @DisplayName("無効なリフレッシュトークンで認証例外が発生する")
        void shouldThrowAuthenticationExceptionForInvalidToken() {
            // Arrange
            RefreshCommand command = createValidCommand();
            
            // リフレッシュトークンが無効
            when(jwtTokenService.validateToken("valid.refresh.token")).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> tokenRefreshHandler.refresh(command))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("無効なトークンです");

            // トークン検証までは実行され、それ以降は実行されない
            verify(jwtTokenService).validateToken("valid.refresh.token");
            verify(jwtTokenService, never()).extractUserId(anyString());
            verify(accountRepository, never()).findById(any());
            verify(jwtTokenService, never()).generateAccessToken(any());
            verify(jwtTokenService, never()).generateRefreshToken(any());
        }

        @Test
        @DisplayName("存在しないユーザーIDで認証例外が発生する")
        void shouldThrowAuthenticationExceptionForNonExistentUser() {
            // Arrange
            RefreshCommand command = createValidCommand();
            
            // トークンは有効だがユーザーが存在しない
            when(jwtTokenService.validateToken("valid.refresh.token")).thenReturn(true);
            when(jwtTokenService.extractUserId("valid.refresh.token")).thenReturn(999L);
            when(accountRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> tokenRefreshHandler.refresh(command))
                .isInstanceOf(AuthenticationException.class)
                .hasMessageContaining("無効なトークンです");

            // ユーザー検索まで実行され、トークン生成は実行されない
            verify(jwtTokenService).validateToken("valid.refresh.token");
            verify(jwtTokenService).extractUserId("valid.refresh.token");
            verify(accountRepository).findById(999L);
            verify(jwtTokenService, never()).generateAccessToken(any());
            verify(jwtTokenService, never()).generateRefreshToken(any());
        }

        @Test
        @DisplayName("トークン検証でランタイム例外が発生した場合に適切に処理される")
        void shouldHandleRuntimeExceptionDuringTokenValidation() {
            // Arrange
            RefreshCommand command = createValidCommand();
            
            // トークン検証でランタイム例外が発生
            when(jwtTokenService.validateToken("valid.refresh.token"))
                .thenThrow(new RuntimeException("Token parsing failed"));

            // Act & Assert
            assertThatThrownBy(() -> tokenRefreshHandler.refresh(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Token parsing failed");

            // 例外発生後は他のメソッドは呼ばれない
            verify(jwtTokenService).validateToken("valid.refresh.token");
            verify(jwtTokenService, never()).extractUserId(anyString());
            verify(accountRepository, never()).findById(any());
        }

        @Test
        @DisplayName("ユーザーID抽出でランタイム例外が発生した場合に適切に処理される")
        void shouldHandleRuntimeExceptionDuringUserIdExtraction() {
            // Arrange
            RefreshCommand command = createValidCommand();
            
            when(jwtTokenService.validateToken("valid.refresh.token")).thenReturn(true);
            when(jwtTokenService.extractUserId("valid.refresh.token"))
                .thenThrow(new RuntimeException("Cannot extract user ID"));

            // Act & Assert
            assertThatThrownBy(() -> tokenRefreshHandler.refresh(command))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Cannot extract user ID");

            verify(jwtTokenService).validateToken("valid.refresh.token");
            verify(jwtTokenService).extractUserId("valid.refresh.token");
            verify(accountRepository, never()).findById(any());
        }

        @Test
        @DisplayName("新しいトークン生成で同じアカウントが使用される")
        void shouldUseSameAccountForTokenGeneration() {
            // Arrange
            RefreshCommand command = createValidCommand();
            Account account = createTestAccount();
            
            when(jwtTokenService.validateToken("valid.refresh.token")).thenReturn(true);
            when(jwtTokenService.extractUserId("valid.refresh.token")).thenReturn(1L);
            when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
            when(jwtTokenService.generateAccessToken(account)).thenReturn("new.access.token");
            when(jwtTokenService.generateRefreshToken(account)).thenReturn("new.refresh.token");
            when(jwtTokenService.getAccessTokenExpirationSeconds()).thenReturn(3600L);

            // Act
            tokenRefreshHandler.refresh(command);

            // Assert - 同じアカウントインスタンスがトークン生成に使われることを確認
            verify(jwtTokenService).generateAccessToken(account);
            verify(jwtTokenService).generateRefreshToken(account);
        }

        @Test
        @DisplayName("nullリフレッシュトークンで例外が発生する")
        void shouldThrowExceptionForNullRefreshToken() {
            // Arrange
            RefreshCommand command = new RefreshCommand(null);

            // Act & Assert
            // RefreshCommandのgetRefreshToken()でnullが返され、JwtTokenServiceに渡される
            // 実装によってはここで例外が発生する可能性がある
            assertThatThrownBy(() -> tokenRefreshHandler.refresh(command))
                .isInstanceOf(Exception.class); // NullPointerException or AuthenticationException

            verify(jwtTokenService).validateToken(null);
        }
    }

    @Nested
    @DisplayName("コンストラクタ")
    class Constructor {

        @Test
        @DisplayName("正常にインスタンスが作成される")
        void shouldCreateInstanceSuccessfully() {
            // Arrange & Act
            TokenRefreshHandler handler = new TokenRefreshHandler(accountRepository, jwtTokenService);

            // Assert
            assertThat(handler).isNotNull();
        }
    }
}
