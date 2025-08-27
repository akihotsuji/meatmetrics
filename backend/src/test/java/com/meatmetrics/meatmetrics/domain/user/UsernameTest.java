package com.meatmetrics.meatmetrics.domain.user;

import com.meatmetrics.meatmetrics.domain.user.exception.InvalidUsernameException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Username値オブジェクトのテスト")
class UsernameTest {

    @Test
    @DisplayName("有効なユーザー名でUsernameオブジェクトが作成できる")
    void shouldCreateValidUsername() {
        // Given
        String validUsername = "testuser";
        
        // When
        Username username = new Username(validUsername);
        
        // Then
        assertThat(username.getValue()).isEqualTo("testuser");
    }
    
    @Test
    @DisplayName("前後の空白は除去される")
    void shouldTrimWhitespace() {
        // Given
        String usernameWithSpaces = "  testuser  ";
        
        // When
        Username username = new Username(usernameWithSpaces);
        
        // Then
        assertThat(username.getValue()).isEqualTo("testuser");
    }
    
    @Test
    @DisplayName("英数字、アンダースコア、ハイフンを含むユーザー名は有効")
    void shouldAcceptValidCharacters() {
        // Given
        String[] validUsernames = {
            "user123",
            "test_user",
            "test-user",
            "User123",
            "test_user-123"
        };
        
        // When & Then
        for (String validUsername : validUsernames) {
            assertThatCode(() -> new Username(validUsername))
                .doesNotThrowAnyException();
        }
    }
    
    @Test
    @DisplayName("nullのユーザー名は無効")
    void shouldRejectNullUsername() {
        // Given & When & Then
        assertThatThrownBy(() -> new Username(null))
            .isInstanceOf(InvalidUsernameException.class)
            .hasMessageContaining("Username cannot be null or empty");
    }
    
    @Test
    @DisplayName("空文字のユーザー名は無効")
    void shouldRejectEmptyUsername() {
        // Given & When & Then
        assertThatThrownBy(() -> new Username(""))
            .isInstanceOf(InvalidUsernameException.class)
            .hasMessageContaining("Username cannot be null or empty");
    }
    
    @Test
    @DisplayName("空白のみのユーザー名は無効")
    void shouldRejectWhitespaceOnlyUsername() {
        // Given & When & Then
        assertThatThrownBy(() -> new Username("   "))
            .isInstanceOf(InvalidUsernameException.class)
            .hasMessageContaining("Username cannot be null or empty");
    }
    
    @Test
    @DisplayName("3文字未満のユーザー名は無効")
    void shouldRejectTooShortUsername() {
        // Given & When & Then
        assertThatThrownBy(() -> new Username("ab"))
            .isInstanceOf(InvalidUsernameException.class)
            .hasMessageContaining("Invalid username");
    }
    
    @Test
    @DisplayName("30文字を超えるユーザー名は無効")
    void shouldRejectTooLongUsername() {
        // Given - 31文字のユーザー名
        String longUsername = "a".repeat(31);
        
        // When & Then
        assertThatThrownBy(() -> new Username(longUsername))
            .isInstanceOf(InvalidUsernameException.class)
            .hasMessageContaining("Invalid username");
    }
    
    @Test
    @DisplayName("無効な文字を含むユーザー名は拒否される")
    void shouldRejectInvalidCharacters() {
        // Given
        String[] invalidUsernames = {
            "user@domain",
            "user name", // space
            "user.name", // dot
            "user#123",  // hash
            "user$123",  // dollar
            "user%123",  // percent
            "用户名",     // non-ASCII
            "ユーザー"    // non-ASCII
        };
        
        // When & Then
        for (String invalidUsername : invalidUsernames) {
            assertThatThrownBy(() -> new Username(invalidUsername))
                .isInstanceOf(InvalidUsernameException.class)
                .hasMessageContaining("Invalid username");
        }
    }
    
    @Test
    @DisplayName("同じユーザー名のUsernameオブジェクトは等価")
    void shouldBeEqualForSameUsername() {
        // Given
        Username username1 = new Username("testuser");
        Username username2 = new Username("testuser");
        
        // When & Then
        assertThat(username1).isEqualTo(username2);
        assertThat(username1.hashCode()).isEqualTo(username2.hashCode());
    }
    
    @Test
    @DisplayName("異なるユーザー名のUsernameオブジェクトは非等価")
    void shouldNotBeEqualForDifferentUsernames() {
        // Given
        Username username1 = new Username("testuser1");
        Username username2 = new Username("testuser2");
        
        // When & Then
        assertThat(username1).isNotEqualTo(username2);
    }
    
    @Test
    @DisplayName("isValidメソッドによる有効性チェック")
    void shouldValidateUsernameCorrectly() {
        // Given & When & Then
        assertThat(Username.isValid("testuser")).isTrue();
        assertThat(Username.isValid("test_user")).isTrue();
        assertThat(Username.isValid("test-user")).isTrue();
        assertThat(Username.isValid("ab")).isFalse(); // too short
        assertThat(Username.isValid("a".repeat(31))).isFalse(); // too long
        assertThat(Username.isValid("user name")).isFalse(); // space
        assertThat(Username.isValid(null)).isFalse();
        assertThat(Username.isValid("")).isFalse();
    }
}
