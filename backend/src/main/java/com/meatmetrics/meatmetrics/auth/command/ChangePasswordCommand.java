package com.meatmetrics.meatmetrics.auth.command;

import com.meatmetrics.meatmetrics.auth.validation.DifferentPasswords;

import jakarta.validation.constraints.*;

/**
 * パスワード変更リクエストを表すコマンドクラス.
 * 
 * 現在のパスワードと新しいパスワードを含み、
 * {@link DifferentPasswords}バリデーションにより新旧パスワードの異同を検証します。
 */
@DifferentPasswords
public class ChangePasswordCommand {
    @NotNull(message = "現在のパスワードは必須です")
    @NotBlank(message = "現在のパスワードは空白のみでは入力できません")
    private String currentPassword;
    @NotNull(message = "新しいパスワードは必須です")
    @Size(min = 8, max = 100, message = "新しいパスワードは8文字以上100文字以内で入力してください")
    private String newPassword;

    /**
     * デフォルトコンストラクタ.
     */
    public ChangePasswordCommand(){}
    
    /**
     * 全フィールドを指定してインスタンスを生成します.
     * 
     * @param currentPassword 現在のパスワード
     * @param newPassword 新しいパスワード
     */
    public ChangePasswordCommand(String currentPassword, String newPassword){
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    /**
     * 現在のパスワードを取得します.
     * 
     * @return 現在のパスワード
     */
    public String getCurrentPassword() { return currentPassword; }
    
    /**
     * 新しいパスワードを取得します.
     * 
     * @return 新しいパスワード
     */
    public String getNewPassword() { return newPassword; }

    /**
     * 現在のパスワードを設定します.
     * 
     * @param currentPassword 現在のパスワード
     */
    void setCurrentPassword(String currentPassword) { 
        this.currentPassword = currentPassword; 
    }
    
    /**
     * 新しいパスワードを設定します.
     * 
     * @param newPassword 新しいパスワード
     */
    void setNewPassword(String newPassword) { 
        this.newPassword = newPassword; 
    }

}
