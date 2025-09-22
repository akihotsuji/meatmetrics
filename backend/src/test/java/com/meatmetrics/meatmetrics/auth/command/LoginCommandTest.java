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
 * LoginCommand のバリデーションテスト。
 */
@DisplayName("LoginCommand のバリデーション")
class LoginCommandTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    @DisplayName("正常: すべての項目が妥当")
    void validCommand_shouldPassValidation() {
        LoginCommand cmd = new LoginCommand("user@example.com", "Password123");
        Set<ConstraintViolation<LoginCommand>> violations = validator.validate(cmd);
        assertThat(violations).isEmpty();
    }

    @Nested
    @DisplayName("メールアドレスの検証")
    class EmailValidation {
        @Test
        @DisplayName("必須違反")
        void email_null_shouldFail() {
            LoginCommand cmd = new LoginCommand(null, "Password123");
            Set<ConstraintViolation<LoginCommand>> violations = validator.validate(cmd);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        }

        @Test
        @DisplayName("形式不正")
        void email_invalidFormat_shouldFail() {
            LoginCommand cmd = new LoginCommand("not-an-email", "Password123");
            Set<ConstraintViolation<LoginCommand>> violations = validator.validate(cmd);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        }
    }

    @Nested
    @DisplayName("パスワードの検証")
    class PasswordValidation {
        @Test
        @DisplayName("必須違反")
        void password_null_shouldFail() {
            LoginCommand cmd = new LoginCommand("user@example.com", null);
            Set<ConstraintViolation<LoginCommand>> violations = validator.validate(cmd);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
        }

        @Test
        @DisplayName("空白のみ不正")
        void password_blank_shouldFail() {
            LoginCommand cmd = new LoginCommand("user@example.com", "   ");
            Set<ConstraintViolation<LoginCommand>> violations = validator.validate(cmd);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("password"));
        }
    }
}
