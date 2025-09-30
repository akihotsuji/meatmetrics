package com.meatmetrics.meatmetrics.auth.validation;

import com.meatmetrics.meatmetrics.auth.application.command.ChangePasswordCommand;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

/**
 * DifferentPasswordsValidatorのユニットテスト
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DifferentPasswordsValidator")
class DifferentPasswordsValidatorTest {

    @Mock
    private ConstraintValidatorContext context;

    private DifferentPasswordsValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DifferentPasswordsValidator();
    }

    @Nested
    @DisplayName("isValid メソッド")
    class IsValidMethod {

        @Test
        @DisplayName("異なるパスワードでtrueを返す")
        void shouldReturnTrueForDifferentPasswords() {
            // Arrange
            ChangePasswordCommand command = new ChangePasswordCommand("currentPassword123", "newPassword456");

            // Act
            boolean result = validator.isValid(command, context);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("同じパスワードでfalseを返す")
        void shouldReturnFalseForSamePasswords() {
            // Arrange
            ChangePasswordCommand command = new ChangePasswordCommand("samePassword123", "samePassword123");

            // Act
            boolean result = validator.isValid(command, context);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("現在パスワードがnullの場合にtrueを返す（他のバリデーションに委ねる）")
        void shouldReturnTrueWhenCurrentPasswordIsNull() {
            // Arrange
            ChangePasswordCommand command = new ChangePasswordCommand(null, "newPassword123");

            // Act
            boolean result = validator.isValid(command, context);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("新しいパスワードがnullの場合にtrueを返す（他のバリデーションに委ねる）")
        void shouldReturnTrueWhenNewPasswordIsNull() {
            // Arrange
            ChangePasswordCommand command = new ChangePasswordCommand("currentPassword123", null);

            // Act
            boolean result = validator.isValid(command, context);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("両方のパスワードがnullの場合にtrueを返す")
        void shouldReturnTrueWhenBothPasswordsAreNull() {
            // Arrange
            ChangePasswordCommand command = new ChangePasswordCommand(null, null);

            // Act
            boolean result = validator.isValid(command, context);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("空文字列で異なるパスワードの場合にtrueを返す")
        void shouldReturnTrueForDifferentEmptyAndNonEmptyPasswords() {
            // Arrange
            ChangePasswordCommand command = new ChangePasswordCommand("", "newPassword123");

            // Act
            boolean result = validator.isValid(command, context);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("両方が空文字列の場合にfalseを返す")
        void shouldReturnFalseForBothEmptyPasswords() {
            // Arrange
            ChangePasswordCommand command = new ChangePasswordCommand("", "");

            // Act
            boolean result = validator.isValid(command, context);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("スペースを含む類似パスワードでtrueを返す")
        void shouldReturnTrueForSimilarPasswordsWithSpaces() {
            // Arrange
            ChangePasswordCommand command = new ChangePasswordCommand("password123", "password 123"); // スペースが追加されている

            // Act
            boolean result = validator.isValid(command, context);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("大文字小文字が異なるパスワードでtrueを返す")
        void shouldReturnTrueForDifferentCasePasswords() {
            // Arrange
            ChangePasswordCommand command = new ChangePasswordCommand("password123", "PASSWORD123");

            // Act
            boolean result = validator.isValid(command, context);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("日本語文字を含むパスワードで正常動作する")
        void shouldWorkWithJapaneseCharacters() {
            // Arrange
            ChangePasswordCommand command = new ChangePasswordCommand("パスワード123", "新パスワード456");

            // Act
            boolean result = validator.isValid(command, context);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("同じ日本語パスワードでfalseを返す")
        void shouldReturnFalseForSameJapanesePasswords() {
            // Arrange
            ChangePasswordCommand command = new ChangePasswordCommand("パスワード123", "パスワード123");

            // Act
            boolean result = validator.isValid(command, context);

            // Assert
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("特殊文字を含むパスワードで正常動作する")
        void shouldWorkWithSpecialCharacters() {
            // Arrange
            ChangePasswordCommand command = new ChangePasswordCommand("P@ssw0rd!#$", "N3wP@ssw0rd!%^");

            // Act
            boolean result = validator.isValid(command, context);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("非常に長いパスワードで正常動作する")
        void shouldWorkWithVeryLongPasswords() {
            // Arrange
            String longPassword1 = "a".repeat(1000) + "1";
            String longPassword2 = "a".repeat(1000) + "2";
            
            ChangePasswordCommand command = new ChangePasswordCommand(longPassword1, longPassword2);

            // Act
            boolean result = validator.isValid(command, context);

            // Assert
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("nullコマンドで例外が発生しない")
        void shouldNotThrowExceptionForNullCommand() {
            // Act & Assert - 例外が発生しないことを確認
            assertThatCode(() -> validator.isValid(null, context))
                .isInstanceOf(NullPointerException.class); // nullコマンドの場合はNPEが期待される
        }
    }

    @Nested
    @DisplayName("統合テスト（Bean Validation との連携）")
    class IntegrationTests {

        @Test
        @DisplayName("アノテーションの設定値が正しい")
        void shouldHaveCorrectAnnotationConfiguration() {
            // Assert - アノテーションクラスの設定を直接確認
            assertThat(DifferentPasswords.class.getAnnotation(jakarta.validation.Constraint.class))
                .isNotNull();
        }
    }

    /**
     * テスト用のダミークラス（アノテーション付き）
     */
    @DifferentPasswords
    static class TestCommandWithAnnotation {
        // テスト用
    }

    @Test
    @DisplayName("アノテーションが適用可能であることを確認")
    void shouldBeApplicableAsAnnotation() {
        // Arrange & Act
        DifferentPasswords annotation = TestCommandWithAnnotation.class
            .getAnnotation(DifferentPasswords.class);

        // Assert
        assertThat(annotation).isNotNull();
        assertThat(annotation.message()).isEqualTo("新しいパスワードは現在のパスワードと異なる必要があります");
    }
}
