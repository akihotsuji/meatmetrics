package com.meatmetrics.meatmetrics.domain.user.valueobject;

import com.meatmetrics.meatmetrics.domain.user.exception.InvalidUsernameException;

import java.util.Objects;

/**
 * Username値オブジェクト
 * 表示用ユーザー名として使用される。
 * 長さ制約と文字種制約を持つ。
 */
public class Username {
    
    private static final int MIN_LENGTH = 3;
    private static final int MAX_LENGTH = 30;
    
    private final String value;
    
    /**
     * Usernameオブジェクトを生成する
     * 
     * @param username ユーザー名文字列
     * @throws InvalidUsernameException 無効なユーザー名の場合
     */
    public Username(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new InvalidUsernameException("Username cannot be null or empty");
        }
        
        String trimmedUsername = username.trim();
        
        if (!isValid(trimmedUsername)) {
            throw new InvalidUsernameException("Invalid username: " + trimmedUsername);
        }
        
        this.value = trimmedUsername;
    }
    
    /**
     * ユーザー名が有効かチェックする
     * 
     * @param username チェック対象のユーザー名
     * @return 有効な場合true
     */
    public static boolean isValid(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        String trimmed = username.trim();
        
        // 長さチェック
        if (trimmed.length() < MIN_LENGTH || trimmed.length() > MAX_LENGTH) {
            return false;
        }
        
        // 文字種チェック（英数字、アンダースコア、ハイフンのみ許可）
        return trimmed.matches("^[a-zA-Z0-9_-]+$");
    }
    
    /**
     * ユーザー名値を取得する
     * 
     * @return ユーザー名文字列
     */
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Username username = (Username) o;
        return Objects.equals(value, username.value);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(value);
    }
    
    @Override
    public String toString() {
        return value;
    }
}
