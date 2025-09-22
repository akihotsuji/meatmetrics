package com.meatmetrics.meatmetrics.auth.command;

import com.meatmetrics.meatmetrics.domain.user.valueobject.Email;

import jakarta.validation.constraints.*;

/**
 * ユーザーログインのためのコマンドオブジェクト
 * 
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
public class LoginCommand {
    /** メールアドレス（必須、有効なメール形式） */
    @NotNull(message = "メールアドレスは必須です")
    @jakarta.validation.constraints.Email(message = "有効なメールアドレスを入力してください")
    private String email;

    /** パスワード（必須、空白不可） */
    @NotNull(message = "パスワードは必須です")
    @NotBlank(message = "パスワードは空白のみでは入力できません")
    private String password;

    /** デフォルトコンストラクタ */
    public LoginCommand() {}

    /** 
     * 全項目指定コンストラクタ
     * 
     * @param email メールアドレス
     * @param password パスワード
     */
    public LoginCommand(String email, String password){
        this.email = email;
        this.password = password;
    }

    /**
     * ドメインのEmail値オブジェクトに変換します
     * 
     * @return Email値オブジェクト
     */
    public Email toEmail() {
        return new Email(this.email);
    }

    /** @return メールアドレス */
    public String getEmail() { return email; }
    
    /** @return パスワード */
    public String getPassword() { return password; }
    
    /** @param email メールアドレス */
    void setEmail(String email) { this.email = email; }
    
    /** @param password パスワード */
    void setPassword(String password) { this.password = password; }
}
