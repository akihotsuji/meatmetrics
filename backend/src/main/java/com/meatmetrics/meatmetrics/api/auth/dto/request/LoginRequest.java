package com.meatmetrics.meatmetrics.api.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.meatmetrics.meatmetrics.auth.command.LoginCommand;

/**
 * ログインリクエストDTO
 * 
 * <p>POST /api/auth/login のリクエストボディを受け取るためのDTO。</p>
 * <p>外部入力の完全バリデーション（HTTP制約・ビジネスルール）を担当し、
 * 検証済みデータをLoginCommandに変換してサービス層に渡す。</p>
 * 
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
public class LoginRequest {
    
    /** メールアドレス（必須、有効なメール形式） */
    @NotNull(message = "メールアドレスは必須です")
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "有効なメールアドレスを入力してください")
    @Size(max = 255, message = "メールアドレスは255文字以内で入力してください")
    private String email;
    
    /** パスワード（必須、空白不可） */
    @NotNull(message = "パスワードは必須です")
    @NotBlank(message = "パスワードは空白のみでは入力できません")
    @Size(max = 100, message = "パスワードは100文字以内で入力してください")
    private String password;
    
    /** デフォルトコンストラクタ（Jackson用） */
    public LoginRequest() {}
    
    /**
     * 全項目指定コンストラクタ
     * 
     * @param email メールアドレス
     * @param password パスワード
     */
    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }
    
    /**
     * LoginCommandに変換
     * 
     * <p>正規化（トリム、メール小文字化）を行ってからCommandを生成。</p>
     * 
     * @return LoginCommand
     */
    public LoginCommand toCommand() {
        String normalizedEmail = email == null ? null : email.trim().toLowerCase();
        return new LoginCommand(normalizedEmail, password);
    }
    
    // Getters
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    
    // Setters（Jackson用）
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
}
