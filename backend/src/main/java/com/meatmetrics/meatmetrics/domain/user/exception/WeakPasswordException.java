package com.meatmetrics.meatmetrics.domain.user.exception;

/**
 * 脆弱なパスワード例外
 * パスワード強度要件を満たさない場合に発生。
 */
public class WeakPasswordException extends DomainException {
    
    public WeakPasswordException() {
        super("Password does not meet strength requirements: minimum 8 characters with letters and digits");
    }
    
    public WeakPasswordException(String message) {
        super(message);
    }
}
