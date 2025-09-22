package com.meatmetrics.meatmetrics.auth.command;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;

/**
 * RegisterUserCommand のバリデーションテスト。
 */
@DisplayName("RegisterUserCommand のバリデーション")
class RegisterUserCommandTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    @DisplayName("正常: すべての項目が妥当")
    void validCommand_shouldPassValidation() {
        RegisterUserCommand cmd = new RegisterUserCommand("user@example.com", "Password123", "valid_user-01");
        Set<ConstraintViolation<RegisterUserCommand>> violations = validator.validate(cmd);
        assertThat(violations).isEmpty();
    }

    @Nested
    @DisplayName("メールアドレスの検証")
    class EmailValidation {
        @Test
        @DisplayName("必須違反")
        void email_null_shouldFail() {
            RegisterUserCommand cmd = new RegisterUserCommand(null, "Password123", "validuser");
            Set<ConstraintViolation<RegisterUserCommand>> violations = validator.validate(cmd);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        }

        @Test
        @DisplayName("形式不正")
        void email_invalidFormat_shouldFail() {
            RegisterUserCommand cmd = new RegisterUserCommand("not-an-email", "Password123", "validuser");
            Set<ConstraintViolation<RegisterUserCommand>> violations = validator.validate(cmd);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        }

        @Test
        @DisplayName("長さ上限超過")
        void email_tooLong_shouldFail() {
            String longLocal = "a".repeat(256);
            RegisterUserCommand cmd = new RegisterUserCommand(longLocal + "@example.com", "Password123", "validuser");
            Set<ConstraintViolation<RegisterUserCommand>> violations = validator.validate(cmd);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        }
    }

    @Nested
    @DisplayName("パスワードの検証")
    class PasswordValidation {
        @Test
        @DisplayName("必須違反")
        void password_null_shouldFail() {
            RegisterUserCommand cmd = new RegisterUserCommand("user@example.com", null, "validuser");
            Set<ConstraintViolation<RegisterUserCommand>> violations = validator.validate(cmd);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
        }

        @Test
        @DisplayName("短すぎる")
        void password_tooShort_shouldFail() {
            RegisterUserCommand cmd = new RegisterUserCommand("user@example.com", "short", "validuser");
            Set<ConstraintViolation<RegisterUserCommand>> violations = validator.validate(cmd);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
        }
    }

    @Nested
    @DisplayName("ユーザー名の検証")
    class UsernameValidation {
        @Test
        @DisplayName("必須違反")
        void username_null_shouldFail() {
            RegisterUserCommand cmd = new RegisterUserCommand("user@example.com", "Password123", null);
            Set<ConstraintViolation<RegisterUserCommand>> violations = validator.validate(cmd);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
        }

        @Test
        @DisplayName("短すぎる")
        void username_tooShort_shouldFail() {
            RegisterUserCommand cmd = new RegisterUserCommand("user@example.com", "Password123", "ab");
            Set<ConstraintViolation<RegisterUserCommand>> violations = validator.validate(cmd);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
        }

        @Test
        @DisplayName("不許可文字を含む")
        void username_invalidChars_shouldFail() {
            RegisterUserCommand cmd = new RegisterUserCommand("user@example.com", "Password123", "invalid!*?");
            Set<ConstraintViolation<RegisterUserCommand>> violations = validator.validate(cmd);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("username"));
        }
    }
}
