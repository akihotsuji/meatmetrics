package com.meatmetrics.meatmetrics.user.domain.exception;

import com.meatmetrics.meatmetrics.sharedkernel.domain.common.exception.DomainException;

/**
 * 無効な目標値例外
 * 栄養目標値が許容範囲外の場合に発生。
 */
public class InvalidGoalValueException extends DomainException {
    
    public InvalidGoalValueException(String field, Object value) {
        super("Invalid goal value for " + field + ": " + value);
    }
    
    public InvalidGoalValueException(String message) {
        super(message);
    }
}
