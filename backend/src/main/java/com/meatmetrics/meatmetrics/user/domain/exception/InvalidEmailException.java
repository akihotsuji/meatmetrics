package com.meatmetrics.meatmetrics.user.domain.exception;

import com.meatmetrics.meatmetrics.sharedkernel.domain.common.exception.DomainException;

/**
 * 無効なメールアドレス例外
 * フォーマット不正、長さ違反等の場合に発生。
 */
public class InvalidEmailException extends DomainException {
    
    public InvalidEmailException(String message) {
        super(message);
    }
}
