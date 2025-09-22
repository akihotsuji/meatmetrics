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
 * ChangePasswordCommand のバリデーションテスト。
 */
@DisplayName("ChangePasswordCommand のバリデーション")
class ChangePasswordCommandTest {

    private final ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    private final Validator validator = factory.getValidator();

    @Test
    @DisplayName("正常: すべての項目が妥当（新旧が異なる）")
    void validCommand_shouldPassValidation() {
        ChangePasswordCommand cmd = new ChangePasswordCommand("CurrentPass123", "NewPass456");
        Set<ConstraintViolation<ChangePasswordCommand>> violations = validator.validate(cmd);
        assertThat(violations).isEmpty();
    }

    @Nested
    @DisplayName("現在パスワードの検証")
    class CurrentPasswordValidation {
        @Test
        @DisplayName("必須違反")
        void currentPassword_null_shouldFail() {
            ChangePasswordCommand cmd = new ChangePasswordCommand(null, "NewPass456");
            Set<ConstraintViolation<ChangePasswordCommand>> violations = validator.validate(cmd);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("currentPassword"));
        }

        @Test
        @DisplayName("空白のみ不正")
        void currentPassword_blank_shouldFail() {
            ChangePasswordCommand cmd = new ChangePasswordCommand("   ", "NewPass456");
            Set<ConstraintViolation<ChangePasswordCommand>> violations = validator.validate(cmd);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("currentPassword"));
        }
    }

    @Nested
    @DisplayName("新しいパスワードの検証")
    class NewPasswordValidation {
        @Test
        @DisplayName("必須違反")
        void newPassword_null_shouldFail() {
            ChangePasswordCommand cmd = new ChangePasswordCommand("CurrentPass123", null);
            Set<ConstraintViolation<ChangePasswordCommand>> violations = validator.validate(cmd);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("newPassword"));
        }

        @Test
        @DisplayName("短すぎる")
        void newPassword_tooShort_shouldFail() {
            ChangePasswordCommand cmd = new ChangePasswordCommand("CurrentPass123", "short");
            Set<ConstraintViolation<ChangePasswordCommand>> violations = validator.validate(cmd);
            assertThat(violations).anyMatch(v -> v.getPropertyPath().toString().equals("newPassword"));
        }
    }

    @Test
    @DisplayName("カスタム: 新旧パスワードが同一ならエラー")
    void samePasswords_shouldFailDifferentPasswordsConstraint() {
        ChangePasswordCommand cmd = new ChangePasswordCommand("SamePass123", "SamePass123");
        Set<ConstraintViolation<ChangePasswordCommand>> violations = validator.validate(cmd);
        assertThat(violations)
            .anyMatch(v -> v.getMessage().contains("新しいパスワードは現在のパスワードと異なる") ||
                           v.getPropertyPath().toString().equals("") // クラスレベル制約
            );
    }
}
