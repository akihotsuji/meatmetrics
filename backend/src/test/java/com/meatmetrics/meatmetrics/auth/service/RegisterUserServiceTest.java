package com.meatmetrics.meatmetrics.auth.service;

import com.meatmetrics.meatmetrics.PostgreSQLTestBase;
import com.meatmetrics.meatmetrics.auth.command.RegisterUserCommand;
import com.meatmetrics.meatmetrics.auth.dto.UserRegisteredResult;
import com.meatmetrics.meatmetrics.domain.user.aggregate.User;
import com.meatmetrics.meatmetrics.domain.user.exception.DuplicateEmailException;
import com.meatmetrics.meatmetrics.domain.user.exception.DuplicateUsernameException;
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
 * RegisterUserServiceの単体テスト
 * 
 * <p>ユーザー登録サービスの動作を検証します。
 * PostgreSQL + Testcontainers環境でテストを実行し、実際のDBトランザクションをテストします。</p>
 * 
 * <h3>テストケース:</h3>
 * <ul>
 *   <li>正常登録ケース</li>
 *   <li>email重複エラーケース</li>
 *   <li>username重複エラーケース</li>
 *   <li>ドメインオブジェクト生成確認</li>
 * </ul>
 * 
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
@DisplayName("RegisterUserService の単体テスト")
class RegisterUserServiceTest extends PostgreSQLTestBase {

    @Autowired
    private RegisterUserService registerUserService;
    
    @Autowired
    private UserRepository userRepository;
    
    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_USERNAME = "testuser";
    private final String TEST_PASSWORD = "TestPass123";
    
    @BeforeEach
    void setUp() {
        // @Transactional により各テスト後に自動ロールバックされるため、
        // 明示的なデータ削除は不要
    }

    @Test
    @DisplayName("正常登録ケース")
    void register_ValidRequest_ShouldCreateUserSuccessfully() {
        // Arrange
        RegisterUserCommand command = new RegisterUserCommand(
            TEST_EMAIL,
            TEST_PASSWORD,
            TEST_USERNAME
        );

        // Act
        UserRegisteredResult result = registerUserService.register(command);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(result.getUsername()).isEqualTo(TEST_USERNAME);
        assertThat(result.getUserId()).isNotNull();
        assertThat(result.getCreatedAt()).isNotNull();
        
        // DBに正しく保存されていることを確認
        User savedUser = userRepository.findById(result.getUserId()).orElseThrow();
        assertThat(savedUser.getEmail().getValue()).isEqualTo(TEST_EMAIL);
        assertThat(savedUser.getUsername().getValue()).isEqualTo(TEST_USERNAME);
        assertThat(savedUser.getPasswordHash().matches(TEST_PASSWORD)).isTrue();
    }

    @Test
    @DisplayName("email重複エラーケース")
    void register_DuplicateEmail_ShouldThrowDuplicateEmailException() {
        // Arrange
        // 既存ユーザーを保存
        User existingUser = new User(
            new Email(TEST_EMAIL),
            new Username("differentuser"),
            new PasswordHash("DifferentPass123")
        );
        userRepository.save(existingUser);
        
        RegisterUserCommand command = new RegisterUserCommand(
            TEST_EMAIL,  // 重複するemail
            TEST_PASSWORD,
            TEST_USERNAME
        );

        // Act & Assert
        assertThatThrownBy(() -> {
            registerUserService.register(command);
        })
        .isInstanceOf(DuplicateEmailException.class)
        .hasMessage("Email already exists: " + TEST_EMAIL);
    }

    @Test
    @DisplayName("username重複エラーケース")
    void register_DuplicateUsername_ShouldThrowDuplicateUsernameException() {
        // Arrange
        // 既存ユーザーを保存
        User existingUser = new User(
            new Email("different@example.com"),
            new Username(TEST_USERNAME),
            new PasswordHash("DifferentPass123")
        );
        userRepository.save(existingUser);
        
        RegisterUserCommand command = new RegisterUserCommand(
            TEST_EMAIL,
            TEST_PASSWORD,
            TEST_USERNAME  // 重複するusername
        );

        // Act & Assert
        assertThatThrownBy(() -> {
            registerUserService.register(command);
        })
        .isInstanceOf(DuplicateUsernameException.class)
        .hasMessage("Username already exists: " + TEST_USERNAME);
    }

    @Test
    @DisplayName("ドメインオブジェクト生成確認")
    void register_ValidRequest_ShouldCreateCorrectDomainObjects() {
        // Arrange
        RegisterUserCommand command = new RegisterUserCommand(
            TEST_EMAIL,
            TEST_PASSWORD,
            TEST_USERNAME
        );

        // Act
        UserRegisteredResult result = registerUserService.register(command);

        // Assert
        User savedUser = userRepository.findById(result.getUserId()).orElseThrow();
        
        // Email値オブジェクトの確認
        assertThat(savedUser.getEmail()).isInstanceOf(Email.class);
        assertThat(savedUser.getEmail().getValue()).isEqualTo(TEST_EMAIL);
        
        // Username値オブジェクトの確認
        assertThat(savedUser.getUsername()).isInstanceOf(Username.class);
        assertThat(savedUser.getUsername().getValue()).isEqualTo(TEST_USERNAME);
        
        // PasswordHash値オブジェクトの確認
        assertThat(savedUser.getPasswordHash()).isInstanceOf(PasswordHash.class);
        assertThat(savedUser.getPasswordHash().matches(TEST_PASSWORD)).isTrue();
        
        // User集約ルートの確認
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("弱いパスワードでドメイン例外")
    void register_WeakPassword_ShouldThrowDomainException() {
        // Arrange
        RegisterUserCommand command = new RegisterUserCommand(
            TEST_EMAIL,
            TEST_USERNAME,
            "weak"  // 弱いパスワード
        );

        // Act & Assert
        // PasswordHashコンストラクタで例外が発生することを検証
        assertThatThrownBy(() -> {
            registerUserService.register(command);
        })
        .isInstanceOf(RuntimeException.class); // WeakPasswordException等のドメイン例外
    }

    @Test
    @DisplayName("登録後にユーザー検索可能")
    void register_ValidRequest_ShouldBeSearchableAfterRegistration() {
        // Arrange
        RegisterUserCommand command = new RegisterUserCommand(
            TEST_EMAIL,
            TEST_PASSWORD,
            TEST_USERNAME
        );

        // Act
        UserRegisteredResult result = registerUserService.register(command);

        // Assert
        // emailで検索可能
        User foundByEmail = userRepository.findByEmail(new Email(TEST_EMAIL)).orElseThrow();
        assertThat(foundByEmail.getId()).isEqualTo(result.getUserId());
        
        // usernameで検索可能
        User foundByUsername = userRepository.findByUsername(new Username(TEST_USERNAME)).orElseThrow();
        assertThat(foundByUsername.getId()).isEqualTo(result.getUserId());
    }
}
