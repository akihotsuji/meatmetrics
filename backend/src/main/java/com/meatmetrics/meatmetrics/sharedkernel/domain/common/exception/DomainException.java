package com.meatmetrics.meatmetrics.sharedkernel.domain.common.exception;

/**
 * ドメイン例外の基底クラス
 * ビジネスルール違反や不変条件違反を表現する。
 */
public abstract class DomainException extends RuntimeException {
    
    public DomainException(String message) {
        super(message);
    }
    
    public DomainException(String message, Throwable cause) {
        super(message, cause);
    }
}
