package com.meatmetrics.meatmetrics.auth.service;

import com.meatmetrics.meatmetrics.PostgreSQLTestBase;
import com.meatmetrics.meatmetrics.auth.command.ChangePasswordCommand;
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

import java.util.NoSuchElementException;

import static org.assertj.core.api.Assertions.*;

/**
 * ChangePasswordServiceの単体テスト
 * 
 * <p>パスワード変更サービスの動作を検証します。
 * PostgreSQL + Testcontainers環境でテストを実行し、実際のDBトランザクションをテストします。</p>
 * 
 * <h3>テストケース:</h3>
 * <ul>
 *   <li>正常なパスワード変更ケース</li>
 *   <li>現在パスワード不一致エラーケース</li>
 *   <li>ユーザー未存在エラーケース</li>
 * </ul>
 * 
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("ChangePasswordService の単体テスト")
class ChangePasswordServiceTest extends PostgreSQLTestBase {

    @Autowired
    private ChangePasswordService changePasswordService;
    
    @Autowired
    private UserRepository userRepository;
    
    private User testUser;
    private final String CURRENT_PASSWORD = "CurrentPass123";
    private final String NEW_PASSWORD = "NewPass456";

    @BeforeEach
    void setUp() {
        // テスト用ユーザーの準備
        testUser = new User(
            new Email("test@example.com"),
            new Username("testuser"),
            new PasswordHash(CURRENT_PASSWORD)
        );
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("正常なパスワード変更ケース")
    void changePassword_ValidRequest_ShouldUpdatePassword() {
        // Arrange
        ChangePasswordCommand command = new ChangePasswordCommand(
            CURRENT_PASSWORD,
            NEW_PASSWORD
        );

        // Act
        assertThatNoException().isThrownBy(() -> {
            changePasswordService.changePassword(testUser.getId(), command);
        });

        // Assert
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getPasswordHash().matches(NEW_PASSWORD)).isTrue();
        assertThat(updatedUser.getPasswordHash().matches(CURRENT_PASSWORD)).isFalse();
    }

    @Test
    @DisplayName("現在パスワード不一致でAuthenticationException")
    void changePassword_WrongCurrentPassword_ShouldThrowAuthenticationException() {
        // Arrange
        ChangePasswordCommand command = new ChangePasswordCommand(
            "WrongPassword123",
            NEW_PASSWORD
        );

        // Act & Assert
        assertThatThrownBy(() -> {
            changePasswordService.changePassword(testUser.getId(), command);
        })
        .isInstanceOf(AuthenticationException.class)
        .hasMessage("現在のパスワードが正しくありません");
    }

    @Test
    @DisplayName("ユーザー未存在でNoSuchElementException")
    void changePassword_UserNotFound_ShouldThrowNoSuchElementException() {
        // Arrange
        Long nonExistentUserId = 99999L;
        ChangePasswordCommand command = new ChangePasswordCommand(
            CURRENT_PASSWORD,
            NEW_PASSWORD
        );

        // Act & Assert
        assertThatThrownBy(() -> {
            changePasswordService.changePassword(nonExistentUserId, command);
        })
        .isInstanceOf(NoSuchElementException.class)
        .hasMessage("ユーザーが見つかりません");
    }

    @Test
    @DisplayName("新パスワードが弱い場合のドメイン例外")
    void changePassword_WeakNewPassword_ShouldThrowDomainException() {
        // Arrange
        ChangePasswordCommand command = new ChangePasswordCommand(
            CURRENT_PASSWORD,
            "weak"  // 弱いパスワード
        );

        // Act & Assert
        // PasswordHashコンストラクタで例外が発生することを検証
        assertThatThrownBy(() -> {
            changePasswordService.changePassword(testUser.getId(), command);
        })
        .isInstanceOf(RuntimeException.class); // WeakPasswordException等のドメイン例外
    }

    @Test
    @DisplayName("パスワード変更後も他のユーザー情報は変更されない")
    void changePassword_ValidRequest_ShouldNotChangeOtherUserProperties() {
        // Arrange
        String originalUsername = testUser.getUsername().getValue();
        String originalEmail = testUser.getEmail().getValue();
        ChangePasswordCommand command = new ChangePasswordCommand(
            CURRENT_PASSWORD,
            NEW_PASSWORD
        );

        // Act
        changePasswordService.changePassword(testUser.getId(), command);

        // Assert
        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        assertThat(updatedUser.getUsername().getValue()).isEqualTo(originalUsername);
        assertThat(updatedUser.getEmail().getValue()).isEqualTo(originalEmail);
    }
}
