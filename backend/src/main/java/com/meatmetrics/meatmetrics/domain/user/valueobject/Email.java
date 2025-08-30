package com.meatmetrics.meatmetrics.domain.user.valueobject;

import com.meatmetrics.meatmetrics.domain.user.exception.InvalidEmailException;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Email値オブジェクト
 * ログイン用識別子として使用される。
 * フォーマットバリデーションと等価性を提供。
 */
public class Email {
    
    // RFC 5322準拠の基本的なメールアドレス形式パターン
    // より厳密にバリデーションを行う
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9._%+-]+@[A-Za-z0-9]([A-Za-z0-9.-]*[A-Za-z0-9])?\\.[A-Za-z]{2,}$"
    );
    
    private final String value;
    
    /**
     * Emailオブジェクトを生成する
     * 
     * @param email メールアドレス文字列
     * @throws InvalidEmailException 無効なメールアドレス形式の場合
     */
    public Email(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new InvalidEmailException("Email cannot be null or empty");
        }
        
        String trimmedEmail = email.trim();
        if (!isValid(trimmedEmail)) {
            throw new InvalidEmailException("Invalid email format: " + trimmedEmail);
        }
        
        if (trimmedEmail.length() > 255) {
            throw new InvalidEmailException("Email is too long. Maximum length is 255 characters");
        }
        
        this.value = trimmedEmail.toLowerCase(); // 正規化のため小文字に変換
    }
    
    /**
     * メールアドレスの形式が有効かチェックする
     * 
     * @param email チェック対象のメールアドレス
     * @return 有効な場合true
     */
    public static boolean isValid(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }
    
    /**
     * メールアドレス値を取得する
     * 
     * @return メールアドレス文字列
     */
    public String getValue() {
        return value;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Email email = (Email) o;
        return Objects.equals(value, email.value);
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
