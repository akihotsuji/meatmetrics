package com.meatmetrics.meatmetrics.auth.service;

import org.springframework.stereotype.Service;

import com.meatmetrics.meatmetrics.config.JwtProperties;
import com.meatmetrics.meatmetrics.domain.user.aggregate.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWTトークンサービス
 * 
 * <p>アクセストークンとリフレッシュトークンの生成・検証を担当します。
 * セキュリティ上の理由から、トークンの有効期限や署名キーの管理も行います。</p>
 * 
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
@Service
public class JwtTokenService {
    
    private final JwtProperties jwtProperties;
    
    /**
     * コンストラクタ
     * 
     * @param jwtProperties JWT設定プロパティ
     */
    public JwtTokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }
    
    /**
     * アクセストークン生成
     * 
     * <p>短い有効期限（1時間）のJWTトークンを生成します。
     * API呼び出し時の認証に使用されます。</p>
     * 
     * @param user トークンを発行するユーザー
     * @return 生成されたアクセストークン
     */
    public String generateAccessToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getAccessToken().getExpirationMs());
        
        return Jwts.builder()
                .subject(user.getId().toString())              // JWTの主体（ユーザーID）を設定
                .claim("email", user.getEmail().getValue())    // カスタムクレーム：メールアドレス
                .claim("username", user.getUsername().getValue()) // カスタムクレーム：ユーザー名
                .id(java.util.UUID.randomUUID().toString())     // 一意識別子を付与して同一時刻発行でも差異を保証
                .issuedAt(now)                                 // 発行日時（iat）
                .expiration(expiryDate)                        // 有効期限（exp）
                .signWith(getSigningKey())                     // HMAC-SHA256で署名
                .compact();                                    // JWT文字列として出力
    }
    
    /**
     * リフレッシュトークン生成
     * 
     * <p>長い有効期限（7日）のJWTトークンを生成します。
     * アクセストークンの再発行に使用されます。</p>
     * 
     * @param user トークンを発行するユーザー
     * @return 生成されたリフレッシュトークン
     */
    public String generateRefreshToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtProperties.getRefreshToken().getExpirationMs());
        
        return Jwts.builder()
                .subject(user.getId().toString())              // JWTの主体（ユーザーID）を設定
                .issuedAt(now)                                 // 発行日時（iat）
                .expiration(expiryDate)                        // 有効期限（exp：7日間）
                .signWith(getSigningKey())                     // HMAC-SHA256で署名
                .compact();                                    // JWT文字列として出力
    }
    
    /**
     * アクセストークン有効期限（秒）を取得
     * 
     * @return 有効期限（秒）
     */
    public Long getAccessTokenExpirationSeconds() {
        return jwtProperties.getAccessToken().getExpirationSeconds();
    }
    
    /**
     * トークン検証
     * 
     * <p>指定されたトークンの有効性を検証します。
     * 署名の確認、有効期限のチェックを行います。</p>
     * 
     * @param token 検証対象のトークン
     * @return トークンが有効な場合true
     */
    public boolean validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())              // 署名検証用キーを設定
                    .build()                                  // パーサーを構築
                    .parseSignedClaims(token);                // トークン解析（署名検証・期限切れ自動チェック）
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            // JWT例外: 署名不正、有効期限切れ、フォーマット不正など
            return false;
        }
    }
    
    /**
     * トークンからユーザーIDを抽出
     * 
     * @param token JWTトークン
     * @return ユーザーID
     * @throws IllegalArgumentException トークンが無効な場合
     */
    public Long extractUserId(String token) {
        if (!validateToken(token)) {                              // 事前にトークンの有効性をチェック
            throw new IllegalArgumentException("Invalid token");
        }
        
        try {
            Jws<Claims> claimsJws = Jwts.parser()
                    .verifyWith(getSigningKey())                   // 署名検証用キーを設定
                    .build()                                       // パーサーを構築
                    .parseSignedClaims(token);                     // トークンを解析してクレームを取得
            
            String subject = claimsJws.getPayload().getSubject();  // subjectクレーム（ユーザーID）を取得
            return Long.parseLong(subject);                        // 文字列のユーザーIDをLong型に変換
        } catch (JwtException | NumberFormatException e) {
            throw new IllegalArgumentException("Invalid token format", e);
        }
    }
    
    /**
     * JWT署名用のキーを取得
     * 
     * @return 署名キー
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecretKey().getBytes(StandardCharsets.UTF_8));
    }
}
