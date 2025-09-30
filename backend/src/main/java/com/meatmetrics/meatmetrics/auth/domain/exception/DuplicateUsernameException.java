package com.meatmetrics.meatmetrics.auth.domain.exception;

import com.meatmetrics.meatmetrics.sharedkernel.domain.common.exception.DomainException;

/**
 * 重複ユーザー名例外
 * システム内で既に使用されているユーザー名での登録時に発生。
 */
public class DuplicateUsernameException extends DomainException {
    
    public DuplicateUsernameException(String username) {
        super("Username already exists: " + username);
    }
}
