package com.meatmetrics.meatmetrics.auth.command;

import com.meatmetrics.meatmetrics.domain.user.valueobject.Email;
import com.meatmetrics.meatmetrics.domain.user.valueobject.Username;
import jakarta.validation.constraints.*;

/**
 * ユーザー登録のためのコマンドオブジェクト
 * 
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
public class RegisterUserCommand {
    
    /** メールアドレス（必須、有効なメール形式、255文字以内） */
    @NotNull(message = "メールアドレスは必須です")
    @jakarta.validation.constraints.Email(message = "有効なメールアドレスを入力してください")
    @Size(max = 255, message = "メールアドレスは255文字以内で入力してください")
    private String email;
    
    /** パスワード（必須、8文字以上100文字以内） */
    @NotNull(message = "パスワードは必須です")
    @Size(min = 8, max = 100, message = "パスワードは8文字以上100文字以内で入力してください")
    private String password;
    
    /** ユーザー名（必須、3文字以上50文字以内、英数字・_・-のみ） */
    @NotNull(message = "ユーザー名は必須です")
    @Size(min = 3, max = 50, message = "ユーザー名は3文字以上50文字以内で入力してください")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "ユーザー名には英数字、アンダースコア、ハイフンのみ使用できます")
    private String username;
    
    /** デフォルトコンストラクタ */
    public RegisterUserCommand() {}
    
    /**
     * 全項目指定コンストラクタ
     * 
     * @param email メールアドレス
     * @param password パスワード
     * @param username ユーザー名
     */
    public RegisterUserCommand(String email, String password, String username) {
        this.email = email;
        this.password = password;
        this.username = username;
    }
    
    /** @return メールアドレス */
    public String getEmail() {
        return email;
    }
    
    /** @return パスワード */
    public String getPassword() {
        return password;
    }
    
    /** @return ユーザー名 */
    public String getUsername() {
        return username;
    }
    
    /** @param email メールアドレス */
    public void setEmail(String email) {
        this.email = email;
    }
    
    /** @param password パスワード */
    public void setPassword(String password) {
        this.password = password;
    }
    
    /** @param username ユーザー名 */
    public void setUsername(String username) {
        this.username = username;
    }
    
    /**
     * ドメインのEmail値オブジェクトに変換します
     * 
     * @return Email値オブジェクト
     */
    public Email toEmail() {
        return new Email(this.email);
    }
    
    /**
     * ドメインのUsername値オブジェクトに変換します
     * 
     * @return Username値オブジェクト
     */
    public Username toUsername() {
        return new Username(this.username);
    }
}
