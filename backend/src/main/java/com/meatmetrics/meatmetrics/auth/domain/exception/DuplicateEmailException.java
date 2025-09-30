package com.meatmetrics.meatmetrics.auth.domain.exception;

import com.meatmetrics.meatmetrics.sharedkernel.domain.common.exception.DomainException;

/**
 * 重複メールアドレス例外
 * システム内で既に使用されているメールアドレスでの登録時に発生。
 */
public class DuplicateEmailException extends DomainException {
    
    public DuplicateEmailException(String email) {
        super("Email already exists: " + email);
    }
}
