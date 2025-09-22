package com.meatmetrics.meatmetrics.auth.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * パスワード変更時に新旧パスワードが異なることを検証するカスタムバリデーション.
 * 
 * {@link ChangePasswordCommand}クラスに適用され、現在のパスワードと新しいパスワードが
 * 同一でないことを保証します。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy  = DifferentPasswordsValidator.class)
public @interface DifferentPasswords {
    String message() default "新しいパスワードは現在のパスワードと異なる必要があります";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
