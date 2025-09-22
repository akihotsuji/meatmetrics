package com.meatmetrics.meatmetrics.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.KeyGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * JWT設定プロパティ
 * 
 * <p>JWT関連の設定値を一元管理します。
 * application.propertiesまたは環境変数から値を読み込みます。</p>
 * 
 * <h3>設定例:</h3>
 * <pre>
 * # application.properties
 * jwt.secret-key=your-super-secret-key-here-must-be-at-least-256-bits
 * jwt.access-token.expiration-ms=3600000
 * jwt.refresh-token.expiration-ms=604800000
 * </pre>
 * 
 * <h3>環境変数例:</h3>
 * <pre>
 * JWT_SECRET_KEY=your-super-secret-key-here-must-be-at-least-256-bits
 * JWT_ACCESS_TOKEN_EXPIRATION_MS=3600000
 * JWT_REFRESH_TOKEN_EXPIRATION_MS=604800000
 * </pre>
 * 
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
@Component
@ConfigurationProperties(prefix = "jwt")
public class JwtProperties {
    
    /**
     * JWT署名用の秘密鍵
     * 最低256ビット（32文字）以上必要
     * 本番環境では必ず環境変数で設定すること
     */
    private String secretKey;
    
    /**
     * アクセストークン設定
     */
    private final AccessToken accessToken = new AccessToken();
    
    /**
     * リフレッシュトークン設定
     */
    private final RefreshToken refreshToken = new RefreshToken();
    
    // Getters and Setters
    public String getSecretKey() { 
        // 秘密鍵が設定されていない場合は動的生成（開発環境用）
        if (!StringUtils.hasText(secretKey)) {
            secretKey = generateRandomSecretKey();
            System.out.println("⚠️  Warning: JWT秘密鍵が設定されていません。動的生成された一時的なキーを使用します。");
            System.out.println("⚠️  本番環境では必ず環境変数 JWT_SECRET_KEY を設定してください。");
        }
        return secretKey; 
    }
    
    public void setSecretKey(String secretKey) { 
        this.secretKey = secretKey; 
    }
    
    /**
     * ランダムな秘密鍵を生成（開発環境用）
     * 
     * @return Base64エンコードされた256ビット秘密鍵
     */
    private String generateRandomSecretKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacSHA256");
            keyGenerator.init(256, new SecureRandom());
            byte[] secretKeyBytes = keyGenerator.generateKey().getEncoded();
            return Base64.getEncoder().encodeToString(secretKeyBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate JWT secret key", e);
        }
    }
    
    public AccessToken getAccessToken() { return accessToken; }
    public RefreshToken getRefreshToken() { return refreshToken; }
    
    /**
     * アクセストークン設定クラス
     */
    public static class AccessToken {
        /**
         * アクセストークン有効期限（ミリ秒）
         * デフォルト: 1時間
         */
        private long expirationMs = 3600 * 1000; // 1時間
        
        public long getExpirationMs() { return expirationMs; }
        public void setExpirationMs(long expirationMs) { this.expirationMs = expirationMs; }
        
        /**
         * 有効期限を秒で取得
         */
        public long getExpirationSeconds() { return expirationMs / 1000; }
    }
    
    /**
     * リフレッシュトークン設定クラス
     */
    public static class RefreshToken {
        /**
         * リフレッシュトークン有効期限（ミリ秒）
         * デフォルト: 7日
         */
        private long expirationMs = 7 * 24 * 3600 * 1000; // 7日
        
        public long getExpirationMs() { return expirationMs; }
        public void setExpirationMs(long expirationMs) { this.expirationMs = expirationMs; }
        
        /**
         * 有効期限を秒で取得
         */
        public long getExpirationSeconds() { return expirationMs / 1000; }
    }
}
