package com.meatmetrics.meatmetrics.auth.domain.account;

import com.meatmetrics.meatmetrics.auth.domain.exception.WeakPasswordException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * PasswordHashのユニットテスト
 * 
 * <p>パスワードハッシュ化、強度チェック、照合機能をテストします。</p>
 */
@DisplayName("PasswordHash")
class PasswordHashTest {

    @Nested
    @DisplayName("コンストラクタ（平文パスワードから）")
    class ConstructorFromPlainPassword {

        @Test
        @DisplayName("有効なパスワードでハッシュが生成される")
        void shouldCreateHashFromValidPassword() {
            // Arrange & Act
            PasswordHash passwordHash = new PasswordHash("password123");

            // Assert
            assertThat(passwordHash.getValue()).isNotNull();
            assertThat(passwordHash.getValue()).isNotEmpty();
            assertThat(passwordHash.getValue()).startsWith("$2a$"); // BCryptの形式
        }

        @Test
        @DisplayName("強度要件を満たさないパスワードで例外が発生する")
        void shouldThrowExceptionForWeakPassword() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new PasswordHash("weak"))
                .isInstanceOf(WeakPasswordException.class);
        }

        @Test
        @DisplayName("nullパスワードで例外が発生する")
        void shouldThrowExceptionForNullPassword() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> new PasswordHash(null))
                .isInstanceOf(WeakPasswordException.class);
        }
    }

    @Nested
    @DisplayName("fromHashメソッド（ハッシュから復元）")
    class FromHashMethod {

        @Test
        @DisplayName("有効なハッシュから復元される")
        void shouldRestoreFromValidHash() {
            // Arrange
            String validHash = "$2a$10$example.hash.value";

            // Act
            PasswordHash passwordHash = PasswordHash.fromHash(validHash);

            // Assert
            assertThat(passwordHash.getValue()).isEqualTo(validHash);
        }

        @Test
        @DisplayName("nullハッシュで例外が発生する")
        void shouldThrowExceptionForNullHash() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> PasswordHash.fromHash(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null or empty");
        }

        @Test
        @DisplayName("空文字ハッシュで例外が発生する")
        void shouldThrowExceptionForEmptyHash() {
            // Arrange & Act & Assert
            assertThatThrownBy(() -> PasswordHash.fromHash(""))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("cannot be null or empty");
        }
    }

    @Nested
    @DisplayName("isStrongEnoughメソッド（強度チェック）")
    class IsStrongEnoughMethod {

        @Test
        @DisplayName("有効なパスワードはtrueを返す")
        void shouldReturnTrueForValidPassword() {
            // Arrange & Act & Assert
            assertThat(PasswordHash.isStrongEnough("password123")).isTrue();
            assertThat(PasswordHash.isStrongEnough("myPassword1")).isTrue();
            assertThat(PasswordHash.isStrongEnough("Test1234")).isTrue();
        }

        @Test
        @DisplayName("短すぎるパスワードはfalseを返す")
        void shouldReturnFalseForTooShortPassword() {
            // Arrange & Act & Assert
            assertThat(PasswordHash.isStrongEnough("short1")).isFalse();
            assertThat(PasswordHash.isStrongEnough("abc123")).isFalse();
        }

        @Test
        @DisplayName("文字を含まないパスワードはfalseを返す")
        void shouldReturnFalseForPasswordWithoutLetters() {
            // Arrange & Act & Assert
            assertThat(PasswordHash.isStrongEnough("12345678")).isFalse();
        }

        @Test
        @DisplayName("数字を含まないパスワードはfalseを返す")
        void shouldReturnFalseForPasswordWithoutDigits() {
            // Arrange & Act & Assert
            assertThat(PasswordHash.isStrongEnough("password")).isFalse();
        }

        @Test
        @DisplayName("nullパスワードはfalseを返す")
        void shouldReturnFalseForNullPassword() {
            // Arrange & Act & Assert
            assertThat(PasswordHash.isStrongEnough(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("matchesメソッド（パスワード照合）")
    class MatchesMethod {

        @Test
        @DisplayName("正しいパスワードでtrueを返す")
        void shouldReturnTrueForCorrectPassword() {
            // Arrange
            String plainPassword = "password123";
            PasswordHash passwordHash = new PasswordHash(plainPassword);

            // Act & Assert
            assertThat(passwordHash.matches(plainPassword)).isTrue();
        }

        @Test
        @DisplayName("間違ったパスワードでfalseを返す")
        void shouldReturnFalseForIncorrectPassword() {
            // Arrange
            PasswordHash passwordHash = new PasswordHash("password123");

            // Act & Assert
            assertThat(passwordHash.matches("wrongpassword")).isFalse();
            assertThat(passwordHash.matches("Password123")).isFalse();
        }

        @Test
        @DisplayName("nullパスワードでfalseを返す")
        void shouldReturnFalseForNullPassword() {
            // Arrange
            PasswordHash passwordHash = new PasswordHash("password123");

            // Act & Assert
            assertThat(passwordHash.matches(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("equalsとhashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("同じハッシュ値のインスタンスは等価")
        void shouldBeEqualForSameHashValue() {
            // Arrange
            String hashValue = "$2a$10$example.hash.value";
            PasswordHash hash1 = PasswordHash.fromHash(hashValue);
            PasswordHash hash2 = PasswordHash.fromHash(hashValue);

            // Act & Assert
            assertThat(hash1).isEqualTo(hash2);
            assertThat(hash1.hashCode()).isEqualTo(hash2.hashCode());
        }

        @Test
        @DisplayName("異なるハッシュ値のインスタンスは非等価")
        void shouldNotBeEqualForDifferentHashValues() {
            // Arrange
            PasswordHash hash1 = new PasswordHash("password123");
            PasswordHash hash2 = new PasswordHash("different456");

            // Act & Assert
            assertThat(hash1).isNotEqualTo(hash2);
        }
    }

    @Nested
    @DisplayName("toStringメソッド")
    class ToStringMethod {

        @Test
        @DisplayName("セキュリティのためハッシュ値を出力しない")
        void shouldNotExposeHashValue() {
            // Arrange
            PasswordHash passwordHash = new PasswordHash("password123");

            // Act
            String result = passwordHash.toString();

            // Assert
            assertThat(result).isEqualTo("[PROTECTED]");
            assertThat(result).doesNotContain("$2a$"); // ハッシュ値を含まない
        }
    }
}
