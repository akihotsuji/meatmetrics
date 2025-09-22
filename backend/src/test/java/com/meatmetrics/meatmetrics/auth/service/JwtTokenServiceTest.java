package com.meatmetrics.meatmetrics.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.meatmetrics.meatmetrics.config.JwtProperties;
import com.meatmetrics.meatmetrics.domain.user.aggregate.User;
import com.meatmetrics.meatmetrics.domain.user.valueobject.Email;
import com.meatmetrics.meatmetrics.domain.user.valueobject.Username;
import com.meatmetrics.meatmetrics.domain.user.valueobject.PasswordHash;

/**
 * JwtTokenServiceのユニットテスト
 * 
 * <p>JWT認証機能の中核となるサービスのテストクラスです。
 * AAAパターン（Arrange-Act-Assert）を採用し、ビジネスロジックの正確性を検証します。</p>
 * 
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
@DisplayName("JWT Token Service")
class JwtTokenServiceTest {

    private JwtTokenService jwtTokenService;
    private JwtProperties jwtProperties;
    private User testUser;

    @BeforeEach
    void setUp() {
        // Arrange: テスト用のJWT設定
        jwtProperties = new JwtProperties();
        jwtProperties.setSecretKey("test-secret-key-for-jwt-authentication-at-least-256-bits-long");
        jwtProperties.getAccessToken().setExpirationMs(3600 * 1000); // 1時間
        jwtProperties.getRefreshToken().setExpirationMs(7 * 24 * 3600 * 1000); // 7日

        jwtTokenService = new JwtTokenService(jwtProperties);

        // Arrange: テスト用ユーザー
        testUser = new User(
            1L,
            new Email("test@example.com"),
            new Username("testuser"),
            new PasswordHash("encodedPassword"),
            new java.util.ArrayList<>(),
            java.time.Instant.now(),
            java.time.Instant.now()
        );
    }

    @Nested
    @DisplayName("アクセストークン生成")
    class GenerateAccessTokenTests {

        @Test
        @DisplayName("正常なユーザーでアクセストークンが生成できる")
        void shouldGenerateAccessTokenForValidUser() {
            // Act: アクセストークン生成
            String token = jwtTokenService.generateAccessToken(testUser);

            // Assert: トークンが生成されている
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT形式（header.payload.signature）
        }

        @Test
        @DisplayName("生成したトークンは有効である")
        void shouldGenerateValidAccessToken() {
            // Act: アクセストークン生成
            String token = jwtTokenService.generateAccessToken(testUser);

            // Assert: 生成されたトークンは有効
            assertThat(jwtTokenService.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("生成したトークンからユーザーIDが抽出できる")
        void shouldExtractUserIdFromGeneratedToken() {
            // Act: アクセストークン生成とユーザーID抽出
            String token = jwtTokenService.generateAccessToken(testUser);
            Long extractedUserId = jwtTokenService.extractUserId(token);

            // Assert: 正しいユーザーIDが抽出される
            assertThat(extractedUserId).isEqualTo(testUser.getId());
        }
    }

    @Nested
    @DisplayName("リフレッシュトークン生成")
    class GenerateRefreshTokenTests {

        @Test
        @DisplayName("正常なユーザーでリフレッシュトークンが生成できる")
        void shouldGenerateRefreshTokenForValidUser() {
            // Act: リフレッシュトークン生成
            String token = jwtTokenService.generateRefreshToken(testUser);

            // Assert: トークンが生成されている
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT形式
        }

        @Test
        @DisplayName("生成したリフレッシュトークンは有効である")
        void shouldGenerateValidRefreshToken() {
            // Act: リフレッシュトークン生成
            String token = jwtTokenService.generateRefreshToken(testUser);

            // Assert: 生成されたトークンは有効
            assertThat(jwtTokenService.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("アクセストークンとリフレッシュトークンは異なる")
        void shouldGenerateDifferentTokens() {
            // Act: 両方のトークンを生成
            String accessToken = jwtTokenService.generateAccessToken(testUser);
            String refreshToken = jwtTokenService.generateRefreshToken(testUser);

            // Assert: 異なるトークンが生成される
            assertThat(accessToken).isNotEqualTo(refreshToken);
        }
    }

    @Nested
    @DisplayName("トークン検証")
    class ValidateTokenTests {

        @Test
        @DisplayName("正常なトークンは有効と判定される")
        void shouldValidateValidToken() {
            // Arrange: 有効なトークン
            String token = jwtTokenService.generateAccessToken(testUser);

            // Act & Assert: 有効と判定される
            assertThat(jwtTokenService.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("nullトークンは無効と判定される")
        void shouldRejectNullToken() {
            // Act & Assert: nullは無効
            assertThat(jwtTokenService.validateToken(null)).isFalse();
        }

        @Test
        @DisplayName("空文字列トークンは無効と判定される")
        void shouldRejectEmptyToken() {
            // Act & Assert: 空文字列は無効
            assertThat(jwtTokenService.validateToken("")).isFalse();
            assertThat(jwtTokenService.validateToken("   ")).isFalse();
        }

        @Test
        @DisplayName("不正な形式のトークンは無効と判定される")
        void shouldRejectInvalidFormatToken() {
            // Act & Assert: 不正な形式は無効
            assertThat(jwtTokenService.validateToken("invalid.token")).isFalse();
            assertThat(jwtTokenService.validateToken("not-a-jwt-token")).isFalse();
        }

        @Test
        @DisplayName("改ざんされたトークンは無効と判定される")
        void shouldRejectTamperedToken() {
            // Arrange: 有効なトークンを生成後、改ざん
            String validToken = jwtTokenService.generateAccessToken(testUser);
            String tamperedToken = validToken.substring(0, validToken.length() - 5) + "TAMPERED";

            // Act & Assert: 改ざんされたトークンは無効
            assertThat(jwtTokenService.validateToken(tamperedToken)).isFalse();
        }
    }

    @Nested
    @DisplayName("ユーザーID抽出")
    class ExtractUserIdTests {

        @Test
        @DisplayName("有効なトークンからユーザーIDを抽出できる")
        void shouldExtractUserIdFromValidToken() {
            // Arrange: 有効なトークン
            String token = jwtTokenService.generateAccessToken(testUser);

            // Act: ユーザーID抽出
            Long extractedUserId = jwtTokenService.extractUserId(token);

            // Assert: 正しいユーザーIDが抽出される
            assertThat(extractedUserId).isEqualTo(testUser.getId());
        }

        @Test
        @DisplayName("無効なトークンからのユーザーID抽出は例外を投げる")
        void shouldThrowExceptionForInvalidToken() {
            // Act & Assert: 無効なトークンは例外
            assertThatThrownBy(() -> jwtTokenService.extractUserId("invalid-token"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid token");
        }

        @Test
        @DisplayName("nullトークンからのユーザーID抽出は例外を投げる")
        void shouldThrowExceptionForNullToken() {
            // Act & Assert: nullトークンは例外
            assertThatThrownBy(() -> jwtTokenService.extractUserId(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Invalid token");
        }
    }

    @Nested
    @DisplayName("設定値取得")
    class ConfigurationTests {

        @Test
        @DisplayName("アクセストークンの有効期限が正しく取得できる")
        void shouldReturnCorrectAccessTokenExpiration() {
            // Act: 有効期限取得
            Long expirationSeconds = jwtTokenService.getAccessTokenExpirationSeconds();

            // Assert: 設定値と一致
            assertThat(expirationSeconds).isEqualTo(3600L); // 1時間
        }
    }
}
