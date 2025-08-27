package com.meatmetrics.meatmetrics.domain.user.exception;

/**
 * 無効なユーザー名例外
 * 長さ制約違反、文字種違反等の場合に発生。
 */
public class InvalidUsernameException extends DomainException {
    
    public InvalidUsernameException(String message) {
        super(message);
    }
}
