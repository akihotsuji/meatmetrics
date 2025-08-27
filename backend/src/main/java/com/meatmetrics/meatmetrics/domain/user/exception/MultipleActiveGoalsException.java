package com.meatmetrics.meatmetrics.domain.user.exception;

/**
 * 複数アクティブ目標例外
 * 1ユーザーに対して複数のアクティブな目標が存在する場合に発生。
 */
public class MultipleActiveGoalsException extends DomainException {
    
    public MultipleActiveGoalsException(Long userId) {
        super("User has multiple active goals: " + userId);
    }
}
