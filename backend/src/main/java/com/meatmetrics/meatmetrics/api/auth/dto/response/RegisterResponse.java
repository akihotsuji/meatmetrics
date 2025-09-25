package com.meatmetrics.meatmetrics.api.auth.dto.response;

import java.time.LocalDateTime;
import com.meatmetrics.meatmetrics.domain.user.aggregate.User;

/**
 * ユーザー登録結果DTO
 * 
 * <p>ユーザー登録成功時のAPIレスポンス用データ転送オブジェクトです。
 * セキュリティ上の理由からパスワードハッシュや内部情報は含まず、
 * 必要最小限の情報のみを公開します。</p>
 * 
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
public class RegisterResponse {
    
    /** ユーザーID */
    private Long userId;
    
    /** メールアドレス */
    private String email;
    
    /** ユーザー名 */
    private String username;
    
    /** 登録日時 */
    private LocalDateTime createdAt;
    
    /** デフォルトコンストラクタ（Jackson用） */
    public RegisterResponse() {}
    
    /**
     * 全項目指定コンストラクタ
     * 
     * @param userId ユーザーID
     * @param email メールアドレス
     * @param username ユーザー名
     * @param createdAt 登録日時
     */
    public RegisterResponse(Long userId, String email, String username, LocalDateTime createdAt) {
        this.userId = userId;
        this.email = email;
        this.username = username;
        this.createdAt = createdAt;
    }
    
    /**
     * UserドメインオブジェクトからDTOを生成するファクトリメソッド
     * 
     * @param user Userドメインオブジェクト
     * @return RegisterResponse DTO
     */
    public static RegisterResponse from(User user) {
        return new RegisterResponse(
            user.getId(),
            user.getEmail().getValue(),
            user.getUsername().getValue(),
            user.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
        );
    }
    
    // Getters
    public Long getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getUsername() { return username; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    
    // Setters（Jackson用）
    public void setUserId(Long userId) { this.userId = userId; }
    public void setEmail(String email) { this.email = email; }
    public void setUsername(String username) { this.username = username; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
