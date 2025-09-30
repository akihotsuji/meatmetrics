package com.meatmetrics.meatmetrics.api.auth.dto.response;

import com.meatmetrics.meatmetrics.auth.domain.account.Account;

/**
 * トークン更新結果DTO
 * 
 * <p>トークン更新成功時のAPIレスポンス用データ転送オブジェクトです。
 * 新しいJWTトークン情報とトークンメタデータを含みます。</p>
 * 
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
public class RefreshResponse {
    
    /** アクセストークン（JWT） */
    private String accessToken;
    
    /** トークンタイプ（固定値：Bearer） */
    private String tokenType = "Bearer";
    
    /** トークン有効期限（秒） */
    private Long expiresIn;
    
    /** リフレッシュトークン（トークン再発行用） */
    private String refreshToken;
    
    /** デフォルトコンストラクタ（Jackson用） */
    public RefreshResponse() {}
    
    /**
     * 全項目指定コンストラクタ
     * 
     * @param accessToken アクセストークン
     * @param expiresIn トークン有効期限（秒）
     * @param refreshToken リフレッシュトークン
     */
    public RefreshResponse(String accessToken, Long expiresIn, String refreshToken){
        this.accessToken = accessToken;
        this.expiresIn = expiresIn;
        this.refreshToken = refreshToken;
    }
    
    /**
     * トークン更新情報からDTOを生成するファクトリメソッド
     * 
     * @param user 認証されたユーザー
     * @param accessToken 生成されたアクセストークン（JWT）
     * @param refreshToken 生成されたリフレッシュトークン
     * @param expirationSeconds アクセストークン有効期限（秒）
     * @return RefreshResponse DTO
     */
    public static RefreshResponse from(Account account, String accessToken, String refreshToken, Long expirationSeconds) {
        return new RefreshResponse(
            accessToken,
            expirationSeconds,
            refreshToken
        );
    }
    
    // Getters
    public String getAccessToken() { return accessToken; }
    public String getTokenType() { return tokenType; }
    public Long getExpiresIn() { return expiresIn; }
    public String getRefreshToken() { return refreshToken; }
    
    // Setters（Jackson用）
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
}
