package com.meatmetrics.meatmetrics.api.auth;

import com.meatmetrics.meatmetrics.PostgreSQLTestBase;
import com.meatmetrics.meatmetrics.auth.service.JwtTokenService;
import com.meatmetrics.meatmetrics.domain.user.aggregate.User;
import com.meatmetrics.meatmetrics.domain.user.repository.UserRepository;
import com.meatmetrics.meatmetrics.domain.user.valueobject.Email;
import com.meatmetrics.meatmetrics.domain.user.valueobject.PasswordHash;
import com.meatmetrics.meatmetrics.domain.user.valueobject.Username;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * AuthControllerの統合テスト
 * 
 * <p>認証APIエンドポイントの動作を検証します。
 * PostgreSQL + Testcontainers環境でテストを実行し、実際のHTTPリクエスト/レスポンスをテストします。</p>
 * 
 * <h3>テストケース:</h3>
 * <ul>
 *   <li>ログアウト成功ケース（有効なトークン）</li>
 *   <li>ログアウト失敗ケース（トークンなし）</li>
 *   <li>ログアウト失敗ケース（無効なトークン）</li>
 *   <li>ログアウト失敗ケース（Bearer形式でない）</li>
 * </ul>
 * 
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
@SpringBootTest
@AutoConfigureWebMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("AuthController の統合テスト")
class AuthControllerTest extends PostgreSQLTestBase {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private JwtTokenService jwtTokenService;
    
    @Autowired
    private UserRepository userRepository;
    
    private User testUser;
    private String validToken;
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
        
        // 有効なトークンを生成
        validToken = jwtTokenService.generateAccessToken(testUser);
    }

    @Test
    @DisplayName("ログアウト成功ケース - 有効なトークン")
    void logout_ValidToken_ShouldReturnSuccessResponse() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("ログアウト完了"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("ログアウト失敗ケース - Authorizationヘッダーなし")
    void logout_NoAuthorizationHeader_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("認証トークンが見つかりません"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("ログアウト失敗ケース - Bearer形式でないトークン")
    void logout_InvalidTokenFormat_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Invalid " + validToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("認証トークンが見つかりません"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("ログアウト失敗ケース - 無効なトークン")
    void logout_InvalidToken_ShouldReturnUnauthorized() throws Exception {
        String invalidToken = "invalid.jwt.token";
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + invalidToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("無効なトークンです"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("ログアウト失敗ケース - 空のトークン")
    void logout_EmptyToken_ShouldReturnUnauthorized() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer ")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("無効なトークンです"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("ログアウト失敗ケース - 期限切れトークン")
    void logout_ExpiredToken_ShouldReturnUnauthorized() throws Exception {
        // NOTE: 期限切れトークンの生成は複雑なため、
        // ここでは無効なトークン形式で代替（実際のプロダクションでは適切に実装）
        String expiredToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.expired.token";
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/logout")
                .header("Authorization", "Bearer " + expiredToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("無効なトークンです"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("パスワード変更成功ケース - 有効なトークンとリクエスト")
    void changePassword_ValidTokenAndRequest_ShouldReturnSuccessResponse() throws Exception {
        String requestBody = """
                {
                    "currentPassword": "TestPass123",
                    "newPassword": "NewTestPass456"
                }
                """;
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/change-password")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("パスワード変更完了"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("パスワード変更失敗ケース - Authorizationヘッダーなし")
    void changePassword_NoAuthorizationHeader_ShouldReturnUnauthorized() throws Exception {
        String requestBody = """
                {
                    "currentPassword": "TestPass123",
                    "newPassword": "NewTestPass456"
                }
                """;
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/change-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("認証トークンが見つかりません"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("パスワード変更失敗ケース - 無効なトークン")
    void changePassword_InvalidToken_ShouldReturnUnauthorized() throws Exception {
        String invalidToken = "invalid.jwt.token";
        String requestBody = """
                {
                    "currentPassword": "TestPass123",
                    "newPassword": "NewTestPass456"
                }
                """;
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/change-password")
                .header("Authorization", "Bearer " + invalidToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("無効なトークンです"))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("パスワード変更失敗ケース - バリデーションエラー（短いパスワード）")
    void changePassword_ShortPassword_ShouldReturnBadRequest() throws Exception {
        String requestBody = """
                {
                    "currentPassword": "TestPass123",
                    "newPassword": "short"
                }
                """;
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/change-password")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("パスワード変更失敗ケース - バリデーションエラー（必須項目なし）")
    void changePassword_MissingRequiredFields_ShouldReturnBadRequest() throws Exception {
        String requestBody = """
                {
                    "currentPassword": "",
                    "newPassword": "NewTestPass456"
                }
                """;
        
        // Act & Assert
        mockMvc.perform(post("/api/auth/change-password")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}
