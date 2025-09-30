package com.meatmetrics.meatmetrics.api.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.meatmetrics.meatmetrics.auth.application.command.ChangePasswordCommand;
import com.meatmetrics.meatmetrics.auth.validation.DifferentPasswords;

/**
 * パスワード変更リクエストDTO
 * 
 * <p>POST /api/auth/change-password のリクエストボディを受け取るためのDTO。</p>
 * <p>外部入力の完全バリデーション（HTTP制約・ビジネスルール）を担当し、
 * 検証済みデータをChangePasswordCommandに変換してサービス層に渡す。</p>
 * 
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
@DifferentPasswords
public class ChangePasswordRequest {
    
    /** 現在のパスワード（必須、空白不可） */
    @NotNull(message = "現在のパスワードは必須です")
    @NotBlank(message = "現在のパスワードは空白のみでは入力できません")
    private String currentPassword;
    
    /** 新しいパスワード（必須、8文字以上100文字以内） */
    @NotNull(message = "新しいパスワードは必須です")
    @NotBlank(message = "新しいパスワードは必須です")
    @Size(min = 8, max = 100, message = "新しいパスワードは8文字以上100文字以内で入力してください")
    private String newPassword;
    
    /** デフォルトコンストラクタ（Jackson用） */
    public ChangePasswordRequest() {}
    
    /**
     * 全項目指定コンストラクタ
     * 
     * @param currentPassword 現在のパスワード
     * @param newPassword 新しいパスワード
     */
    public ChangePasswordRequest(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }
    
    /**
     * ChangePasswordCommandに変換
     * 
     * <p>正規化（トリム）を行ってからCommandを生成。</p>
     * 
     * @return ChangePasswordCommand
     */
    public ChangePasswordCommand toCommand() {
        return new ChangePasswordCommand(
            currentPassword == null ? null : currentPassword.trim(),
            newPassword == null ? null : newPassword.trim()
        );
    }
    
    // Getters
    public String getCurrentPassword() { return currentPassword; }
    public String getNewPassword() { return newPassword; }
    
    // Setters（Jackson用）
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
