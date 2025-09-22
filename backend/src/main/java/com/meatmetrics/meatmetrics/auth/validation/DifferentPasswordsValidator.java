package com.meatmetrics.meatmetrics.auth.validation;

import com.meatmetrics.meatmetrics.auth.command.ChangePasswordCommand;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * {@link DifferentPasswords}アノテーションの検証ロジックを実装するバリデータ.
 * 
 * パスワード変更コマンドの現在のパスワードと新しいパスワードが異なることを検証します。
 */
public class DifferentPasswordsValidator implements ConstraintValidator<DifferentPasswords, ChangePasswordCommand>{
    
    /**
     * パスワードの異同を検証します.
     * 
     * @param command パスワード変更コマンド
     * @param context バリデーションコンテキスト
     * @return 新旧パスワードが異なる場合true、同一の場合false
     */
    @Override
    public boolean isValid(ChangePasswordCommand command,  ConstraintValidatorContext context){
        if(command.getCurrentPassword() == null || command.getNewPassword() == null){
            return true;
        }
        return !command.getCurrentPassword().equals(command.getNewPassword());
    }
}
