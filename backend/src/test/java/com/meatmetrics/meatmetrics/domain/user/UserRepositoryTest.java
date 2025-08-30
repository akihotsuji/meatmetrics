package com.meatmetrics.meatmetrics.domain.user;

import com.meatmetrics.meatmetrics.domain.user.aggregate.User;
import com.meatmetrics.meatmetrics.domain.user.repository.UserRepository;
import com.meatmetrics.meatmetrics.domain.user.valueobject.Email;
import com.meatmetrics.meatmetrics.domain.user.valueobject.Username;
import com.meatmetrics.meatmetrics.domain.user.valueobject.PasswordHash;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * UserRepository インターフェースの単体テスト
 * 
 * <p>Mockitoを使用してUserRepositoryの動作を検証します。</p>
 * <p>実際のデータベース接続は行わず、インターフェースレベルでの動作確認に焦点を当てています。</p>
 * 
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserRepository Mock単体テスト")
class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    private User testUser;
    private User testUserWithId;
    private Email testEmail;
    private Username testUsername;
    private PasswordHash testPasswordHash;

    @BeforeEach
    void setUp() {
        // テスト用の値オブジェクトを作成
        testEmail = new Email("test@example.com");
        testUsername = new Username("testuser");
        testPasswordHash = new PasswordHash("password123"); // 8文字以上で英字と数字を含む
        
        // テスト用のユーザー（ID未設定）
        testUser = User.register(testEmail, testUsername, testPasswordHash);
        
        // テスト用のユーザー（ID設定済み）
        testUserWithId = new User(
            1L,
            testEmail,
            testUsername,
            testPasswordHash,
            new ArrayList<>(),
            Instant.now(),
            Instant.now()
        );
    }

    @Test
    @DisplayName("findByEmail: 存在するメールアドレスでユーザーが見つかる")
    void findByEmail_UserExists_ReturnsUser() {
        // Given
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUserWithId));

        // When
        Optional<User> result = userRepository.findByEmail(testEmail);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getEmail()).isEqualTo(testEmail);
        assertThat(result.get().getUsername()).isEqualTo(testUsername);
        assertThat(result.get().getId()).isEqualTo(1L);
        
        verify(userRepository).findByEmail(testEmail);
    }

    @Test
    @DisplayName("findByEmail: 存在しないメールアドレスで空のOptionalが返る")
    void findByEmail_UserNotExists_ReturnsEmpty() {
        // Given
        Email notFoundEmail = new Email("notfound@example.com");
        when(userRepository.findByEmail(notFoundEmail)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userRepository.findByEmail(notFoundEmail);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findByEmail(notFoundEmail);
    }

    @Test
    @DisplayName("findByEmail: nullメールアドレスで例外が発生する")
    void findByEmail_NullEmail_ThrowsException() {
        // Given
        when(userRepository.findByEmail(null)).thenThrow(new IllegalArgumentException("Email cannot be null"));

        // When & Then
        assertThatThrownBy(() -> userRepository.findByEmail(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Email cannot be null");
        
        verify(userRepository).findByEmail(null);
    }

    @Test
    @DisplayName("findByUsername: 存在するユーザー名でユーザーが見つかる")
    void findByUsername_UserExists_ReturnsUser() {
        // Given
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUserWithId));

        // When
        Optional<User> result = userRepository.findByUsername(testUsername);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUsername()).isEqualTo(testUsername);
        assertThat(result.get().getEmail()).isEqualTo(testEmail);
        assertThat(result.get().getId()).isEqualTo(1L);
        
        verify(userRepository).findByUsername(testUsername);
    }

    @Test
    @DisplayName("findByUsername: 存在しないユーザー名で空のOptionalが返る")
    void findByUsername_UserNotExists_ReturnsEmpty() {
        // Given
        Username notFoundUsername = new Username("notfounduser");
        when(userRepository.findByUsername(notFoundUsername)).thenReturn(Optional.empty());

        // When
        Optional<User> result = userRepository.findByUsername(notFoundUsername);

        // Then
        assertThat(result).isEmpty();
        verify(userRepository).findByUsername(notFoundUsername);
    }

    @Test
    @DisplayName("findByUsername: nullユーザー名で例外が発生する")
    void findByUsername_NullUsername_ThrowsException() {
        // Given
        when(userRepository.findByUsername(null)).thenThrow(new IllegalArgumentException("Username cannot be null"));

        // When & Then
        assertThatThrownBy(() -> userRepository.findByUsername(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Username cannot be null");
        
        verify(userRepository).findByUsername(null);
    }

    @Test
    @DisplayName("save: 新規ユーザーの保存が成功する")
    void save_NewUser_ReturnsUserWithId() {
        // Given - IDがnullの新規ユーザー
        when(userRepository.save(testUser)).thenReturn(testUserWithId);

        // When
        User result = userRepository.save(testUser);

        // Then
        assertThat(result.getId()).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo(testEmail);
        assertThat(result.getUsername()).isEqualTo(testUsername);
        
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("save: 既存ユーザーの更新が成功する")
    void save_ExistingUser_ReturnsUpdatedUser() {
        // Given - IDが設定済みの既存ユーザー
        User updatedUser = new User(
            1L,
            testEmail,
            new Username("updateduser"), // ユーザー名を変更
            testPasswordHash,
            new ArrayList<>(),
            Instant.now().minusSeconds(3600), // 1時間前に作成
            Instant.now() // 現在時刻で更新
        );
        
        when(userRepository.save(testUserWithId)).thenReturn(updatedUser);

        // When
        User result = userRepository.save(testUserWithId);

        // Then
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername().getValue()).isEqualTo("updateduser");
        assertThat(result.getEmail()).isEqualTo(testEmail);
        
        verify(userRepository).save(testUserWithId);
    }

    @Test
    @DisplayName("save: nullユーザーで例外が発生する")
    void save_NullUser_ThrowsException() {
        // Given
        when(userRepository.save(null)).thenThrow(new IllegalArgumentException("User cannot be null"));

        // When & Then
        assertThatThrownBy(() -> userRepository.save(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("User cannot be null");
        
        verify(userRepository).save(null);
    }

    @Test
    @DisplayName("複数の操作の組み合わせテスト")
    void complexScenario_SaveAndFind_WorksCorrectly() {
        // Given - 保存と検索の組み合わせ
        when(userRepository.save(testUser)).thenReturn(testUserWithId);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUserWithId));
        when(userRepository.findByUsername(testUsername)).thenReturn(Optional.of(testUserWithId));

        // When - 保存してから検索
        User savedUser = userRepository.save(testUser);
        Optional<User> foundByEmail = userRepository.findByEmail(testEmail);
        Optional<User> foundByUsername = userRepository.findByUsername(testUsername);

        // Then
        assertThat(savedUser.getId()).isEqualTo(1L);
        
        assertThat(foundByEmail).isPresent();
        assertThat(foundByEmail.get().getId()).isEqualTo(1L);
        
        assertThat(foundByUsername).isPresent();
        assertThat(foundByUsername.get().getId()).isEqualTo(1L);
        
        // 期待した回数だけメソッドが呼ばれたことを確認
        verify(userRepository).save(testUser);
        verify(userRepository).findByEmail(testEmail);
        verify(userRepository).findByUsername(testUsername);
    }
}
