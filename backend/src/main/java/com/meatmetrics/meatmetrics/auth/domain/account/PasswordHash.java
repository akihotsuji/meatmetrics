package com.meatmetrics.meatmetrics.auth.domain.account;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.meatmetrics.meatmetrics.auth.domain.exception.WeakPasswordException;

import java.util.Objects;
import java.util.regex.Pattern;

/**
 * PasswordHash値オブジェクト
 * パスワードのハッシュ化と強度チェックを提供する。
 * 平文パスワードは内部で保持せず、ハッシュ化された値のみを持つ。
 */
public class PasswordHash {
    
    // パスワード強度要件
    private static final int MIN_LENGTH = 8;
    private static final Pattern CONTAINS_LETTER = Pattern.compile(".*[a-zA-Z].*");
    private static final Pattern CONTAINS_DIGIT = Pattern.compile(".*\\d.*");
    
    private static final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    private String hashedValue;
    
    /**
     * 平文パスワードからPasswordHashオブジェクトを生成する
     * 
     * @param plainPassword 平文パスワード
     * @throws WeakPasswordException パスワードが強度要件を満たさない場合
     */
    public PasswordHash(String plainPassword) {
        if (!isStrongEnough(plainPassword)) {
            throw new WeakPasswordException();
        }
        
        this.hashedValue = passwordEncoder.encode(plainPassword);
    }
    
    /**
     * 既にハッシュ化されたパスワードからPasswordHashオブジェクトを生成する
     * （データベースからの復元用）
     * 
     * @param hashedPassword ハッシュ化済みパスワード
     * @param isFromDatabase データベースからの復元かどうかのフラグ
     */
    public static PasswordHash fromHash(String hashedPassword) {
        if (hashedPassword == null || hashedPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Hashed password cannot be null or empty");
        }
        
        PasswordHash passwordHash = new PasswordHash();
        passwordHash.hashedValue = hashedPassword;
        return passwordHash;
    }
    
    /**
     * プライベートコンストラクタ（fromHashメソッド用）
     */
    private PasswordHash() {
        this.hashedValue = null; // fromHashで設定される
    }
    
    /**
     * パスワードが強度要件を満たすかチェックする
     * 
     * @param password チェック対象のパスワード
     * @return 強度要件を満たす場合true
     */
    public static boolean isStrongEnough(String password) {
        if (password == null || password.length() < MIN_LENGTH) {
            return false;
        }
        
        // 英文字を含む
        if (!CONTAINS_LETTER.matcher(password).matches()) {
            return false;
        }
        
        // 数字を含む
        if (!CONTAINS_DIGIT.matcher(password).matches()) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 指定されたパスワードがこのハッシュと一致するかチェックする
     * 
     * @param plainPassword チェック対象の平文パスワード
     * @return 一致する場合true
     */
    public boolean matches(String plainPassword) {
        if (plainPassword == null) {
            return false;
        }
        return passwordEncoder.matches(plainPassword, hashedValue);
    }
    
    /**
     * ハッシュ化されたパスワード値を取得する
     * 
     * @return ハッシュ化されたパスワード文字列
     */
    public String getValue() {
        return hashedValue;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PasswordHash that = (PasswordHash) o;
        return Objects.equals(hashedValue, that.hashedValue);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(hashedValue);
    }
    
    @Override
    public String toString() {
        return "[PROTECTED]"; // セキュリティのためハッシュ値は出力しない
    }
}
