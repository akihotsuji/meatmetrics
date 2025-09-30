package com.meatmetrics.meatmetrics.auth.application.command;

import com.meatmetrics.meatmetrics.sharedkernel.domain.common.Email;

/**
 * ユーザーログインのためのコマンドオブジェクト
 * 
 * <p>Request層でバリデーション済みのデータを受け取る、型安全な引数パック。</p>
 * <p>ビジネスロジック実行のために必要最小限の情報を保持する。</p>
 * 
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
public class LoginCommand {
    /** メールアドレス（バリデーション済み） */
    private String email;

    /** パスワード（バリデーション済み） */
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
