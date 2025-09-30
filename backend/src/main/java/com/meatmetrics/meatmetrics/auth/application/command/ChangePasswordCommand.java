package com.meatmetrics.meatmetrics.auth.application.command;


/**
 * パスワード変更のためのコマンドクラス
 * 
 * <p>Request層でバリデーション済みのデータを受け取る、型安全な引数パック。</p>
 * <p>ビジネスロジック実行のために必要最小限の情報を保持する。</p>
 * 
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
public class ChangePasswordCommand {
    /** 現在のパスワード（バリデーション済み） */
    private String currentPassword;
    
    /** 新しいパスワード（バリデーション済み） */
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
