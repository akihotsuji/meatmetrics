package com.meatmetrics.meatmetrics.domain.user;

import com.meatmetrics.meatmetrics.domain.user.valueobject.PasswordHash;
import com.meatmetrics.meatmetrics.domain.user.exception.WeakPasswordException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PasswordHash値オブジェクトのテスト")
class PasswordHashTest {

    @Test
    @DisplayName("強度要件を満たすパスワードでPasswordHashオブジェクトが作成できる")
    void shouldCreateValidPasswordHash() {
        // Given
        String strongPassword = "password123";
        
        // When
        PasswordHash passwordHash = new PasswordHash(strongPassword);
        
        // Then
        assertThat(passwordHash.getValue()).isNotNull();
        assertThat(passwordHash.getValue()).isNotEqualTo(strongPassword); // ハッシュ化されている
    }
    
    @Test
    @DisplayName("パスワードマッチングが正しく動作する")
    void shouldMatchCorrectPassword() {
        // Given
        String password = "password123";
        PasswordHash passwordHash = new PasswordHash(password);
        
        // When & Then
        assertThat(passwordHash.matches(password)).isTrue();
        assertThat(passwordHash.matches("wrongpassword")).isFalse();
        assertThat(passwordHash.matches(null)).isFalse();
    }
    
    @Test
    @DisplayName("fromHashメソッドでハッシュ化済みパスワードから復元できる")
    void shouldCreateFromExistingHash() {
        // Given
        String originalPassword = "password123";
        PasswordHash originalHash = new PasswordHash(originalPassword);
        String hashedValue = originalHash.getValue();
        
        // When
        PasswordHash restoredHash = PasswordHash.fromHash(hashedValue);
        
        // Then
        assertThat(restoredHash.getValue()).isEqualTo(hashedValue);
        assertThat(restoredHash.matches(originalPassword)).isTrue();
    }
    
    @Test
    @DisplayName("fromHashメソッドでnullまたは空文字は拒否される")
    void shouldRejectNullOrEmptyHashInFromHash() {
        // Given & When & Then
        assertThatThrownBy(() -> PasswordHash.fromHash(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Hashed password cannot be null or empty");
            
        assertThatThrownBy(() -> PasswordHash.fromHash(""))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Hashed password cannot be null or empty");
            
        assertThatThrownBy(() -> PasswordHash.fromHash("   "))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Hashed password cannot be null or empty");
    }
    
    @Test
    @DisplayName("8文字未満のパスワードは脆弱として拒否される")
    void shouldRejectTooShortPassword() {
        // Given
        String shortPassword = "pass12";
        
        // When & Then
        assertThatThrownBy(() -> new PasswordHash(shortPassword))
            .isInstanceOf(WeakPasswordException.class)
            .hasMessageContaining("Password does not meet strength requirements");
    }
    
    @Test
    @DisplayName("英文字を含まないパスワードは脆弱として拒否される")
    void shouldRejectPasswordWithoutLetters() {
        // Given
        String noLettersPassword = "12345678";
        
        // When & Then
        assertThatThrownBy(() -> new PasswordHash(noLettersPassword))
            .isInstanceOf(WeakPasswordException.class)
            .hasMessageContaining("Password does not meet strength requirements");
    }
    
    @Test
    @DisplayName("数字を含まないパスワードは脆弱として拒否される")
    void shouldRejectPasswordWithoutDigits() {
        // Given
        String noDigitsPassword = "password";
        
        // When & Then
        assertThatThrownBy(() -> new PasswordHash(noDigitsPassword))
            .isInstanceOf(WeakPasswordException.class)
            .hasMessageContaining("Password does not meet strength requirements");
    }
    
    @Test
    @DisplayName("nullパスワードは脆弱として拒否される")
    void shouldRejectNullPassword() {
        // Given & When & Then
        assertThatThrownBy(() -> new PasswordHash(null))
            .isInstanceOf(WeakPasswordException.class)
            .hasMessageContaining("Password does not meet strength requirements");
    }
    
    @Test
    @DisplayName("isStrongEnoughメソッドによる強度チェック")
    void shouldValidatePasswordStrengthCorrectly() {
        // Given & When & Then
        // 有効なパスワード
        assertThat(PasswordHash.isStrongEnough("password123")).isTrue();
        assertThat(PasswordHash.isStrongEnough("StrongPass1")).isTrue();
        assertThat(PasswordHash.isStrongEnough("mypassword123")).isTrue();
        
        // 無効なパスワード
        assertThat(PasswordHash.isStrongEnough("pass12")).isFalse(); // 短すぎる
        assertThat(PasswordHash.isStrongEnough("password")).isFalse(); // 数字なし
        assertThat(PasswordHash.isStrongEnough("12345678")).isFalse(); // 英文字なし
        assertThat(PasswordHash.isStrongEnough(null)).isFalse(); // null
    }
    
    @Test
    @DisplayName("同じハッシュ値のPasswordHashオブジェクトは等価")
    void shouldBeEqualForSameHashValue() {
        // Given
        String password = "password123";
        PasswordHash hash1 = new PasswordHash(password);
        PasswordHash hash2 = PasswordHash.fromHash(hash1.getValue());
        
        // When & Then
        assertThat(hash1).isEqualTo(hash2);
        assertThat(hash1.hashCode()).isEqualTo(hash2.hashCode());
    }
    
    @Test
    @DisplayName("異なるハッシュ値のPasswordHashオブジェクトは非等価")
    void shouldNotBeEqualForDifferentHashValues() {
        // Given
        PasswordHash hash1 = new PasswordHash("password123");
        PasswordHash hash2 = new PasswordHash("password456");
        
        // When & Then
        assertThat(hash1).isNotEqualTo(hash2);
    }
    
    @Test
    @DisplayName("toStringメソッドはハッシュ値を出力しない")
    void shouldNotExposeHashValueInToString() {
        // Given
        PasswordHash passwordHash = new PasswordHash("password123");
        
        // When
        String toString = passwordHash.toString();
        
        // Then
        assertThat(toString).isEqualTo("[PROTECTED]");
        assertThat(toString).doesNotContain(passwordHash.getValue());
    }
}
