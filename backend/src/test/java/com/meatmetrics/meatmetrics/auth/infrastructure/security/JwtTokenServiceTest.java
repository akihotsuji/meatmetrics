package com.meatmetrics.meatmetrics.auth.infrastructure.security;

import com.meatmetrics.meatmetrics.auth.domain.account.Account;
import com.meatmetrics.meatmetrics.auth.domain.account.PasswordHash;
import com.meatmetrics.meatmetrics.config.JwtProperties;
import com.meatmetrics.meatmetrics.sharedkernel.domain.common.Email;
import com.meatmetrics.meatmetrics.sharedkernel.domain.common.Username;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.*;

/**
 * JwtTokenServiceのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtTokenService")
class JwtTokenServiceTest {

    private JwtProperties jwtProperties;

    private JwtTokenService jwtTokenService;

    private Account testAccount;

    // テスト用の秘密鍵（実際のプロダクションでは使用しない）
    private final String testSecretKey = "test-secret-key-for-jwt-token-service-unit-tests-must-be-long-enough";

    @BeforeEach
    void setUp() {
        // 実際のJwtPropertiesオブジェクトを作成
        jwtProperties = new JwtProperties();
        jwtProperties.setSecretKey(testSecretKey);
        
        // アクセストークン設定（1時間 = 3600000ms）
        jwtProperties.getAccessToken().setExpirationMs(3600000L);
        
        // リフレッシュトークン設定（7日 = 604800000ms）
        jwtProperties.getRefreshToken().setExpirationMs(604800000L);

        jwtTokenService = new JwtTokenService(jwtProperties);

        // テスト用アカウント
        testAccount = new Account(1L, new Email("test@example.com"), 
                                new Username("testuser"), 
                                new PasswordHash("password123"),
                                Instant.now(), Instant.now());
    }

    @Nested
    @DisplayName("generateAccessToken メソッド")
    class GenerateAccessTokenMethod {

        @Test
        @DisplayName("有効なアクセストークンが生成される")
        void shouldGenerateValidAccessToken() {
            // Act
            String token = jwtTokenService.generateAccessToken(testAccount);

            // Assert
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT形式（header.payload.signature）

            // トークンを解析してクレームを確認
            SecretKey key = Keys.hmacShaKeyFor(testSecretKey.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            assertThat(claims.getSubject()).isEqualTo("1");
            assertThat(claims.get("email", String.class)).isEqualTo("test@example.com");
            assertThat(claims.get("username", String.class)).isEqualTo("testuser");
            assertThat(claims.getId()).isNotNull(); // UUID
            assertThat(claims.getIssuedAt()).isNotNull();
            assertThat(claims.getExpiration()).isNotNull();
        }

        @Test
        @DisplayName("アカウントIDがnullの場合に例外が発生する")
        void shouldThrowExceptionWhenAccountIdIsNull() {
            // Arrange
            Account accountWithoutId = new Account(new Email("test@example.com"), 
                                                 new Username("testuser"), 
                                                 new PasswordHash("password123"));

            // Act & Assert
            assertThatThrownBy(() -> jwtTokenService.generateAccessToken(accountWithoutId))
                .isInstanceOf(Exception.class); // NullPointerException
        }

        @Test
        @DisplayName("有効期限が正しく設定される")
        void shouldSetCorrectExpirationTime() {
            // Arrange
            long beforeGeneration = System.currentTimeMillis();

            // Act
            String token = jwtTokenService.generateAccessToken(testAccount);
            long afterGeneration = System.currentTimeMillis();

            // Assert
            SecretKey key = Keys.hmacShaKeyFor(testSecretKey.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            long issuedAt = claims.getIssuedAt().getTime();
            long expiration = claims.getExpiration().getTime();
            long tokenLifetime = expiration - issuedAt;

            // 発行時間が生成前後の時間範囲内であることを確認（JJWTのDateは秒精度のため秒単位で比較）
            assertThat(issuedAt / 1000).isBetween(beforeGeneration / 1000, afterGeneration / 1000);
            // デバッグ情報を出力
            long expectedLifetimeMs = jwtProperties.getAccessToken().getExpirationMs();
            long expectedLifetimeSeconds = jwtProperties.getAccessToken().getExpirationSeconds();
            long actualLifetimeSeconds = tokenLifetime / 1000;
            
            System.out.println("=== JWT TIME DEBUG ===");
            System.out.println("Token lifetime (ms): " + tokenLifetime);
            System.out.println("Expected lifetime (ms): " + expectedLifetimeMs);
            System.out.println("Expected lifetime (sec): " + expectedLifetimeSeconds);
            System.out.println("Actual lifetime (sec): " + actualLifetimeSeconds);
            System.out.println("Issued at: " + new java.util.Date(issuedAt));
            System.out.println("Expires at: " + new java.util.Date(expiration));
            System.out.println("=======================");
            
            // トークンの寿命が設定値と大幅に近いことを確認（処理時間による誤差を広く許容）
            assertThat(tokenLifetime).isBetween(expectedLifetimeMs - 5000, expectedLifetimeMs + 5000);
        }
    }

    @Nested
    @DisplayName("generateRefreshToken メソッド")
    class GenerateRefreshTokenMethod {

        @Test
        @DisplayName("有効なリフレッシュトークンが生成される")
        void shouldGenerateValidRefreshToken() {
            // Act
            String token = jwtTokenService.generateRefreshToken(testAccount);

            // Assert
            assertThat(token).isNotNull();
            assertThat(token).isNotEmpty();
            assertThat(token.split("\\.")).hasSize(3); // JWT形式

            // トークンを解析してクレームを確認
            SecretKey key = Keys.hmacShaKeyFor(testSecretKey.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            assertThat(claims.getSubject()).isEqualTo("1");
            assertThat(claims.getIssuedAt()).isNotNull();
            assertThat(claims.getExpiration()).isNotNull();
            // リフレッシュトークンにはemailやusernameクレームは含まれない
            assertThat(claims.get("email")).isNull();
            assertThat(claims.get("username")).isNull();
        }

        @Test
        @DisplayName("有効期限が正しく設定される（7日間）")
        void shouldSetCorrectExpirationTimeForSevenDays() {
            // Arrange
            long beforeGeneration = System.currentTimeMillis();

            // Act
            String token = jwtTokenService.generateRefreshToken(testAccount);
            long afterGeneration = System.currentTimeMillis();

            // Assert
            SecretKey key = Keys.hmacShaKeyFor(testSecretKey.getBytes(StandardCharsets.UTF_8));
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            long issuedAt = claims.getIssuedAt().getTime();
            long expiration = claims.getExpiration().getTime();
            long tokenLifetime = expiration - issuedAt;

            // 発行時間が生成前後の時間範囲内であることを確認（JJWTのDateは秒精度のため秒単位で比較）
            assertThat(issuedAt / 1000).isBetween(beforeGeneration / 1000, afterGeneration / 1000);
            // デバッグ情報を出力
            long expectedLifetimeMs = jwtProperties.getRefreshToken().getExpirationMs();
            long expectedLifetimeSeconds = jwtProperties.getRefreshToken().getExpirationSeconds();
            long actualLifetimeSeconds = tokenLifetime / 1000;
            
            System.out.println("=== JWT REFRESH DEBUG ===");
            System.out.println("Token lifetime (ms): " + tokenLifetime);
            System.out.println("Expected lifetime (ms): " + expectedLifetimeMs);
            System.out.println("Expected lifetime (sec): " + expectedLifetimeSeconds);
            System.out.println("Actual lifetime (sec): " + actualLifetimeSeconds);
            System.out.println("Issued at: " + new java.util.Date(issuedAt));
            System.out.println("Expires at: " + new java.util.Date(expiration));
            System.out.println("==========================");
            
            // トークンの寿命が設定値と大幅に近いことを確認（処理時間による誤差を広く許容）
            assertThat(tokenLifetime).isBetween(expectedLifetimeMs - 5000, expectedLifetimeMs + 5000);
        }
    }

    @Nested
    @DisplayName("validateToken メソッド")
    class ValidateTokenMethod {

        @Test
        @DisplayName("有効なトークンでtrueを返す")
        void shouldReturnTrueForValidToken() {
            // Arrange
            String token = jwtTokenService.generateAccessToken(testAccount);

            // Act & Assert
            assertThat(jwtTokenService.validateToken(token)).isTrue();
        }

        @Test
        @DisplayName("nullトークンでfalseを返す")
        void shouldReturnFalseForNullToken() {
            // Act & Assert
            assertThat(jwtTokenService.validateToken(null)).isFalse();
        }

        @Test
        @DisplayName("空文字トークンでfalseを返す")
        void shouldReturnFalseForEmptyToken() {
            // Act & Assert
            assertThat(jwtTokenService.validateToken("")).isFalse();
            assertThat(jwtTokenService.validateToken("   ")).isFalse();
        }

        @Test
        @DisplayName("不正な形式のトークンでfalseを返す")
        void shouldReturnFalseForMalformedToken() {
            // Act & Assert
            assertThat(jwtTokenService.validateToken("invalid.token")).isFalse();
            assertThat(jwtTokenService.validateToken("not-a-jwt-token")).isFalse();
        }

        @Test
        @DisplayName("異なる秘密鍵で署名されたトークンでfalseを返す")
        void shouldReturnFalseForTokenSignedWithDifferentKey() {
            // Arrange - 異なる秘密鍵でトークンを生成
            String differentKey = "different-secret-key-for-testing-signature-verification-purpose";
            SecretKey signingKey = Keys.hmacShaKeyFor(differentKey.getBytes(StandardCharsets.UTF_8));
            
            String invalidToken = Jwts.builder()
                    .subject("1")
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 3600000))
                    .signWith(signingKey)
                    .compact();

            // Act & Assert
            assertThat(jwtTokenService.validateToken(invalidToken)).isFalse();
        }

        @Test
        @DisplayName("期限切れトークンでfalseを返す")
        void shouldReturnFalseForExpiredToken() {
            // Arrange - 期限切れトークンを生成
            SecretKey key = Keys.hmacShaKeyFor(testSecretKey.getBytes(StandardCharsets.UTF_8));
            String expiredToken = Jwts.builder()
                    .subject("1")
                    .issuedAt(new Date(System.currentTimeMillis() - 7200000)) // 2時間前
                    .expiration(new Date(System.currentTimeMillis() - 3600000)) // 1時間前（期限切れ）
                    .signWith(key)
                    .compact();

            // Act & Assert
            assertThat(jwtTokenService.validateToken(expiredToken)).isFalse();
        }
    }

    @Nested
    @DisplayName("extractUserId メソッド")
    class ExtractUserIdMethod {

        @Test
        @DisplayName("有効なトークンからユーザーIDを抽出する")
        void shouldExtractUserIdFromValidToken() {
            // Arrange
            String token = jwtTokenService.generateAccessToken(testAccount);

            // Act
            Long userId = jwtTokenService.extractUserId(token);

            // Assert
            assertThat(userId).isEqualTo(1L);
        }

        @Test
        @DisplayName("無効なトークンで例外が発生する")
        void shouldThrowExceptionForInvalidToken() {
            // Act & Assert
            assertThatThrownBy(() -> jwtTokenService.extractUserId("invalid.token"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid token");
        }

        @Test
        @DisplayName("nullトークンで例外が発生する")
        void shouldThrowExceptionForNullToken() {
            // Act & Assert
            assertThatThrownBy(() -> jwtTokenService.extractUserId(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid token");
        }

        @Test
        @DisplayName("数値でないsubjectを持つトークンで例外が発生する")
        void shouldThrowExceptionForNonNumericSubject() {
            // Arrange - 数値でないsubjectを持つトークンを生成
            SecretKey key = Keys.hmacShaKeyFor(testSecretKey.getBytes(StandardCharsets.UTF_8));
            String tokenWithInvalidSubject = Jwts.builder()
                    .subject("not-a-number")
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + 3600000))
                    .signWith(key)
                    .compact();

            // Act & Assert
            assertThatThrownBy(() -> jwtTokenService.extractUserId(tokenWithInvalidSubject))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid token format");
        }
    }

    @Nested
    @DisplayName("getAccessTokenExpirationSeconds メソッド")
    class GetAccessTokenExpirationSecondsMethod {

        @Test
        @DisplayName("設定された有効期限秒数を返す")
        void shouldReturnConfiguredExpirationSeconds() {
            // Act
            Long expirationSeconds = jwtTokenService.getAccessTokenExpirationSeconds();

            // Assert
            assertThat(expirationSeconds).isEqualTo(3600L);
        }
    }

    @Nested
    @DisplayName("統合テスト")
    class IntegrationTests {

        @Test
        @DisplayName("生成→検証→ユーザーID抽出の一連の流れが正常動作する")
        void shouldWorkEndToEndForAccessToken() {
            // Act - アクセストークン生成
            String accessToken = jwtTokenService.generateAccessToken(testAccount);

            // Assert - 検証が成功する
            assertThat(jwtTokenService.validateToken(accessToken)).isTrue();

            // Assert - ユーザーIDが正しく抽出される
            Long extractedUserId = jwtTokenService.extractUserId(accessToken);
            assertThat(extractedUserId).isEqualTo(1L);
        }

        @Test
        @DisplayName("リフレッシュトークンでも検証とユーザーID抽出が正常動作する")
        void shouldWorkEndToEndForRefreshToken() {
            // Act - リフレッシュトークン生成
            String refreshToken = jwtTokenService.generateRefreshToken(testAccount);

            // Assert - 検証が成功する
            assertThat(jwtTokenService.validateToken(refreshToken)).isTrue();

            // Assert - ユーザーIDが正しく抽出される
            Long extractedUserId = jwtTokenService.extractUserId(refreshToken);
            assertThat(extractedUserId).isEqualTo(1L);
        }
    }
}
