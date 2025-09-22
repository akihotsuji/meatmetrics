package com.meatmetrics.meatmetrics.auth.service;

import com.meatmetrics.meatmetrics.PostgreSQLTestBase;
import com.meatmetrics.meatmetrics.auth.command.LoginCommand;
import com.meatmetrics.meatmetrics.auth.dto.LoginResult;
import com.meatmetrics.meatmetrics.auth.exception.AuthenticationException;
import com.meatmetrics.meatmetrics.domain.user.aggregate.User;
import com.meatmetrics.meatmetrics.domain.user.repository.UserRepository;
import com.meatmetrics.meatmetrics.domain.user.valueobject.Email;
import com.meatmetrics.meatmetrics.domain.user.valueobject.PasswordHash;
import com.meatmetrics.meatmetrics.domain.user.valueobject.Username;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

/**
 * LoginServiceの単体テスト
 * 
 * <p>ユーザーログインサービスの動作を検証します。
 * PostgreSQL + Testcontainers環境でテストを実行し、実際のDBトランザクションをテストします。</p>
 * 
 * <h3>テストケース:</h3>
 * <ul>
 *   <li>正常ログインケース</li>
 *   <li>ユーザー未存在エラーケース</li>
 *   <li>パスワード不一致エラーケース</li>
 *   <li>JWT生成確認</li>
 * </ul>
 * 
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("LoginService の単体テスト")
class LoginServiceTest extends PostgreSQLTestBase {

    @Autowired
    private LoginService loginService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtTokenService jwtTokenService;
    
    private User testUser;
    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_USERNAME = "testuser";
    private final String TEST_PASSWORD = "TestPass123";
    
    @BeforeEach
    void setUp() {
        // テスト用ユーザーの準備
        testUser = new User(
            new Email(TEST_EMAIL),
            new Username(TEST_USERNAME),
            new PasswordHash(TEST_PASSWORD)
        );
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("正常ログインケース")
    void login_ValidCredentials_ShouldReturnLoginResult() {
        // Arrange
        LoginCommand command = new LoginCommand(TEST_EMAIL, TEST_PASSWORD);

        // Act
        LoginResult result = loginService.login(command);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAccessToken()).isNotNull();
        assertThat(result.getAccessToken()).isNotEmpty();
        assertThat(result.getRefreshToken()).isNotNull();
        assertThat(result.getRefreshToken()).isNotEmpty();
        assertThat(result.getTokenType()).isEqualTo("Bearer");
        assertThat(result.getExpiresIn()).isNotNull();
        assertThat(result.getExpiresIn()).isGreaterThan(0L);
    }

    @Test
    @DisplayName("ユーザー未存在エラーケース")
    void login_UserNotFound_ShouldThrowAuthenticationException() {
        // Arrange
        LoginCommand command = new LoginCommand("nonexistent@example.com", TEST_PASSWORD);

        // Act & Assert
        assertThatThrownBy(() -> {
            loginService.login(command);
        })
        .isInstanceOf(AuthenticationException.class)
        .hasMessage("メールアドレスまたはパスワードが不正です");
    }

    @Test
    @DisplayName("パスワード不一致エラーケース")
    void login_WrongPassword_ShouldThrowAuthenticationException() {
        // Arrange
        LoginCommand command = new LoginCommand(TEST_EMAIL, "WrongPassword123");

        // Act & Assert
        assertThatThrownBy(() -> {
            loginService.login(command);
        })
        .isInstanceOf(AuthenticationException.class)
        .hasMessage("メールアドレスまたはパスワードが不正です");
    }

    @Test
    @DisplayName("JWT生成確認")
    void login_ValidCredentials_ShouldGenerateValidJwt() {
        // Arrange
        LoginCommand command = new LoginCommand(TEST_EMAIL, TEST_PASSWORD);

        // Act
        LoginResult result = loginService.login(command);

        // Assert
        // アクセストークンの検証
        assertThat(jwtTokenService.validateToken(result.getAccessToken())).isTrue();
        Long extractedUserId = jwtTokenService.extractUserId(result.getAccessToken());
        assertThat(extractedUserId).isEqualTo(testUser.getId());
        
        // リフレッシュトークンの検証
        assertThat(jwtTokenService.validateToken(result.getRefreshToken())).isTrue();
        Long refreshTokenUserId = jwtTokenService.extractUserId(result.getRefreshToken());
        assertThat(refreshTokenUserId).isEqualTo(testUser.getId());
        
        // JWT形式の確認
        assertThat(result.getAccessToken().split("\\.")).hasSize(3);
        assertThat(result.getRefreshToken().split("\\.")).hasSize(3);
    }

    @Test
    @DisplayName("ログイン後にユーザー情報の整合性確認")
    void login_ValidCredentials_ShouldMaintainUserDataIntegrity() {
        // Arrange
        LoginCommand command = new LoginCommand(TEST_EMAIL, TEST_PASSWORD);

        // Act
        LoginResult result = loginService.login(command);

        // Assert
        // DBのユーザー情報が変更されていないことを確認
        User userAfterLogin = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(userAfterLogin.getEmail().getValue()).isEqualTo(TEST_EMAIL);
        assertThat(userAfterLogin.getUsername().getValue()).isEqualTo(TEST_USERNAME);
        assertThat(userAfterLogin.getPasswordHash().matches(TEST_PASSWORD)).isTrue();
        
        // JWTから取得したユーザーIDがDBのユーザーと一致
        Long jwtUserId = jwtTokenService.extractUserId(result.getAccessToken());
        assertThat(jwtUserId).isEqualTo(userAfterLogin.getId());
    }

    @Test
    @DisplayName("トークン再発行機能のテスト")
    void refreshToken_ValidRefreshToken_ShouldReturnNewAccessToken() {
        // Arrange
        LoginCommand command = new LoginCommand(TEST_EMAIL, TEST_PASSWORD);
        LoginResult initialLogin = loginService.login(command);
        String refreshToken = initialLogin.getRefreshToken();

        // Act
        LoginResult refreshResult = loginService.refreshToken(refreshToken);

        // Assert
        assertThat(refreshResult).isNotNull();
        assertThat(refreshResult.getAccessToken()).isNotNull();
        assertThat(refreshResult.getAccessToken()).isNotEmpty();
        assertThat(refreshResult.getRefreshToken()).isEqualTo(refreshToken); // リフレッシュトークンは同じ
        
        // 新しいアクセストークンが有効
        assertThat(jwtTokenService.validateToken(refreshResult.getAccessToken())).isTrue();
        Long newTokenUserId = jwtTokenService.extractUserId(refreshResult.getAccessToken());
        assertThat(newTokenUserId).isEqualTo(testUser.getId());
        
        // 新旧のアクセストークンは異なる（時刻が違うため）
        assertThat(refreshResult.getAccessToken()).isNotEqualTo(initialLogin.getAccessToken());
    }

    @Test
    @DisplayName("無効なリフレッシュトークンで認証エラー")
    void refreshToken_InvalidRefreshToken_ShouldThrowAuthenticationException() {
        // Arrange
        String invalidRefreshToken = "invalid.refresh.token";

        // Act & Assert
        assertThatThrownBy(() -> {
            loginService.refreshToken(invalidRefreshToken);
        })
        .isInstanceOf(AuthenticationException.class)
        .hasMessage("認証に失敗しました");
    }

    @Test
    @DisplayName("空のパスワードで認証エラー")
    void login_EmptyPassword_ShouldThrowAuthenticationException() {
        // Arrange
        LoginCommand command = new LoginCommand(TEST_EMAIL, "");

        // Act & Assert
        assertThatThrownBy(() -> {
            loginService.login(command);
        })
        .isInstanceOf(AuthenticationException.class)
        .hasMessage("メールアドレスまたはパスワードが不正です");
    }
}
