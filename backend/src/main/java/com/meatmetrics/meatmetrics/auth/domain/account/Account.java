package com.meatmetrics.meatmetrics.auth.domain.account;

import com.meatmetrics.meatmetrics.auth.domain.exception.DuplicateEmailException;
import com.meatmetrics.meatmetrics.auth.domain.exception.DuplicateUsernameException;
import com.meatmetrics.meatmetrics.sharedkernel.domain.common.Email;
import com.meatmetrics.meatmetrics.sharedkernel.domain.common.Username;

import java.time.Instant;
import java.util.Objects;

/**
 * Account集約ルート
 * 認証情報（メール、アカウント名、パスワード）を管理する。
 * ログイン認証、パスワード変更、重複チェックを担当する。
 */
public class Account {
    
    private final Long id;
    private final Email email;
    private Username username;
    private PasswordHash passwordHash;
    private final Instant createdAt;
    private Instant updatedAt;
    
    /**
     * 新規Accountを作成する（IDなし）
     */
    public Account(Email email, Username username, PasswordHash passwordHash) {
        this(null, email, username, passwordHash, Instant.now(), Instant.now());
    }
    
    /**
     * 既存Accountを復元する（IDあり）
     */
    public Account(Long id, Email email, Username username, PasswordHash passwordHash,
               Instant createdAt, Instant updatedAt) {
        
        // 不変条件チェック
        validateEmail(email);
        validateUsername(username);
        validatePasswordHash(passwordHash);
        
        this.id = id;
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }
    
    /**
     * ファクトリメソッド：アカウント登録
     */
    public static Account register(Email email, Username username, PasswordHash passwordHash) {
        return new Account(email, username, passwordHash);
    }
    
    /**
     * ログイン認証
     */
    public boolean login(String plainPassword) {
        return passwordHash.matches(plainPassword);
    }
    
    /**
     * パスワード変更
     */
    public void changePassword(String oldPassword, PasswordHash newPasswordHash) {
        if (!passwordHash.matches(oldPassword)) {
            throw new IllegalArgumentException("Current password is incorrect");
        }
        
        this.passwordHash = newPasswordHash;
        this.updatedAt = Instant.now();
    }
    
    
    /**
     * 不変条件：Email重複禁止の検証
     * 実際の重複チェックはリポジトリ層で行われる
     */
    public void validateEmailUniqueness(boolean emailExists) {
        if (emailExists) {
            throw new DuplicateEmailException(email.getValue());
        }
    }
    
    /**
     * 不変条件：Username重複禁止の検証
     * 実際の重複チェックはリポジトリ層で行われる
     */
    public void validateUsernameUniqueness(boolean usernameExists) {
        if (usernameExists) {
            throw new DuplicateUsernameException(username.getValue());
        }
    }
    
    private void validateEmail(Email email) {
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }
    }
    
    private void validateUsername(Username username) {
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }
    }
    
    private void validatePasswordHash(PasswordHash passwordHash) {
        if (passwordHash == null) {
            throw new IllegalArgumentException("PasswordHash cannot be null");
        }
    }
    
    
    // Getters
    public Long getId() { return id; }
    public Email getEmail() { return email; }
    public Username getUsername() { return username; }
    public PasswordHash getPasswordHash() { return passwordHash; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Account account = (Account) o;
        
        // IDが両方nullでない場合はIDで比較
        if (id != null && account.id != null) {
            return Objects.equals(id, account.id);
        }
        
        // IDがない場合はemailで比較（emailは一意のため）
        return Objects.equals(email, account.email);
    }
    
    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(email);
    }
    
    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", email=" + email +
                ", username=" + username +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
