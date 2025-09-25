package com.meatmetrics.meatmetrics.api.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

import com.meatmetrics.meatmetrics.auth.command.RefreshCommand;

/**
 * トークン更新リクエストDTO
 * 
 * <p>POST /api/auth/refresh のリクエストボディを受け取るためのDTO。</p>
 * <p>リフレッシュトークンを受け取り、新しいアクセストークンを発行する。</p>
 * 
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
public class RefreshRequest {
    
    /** リフレッシュトークン（必須） */
    @NotBlank(message = "リフレッシュトークンは必須です")
    private String refreshToken;
    
    /** デフォルトコンストラクタ（Jackson用） */
    public RefreshRequest() {}
    
    /**
     * 全項目指定コンストラクタ
     * 
     * @param refreshToken リフレッシュトークン
     */
    public RefreshRequest(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    /**
     * リフレッシュトークンを取得（正規化済み）
     * 
     * @return 正規化されたリフレッシュトークン
     */
    public String getRefreshToken() {
        return refreshToken == null ? null : refreshToken.trim();
    }
    
    /** DTOをアプリケーション層のコマンドに変換 */
    public RefreshCommand toCommand() {
        return new RefreshCommand(getRefreshToken());
    }
    
    // Setter（Jackson用）
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
