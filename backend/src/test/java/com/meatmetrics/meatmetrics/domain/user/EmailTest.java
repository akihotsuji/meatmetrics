package com.meatmetrics.meatmetrics.domain.user;

import com.meatmetrics.meatmetrics.domain.user.exception.InvalidEmailException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Email値オブジェクトのテスト")
class EmailTest {

    @Test
    @DisplayName("有効なメールアドレスでEmailオブジェクトが作成できる")
    void shouldCreateValidEmail() {
        // Given
        String validEmail = "test@example.com";
        
        // When
        Email email = new Email(validEmail);
        
        // Then
        assertThat(email.getValue()).isEqualTo("test@example.com");
    }
    
    @Test
    @DisplayName("メールアドレスは正規化される（小文字変換）")
    void shouldNormalizeEmailToLowercase() {
        // Given
        String upperCaseEmail = "TEST@EXAMPLE.COM";
        
        // When
        Email email = new Email(upperCaseEmail);
        
        // Then
        assertThat(email.getValue()).isEqualTo("test@example.com");
    }
    
    @Test
    @DisplayName("前後の空白は除去される")
    void shouldTrimWhitespace() {
        // Given
        String emailWithSpaces = "  test@example.com  ";
        
        // When
        Email email = new Email(emailWithSpaces);
        
        // Then
        assertThat(email.getValue()).isEqualTo("test@example.com");
    }
    
    @Test
    @DisplayName("nullのメールアドレスは無効")
    void shouldRejectNullEmail() {
        // Given & When & Then
        assertThatThrownBy(() -> new Email(null))
            .isInstanceOf(InvalidEmailException.class)
            .hasMessageContaining("Email cannot be null or empty");
    }
    
    @Test
    @DisplayName("空文字のメールアドレスは無効")
    void shouldRejectEmptyEmail() {
        // Given & When & Then
        assertThatThrownBy(() -> new Email(""))
            .isInstanceOf(InvalidEmailException.class)
            .hasMessageContaining("Email cannot be null or empty");
    }
    
    @Test
    @DisplayName("空白のみのメールアドレスは無効")
    void shouldRejectWhitespaceOnlyEmail() {
        // Given & When & Then
        assertThatThrownBy(() -> new Email("   "))
            .isInstanceOf(InvalidEmailException.class)
            .hasMessageContaining("Email cannot be null or empty");
    }
    
    @Test
    @DisplayName("無効な形式のメールアドレスは拒否される")
    void shouldRejectInvalidEmailFormats() {
        // Given
        String[] invalidEmails = {
            "invalid-email",
            "user@",
            "@domain.com",
            "user@domain",
            "user.domain.com",
            "user@@domain.com",
            "user@domain..com"
        };
        
        // When & Then
        for (String invalidEmail : invalidEmails) {
            assertThatThrownBy(() -> new Email(invalidEmail))
                .isInstanceOf(InvalidEmailException.class)
                .hasMessageContaining("Invalid email format");
        }
    }
    
    @Test
    @DisplayName("255文字を超えるメールアドレスは拒否される")
    void shouldRejectTooLongEmail() {
        // Given - 256文字のメールアドレス（256文字になるよう調整）
        String longEmail = "a".repeat(244) + "@example.com"; // 244 + 12 = 256文字
        
        // When & Then
        assertThatThrownBy(() -> new Email(longEmail))
            .isInstanceOf(InvalidEmailException.class)
            .hasMessageContaining("Email is too long");
    }
    
    @Test
    @DisplayName("同じメールアドレスのEmailオブジェクトは等価")
    void shouldBeEqualForSameEmailAddress() {
        // Given
        Email email1 = new Email("test@example.com");
        Email email2 = new Email("TEST@EXAMPLE.COM");
        
        // When & Then
        assertThat(email1).isEqualTo(email2);
        assertThat(email1.hashCode()).isEqualTo(email2.hashCode());
    }
    
    @Test
    @DisplayName("異なるメールアドレスのEmailオブジェクトは非等価")
    void shouldNotBeEqualForDifferentEmailAddresses() {
        // Given
        Email email1 = new Email("test1@example.com");
        Email email2 = new Email("test2@example.com");
        
        // When & Then
        assertThat(email1).isNotEqualTo(email2);
    }
    
    @Test
    @DisplayName("isValidメソッドによる有効性チェック")
    void shouldValidateEmailCorrectly() {
        // Given & When & Then
        assertThat(Email.isValid("test@example.com")).isTrue();
        assertThat(Email.isValid("user.name+tag@domain.co.uk")).isTrue();
        assertThat(Email.isValid("invalid-email")).isFalse();
        assertThat(Email.isValid(null)).isFalse();
        assertThat(Email.isValid("")).isFalse();
    }
}
