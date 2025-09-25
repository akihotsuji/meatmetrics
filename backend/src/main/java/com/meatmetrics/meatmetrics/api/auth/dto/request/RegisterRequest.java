package com.meatmetrics.meatmetrics.api.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.meatmetrics.meatmetrics.auth.command.RegisterUserCommand;

/**
 * ユーザー登録リクエストDTO
 * 
 * <p>POST /api/auth/register のリクエストボディを受け取るためのDTO。</p>
 * <p>外部入力の完全バリデーション（HTTP制約・ビジネスルール）を担当し、
 * 検証済みデータをRegisterUserCommandに変換してサービス層に渡す。</p>
 * 
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
public class RegisterRequest {
    
    /** メールアドレス（必須、有効なメール形式、255文字以内） */
    @NotNull(message = "メールアドレスは必須です")
    @NotBlank(message = "メールアドレスは必須です")
    @Email(message = "有効なメールアドレスを入力してください")
    @Size(max = 255, message = "メールアドレスは255文字以内で入力してください")
    private String email;
    
    /** パスワード（必須、8文字以上100文字以内） */
    @NotNull(message = "パスワードは必須です")
    @NotBlank(message = "パスワードは必須です")
    @Size(min = 8, max = 100, message = "パスワードは8文字以上100文字以内で入力してください")
    private String password;
    
    /** ユーザー名（必須、3文字以上50文字以内、英数字・_・-のみ） */
    @NotNull(message = "ユーザー名は必須です")
    @NotBlank(message = "ユーザー名は必須です")
    @Size(min = 3, max = 50, message = "ユーザー名は3文字以上50文字以内で入力してください")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "ユーザー名には英数字、アンダースコア、ハイフンのみ使用できます")
    private String username;
    
    /** デフォルトコンストラクタ（Jackson用） */
    public RegisterRequest() {}
    
    /**
     * 全項目指定コンストラクタ
     * 
     * @param email メールアドレス
     * @param password パスワード
     * @param username ユーザー名
     */
    public RegisterRequest(String email, String password, String username) {
        this.email = email;
        this.password = password;
        this.username = username;
    }
    
    /**
     * RegisterUserCommandに変換
     * 
     * <p>正規化（トリム、メール小文字化）を行ってからCommandを生成。</p>
     * 
     * @return RegisterUserCommand
     */
    public RegisterUserCommand toCommand() {
        String normalizedEmail = email == null ? null : email.trim().toLowerCase();
        String normalizedUsername = username == null ? null : username.trim();
        return new RegisterUserCommand(normalizedEmail, password, normalizedUsername);
    }
    
    // Getters
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getUsername() { return username; }
    
    // Setters（Jackson用）
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setUsername(String username) { this.username = username; }
}
