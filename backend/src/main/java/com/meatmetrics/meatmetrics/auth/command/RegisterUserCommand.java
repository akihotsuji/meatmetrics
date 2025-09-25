package com.meatmetrics.meatmetrics.auth.command;

import com.meatmetrics.meatmetrics.domain.user.valueobject.Email;
import com.meatmetrics.meatmetrics.domain.user.valueobject.Username;

/**
 * ユーザー登録のためのコマンドオブジェクト
 * 
 * <p>Request層でバリデーション済みのデータを受け取る、型安全な引数パック。</p>
 * <p>ビジネスロジック実行のために必要最小限の情報を保持する。</p>
 * 
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
public class RegisterUserCommand {
    
    /** メールアドレス（バリデーション済み） */
    private String email;
    
    /** パスワード（バリデーション済み） */
    private String password;
    
    /** ユーザー名（バリデーション済み） */
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
