package com.meatmetrics.meatmetrics.domain.user.aggregate;

import com.meatmetrics.meatmetrics.domain.user.valueobject.Email;
import com.meatmetrics.meatmetrics.domain.user.valueobject.Username;
import com.meatmetrics.meatmetrics.domain.user.valueobject.PasswordHash;
import com.meatmetrics.meatmetrics.domain.user.exception.DuplicateEmailException;
import com.meatmetrics.meatmetrics.domain.user.exception.DuplicateUsernameException;
import com.meatmetrics.meatmetrics.domain.user.exception.MultipleActiveGoalsException;

import java.time.Instant;
import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * User集約ルート
 * ユーザーの認証情報とプロフィール情報を管理する。
 * UserGoalの生成・管理も担当し、集約の不変条件を維持する。
 */
public class User {
    
    private final Long id;
    private final Email email;
    private Username username;
    private PasswordHash passwordHash;
    private final List<UserGoal> goals;
    private final Instant createdAt;
    private Instant updatedAt;
    
    /**
     * 新規Userを作成する（IDなし）
     */
    public User(Email email, Username username, PasswordHash passwordHash) {
        this(null, email, username, passwordHash, new ArrayList<>(), Instant.now(), Instant.now());
    }
    
    /**
     * 既存Userを復元する（IDあり）
     */
    public User(Long id, Email email, Username username, PasswordHash passwordHash,
               List<UserGoal> goals, Instant createdAt, Instant updatedAt) {
        
        // 不変条件チェック
        validateEmail(email);
        validateUsername(username);
        validatePasswordHash(passwordHash);
        validateGoals(goals);
        
        this.id = id;
        this.email = email;
        this.username = username;
        this.passwordHash = passwordHash;
        this.goals = goals != null ? new ArrayList<>(goals) : new ArrayList<>();
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }
    
    /**
     * ファクトリメソッド：ユーザー登録
     */
    public static User register(Email email, Username username, PasswordHash passwordHash) {
        return new User(email, username, passwordHash);
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
     * プロフィール更新
     */
    public void updateProfile(Username newUsername) {
        validateUsername(newUsername);
        this.username = newUsername;
        this.updatedAt = Instant.now();
    }
    
    /**
     * 新しい目標を設定
     * 既存のアクティブ目標は自動的に非アクティブになる
     */
    public UserGoal setGoal(Integer dailyCalorieGoal, BigDecimal proteinGoalG,
                           BigDecimal fatGoalG, BigDecimal netCarbsGoalG, LocalDate effectiveDate) {
        
        // 既存のアクティブ目標を非アクティブにする
        deactivateAllGoals();
        
        // 新しい目標を作成してアクティブにする
        UserGoal newGoal = UserGoal.createActiveGoal(
            this.id, dailyCalorieGoal, proteinGoalG, fatGoalG, netCarbsGoalG, effectiveDate
        );
        
        this.goals.add(newGoal);
        this.updatedAt = Instant.now();
        
        return newGoal;
    }
    
    /**
     * アクティブな目標を取得
     */
    public Optional<UserGoal> getActiveGoal() {
        return goals.stream()
                   .filter(UserGoal::getIsActive)
                   .findFirst();
    }
    
    /**
     * 全ての目標を取得（新しい順）
     */
    public List<UserGoal> getAllGoals() {
        return goals.stream()
                   .sorted((g1, g2) -> g2.getEffectiveDate().compareTo(g1.getEffectiveDate()))
                   .toList();
    }
    
    /**
     * 全てのアクティブ目標を非アクティブにする
     */
    private void deactivateAllGoals() {
        goals.stream()
             .filter(UserGoal::getIsActive)
             .forEach(UserGoal::deactivate);
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
    
    /**
     * 不変条件：アクティブなUserGoalは同時に1つのみ
     */
    private void validateGoals(List<UserGoal> goals) {
        if (goals == null) {
            return;
        }
        
        long activeGoalCount = goals.stream()
                                   .filter(UserGoal::getIsActive)
                                   .count();
        
        if (activeGoalCount > 1) {
            throw new MultipleActiveGoalsException(this.id);
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
        User user = (User) o;
        
        // IDが両方nullでない場合はIDで比較
        if (id != null && user.id != null) {
            return Objects.equals(id, user.id);
        }
        
        // IDがない場合はemailで比較（emailは一意のため）
        return Objects.equals(email, user.email);
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
        return "User{" +
                "id=" + id +
                ", email=" + email +
                ", username=" + username +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                ", activeGoals=" + goals.stream().filter(UserGoal::getIsActive).count() +
                '}';
    }
}
