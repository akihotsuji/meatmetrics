package com.meatmetrics.meatmetrics.domain.user;

import com.meatmetrics.meatmetrics.domain.user.aggregate.User;
import com.meatmetrics.meatmetrics.domain.user.aggregate.UserGoal;
import com.meatmetrics.meatmetrics.domain.user.valueobject.Email;
import com.meatmetrics.meatmetrics.domain.user.valueobject.Username;
import com.meatmetrics.meatmetrics.domain.user.valueobject.PasswordHash;
import com.meatmetrics.meatmetrics.domain.user.exception.DuplicateEmailException;
import com.meatmetrics.meatmetrics.domain.user.exception.DuplicateUsernameException;
import com.meatmetrics.meatmetrics.domain.user.exception.MultipleActiveGoalsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("User集約ルートのテスト")
class UserTest {

    @Test
    @DisplayName("有効な情報でUserが作成できる")
    void shouldCreateValidUser() {
        // Given
        Email email = new Email("test@example.com");
        Username username = new Username("testuser");
        PasswordHash passwordHash = new PasswordHash("password123");
        
        // When
        User user = new User(email, username, passwordHash);
        
        // Then
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getPasswordHash()).isEqualTo(passwordHash);
        assertThat(user.getAllGoals()).isEmpty();
    }
    
    @Test
    @DisplayName("ファクトリメソッドregisterで新規ユーザーが作成できる")
    void shouldCreateUserUsingFactoryMethod() {
        // Given
        Email email = new Email("test@example.com");
        Username username = new Username("testuser");
        PasswordHash passwordHash = new PasswordHash("password123");
        
        // When
        User user = User.register(email, username, passwordHash);
        
        // Then
        assertThat(user.getEmail()).isEqualTo(email);
        assertThat(user.getUsername()).isEqualTo(username);
        assertThat(user.getPasswordHash()).isEqualTo(passwordHash);
    }
    
    @Test
    @DisplayName("正しいパスワードでログインできる")
    void shouldLoginWithCorrectPassword() {
        // Given
        User user = createValidUser();
        String correctPassword = "password123";
        
        // When
        boolean loginResult = user.login(correctPassword);
        
        // Then
        assertThat(loginResult).isTrue();
    }
    
    @Test
    @DisplayName("間違ったパスワードではログインできない")
    void shouldNotLoginWithIncorrectPassword() {
        // Given
        User user = createValidUser();
        String incorrectPassword = "wrongpassword";
        
        // When
        boolean loginResult = user.login(incorrectPassword);
        
        // Then
        assertThat(loginResult).isFalse();
    }
    
    @Test
    @DisplayName("正しい旧パスワードでパスワード変更できる")
    void shouldChangePasswordWithCorrectOldPassword() {
        // Given
        User user = createValidUser();
        String oldPassword = "password123";
        PasswordHash newPasswordHash = new PasswordHash("newpassword123");
        
        // When
        user.changePassword(oldPassword, newPasswordHash);
        
        // Then
        assertThat(user.login("newpassword123")).isTrue();
        assertThat(user.login("password123")).isFalse();
    }
    
    @Test
    @DisplayName("間違った旧パスワードではパスワード変更できない")
    void shouldNotChangePasswordWithIncorrectOldPassword() {
        // Given
        User user = createValidUser();
        String wrongOldPassword = "wrongpassword";
        PasswordHash newPasswordHash = new PasswordHash("newpassword123");
        
        // When & Then
        assertThatThrownBy(() -> user.changePassword(wrongOldPassword, newPasswordHash))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Current password is incorrect");
    }
    
    @Test
    @DisplayName("プロフィール更新ができる")
    void shouldUpdateProfile() {
        // Given
        User user = createValidUser();
        Username newUsername = new Username("newusername");
        
        // When
        user.updateProfile(newUsername);
        
        // Then
        assertThat(user.getUsername()).isEqualTo(newUsername);
    }
    
    @Test
    @DisplayName("新しい目標を設定できる")
    void shouldSetNewGoal() {
        // Given
        User user = createValidUserWithId();
        Integer calorieGoal = 2000;
        BigDecimal proteinGoal = new BigDecimal("150.00");
        BigDecimal fatGoal = new BigDecimal("120.00");
        BigDecimal netCarbsGoal = new BigDecimal("20.00");
        LocalDate effectiveDate = LocalDate.now();
        
        // When
        UserGoal goal = user.setGoal(calorieGoal, proteinGoal, fatGoal, netCarbsGoal, effectiveDate);
        
        // Then
        assertThat(goal).isNotNull();
        assertThat(goal.getIsActive()).isTrue();
        assertThat(user.getActiveGoal()).isPresent();
        assertThat(user.getActiveGoal().get()).isEqualTo(goal);
    }
    
    @Test
    @DisplayName("新しい目標設定時に既存のアクティブ目標は非アクティブになる")
    void shouldDeactivateExistingGoalWhenSettingNew() {
        // Given
        User user = createValidUserWithId();
        
        // 最初の目標を設定
        UserGoal firstGoal = user.setGoal(2000, new BigDecimal("150"), new BigDecimal("120"), 
                                         new BigDecimal("20"), LocalDate.now().minusDays(1));
        
        // When - 新しい目標を設定
        UserGoal secondGoal = user.setGoal(2200, new BigDecimal("160"), new BigDecimal("130"), 
                                          new BigDecimal("30"), LocalDate.now());
        
        // Then
        assertThat(firstGoal.getIsActive()).isFalse();
        assertThat(secondGoal.getIsActive()).isTrue();
        assertThat(user.getActiveGoal()).isPresent();
        assertThat(user.getActiveGoal().get()).isEqualTo(secondGoal);
        assertThat(user.getAllGoals()).hasSize(2);
    }
    
    @Test
    @DisplayName("アクティブな目標がない場合はOptional.emptyが返される")
    void shouldReturnEmptyOptionalWhenNoActiveGoal() {
        // Given
        User user = createValidUser();
        
        // When
        Optional<UserGoal> activeGoal = user.getActiveGoal();
        
        // Then
        assertThat(activeGoal).isEmpty();
    }
    
    @Test
    @DisplayName("全ての目標を新しい順で取得できる")
    void shouldGetAllGoalsInDescendingOrder() {
        // Given
        User user = createValidUserWithId();
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        
        UserGoal oldGoal = user.setGoal(2000, new BigDecimal("150"), new BigDecimal("120"), 
                                       new BigDecimal("20"), yesterday);
        UserGoal newGoal = user.setGoal(2200, new BigDecimal("160"), new BigDecimal("130"), 
                                       new BigDecimal("30"), today);
        
        // When
        List<UserGoal> allGoals = user.getAllGoals();
        
        // Then
        assertThat(allGoals).hasSize(2);
        assertThat(allGoals.get(0)).isEqualTo(newGoal); // より新しい目標が最初
        assertThat(allGoals.get(1)).isEqualTo(oldGoal);
    }
    
    @Test
    @DisplayName("Email重複時に例外が発生する")
    void shouldThrowExceptionForDuplicateEmail() {
        // Given
        User user = createValidUser();
        
        // When & Then
        assertThatThrownBy(() -> user.validateEmailUniqueness(true))
            .isInstanceOf(DuplicateEmailException.class)
            .hasMessageContaining("Email already exists");
    }
    
    @Test
    @DisplayName("Username重複時に例外が発生する")
    void shouldThrowExceptionForDuplicateUsername() {
        // Given
        User user = createValidUser();
        
        // When & Then
        assertThatThrownBy(() -> user.validateUsernameUniqueness(true))
            .isInstanceOf(DuplicateUsernameException.class)
            .hasMessageContaining("Username already exists");
    }
    
    @Test
    @DisplayName("複数のアクティブ目標がある場合に例外が発生する")
    void shouldThrowExceptionForMultipleActiveGoals() {
        // Given
        List<UserGoal> goalsWithMultipleActive = new ArrayList<>();
        UserGoal goal1 = new UserGoal(1L, 1L, 2000, new BigDecimal("150"), new BigDecimal("120"), 
                                     new BigDecimal("20"), LocalDate.now(), true, null, null);
        UserGoal goal2 = new UserGoal(2L, 1L, 2200, new BigDecimal("160"), new BigDecimal("130"), 
                                     new BigDecimal("30"), LocalDate.now(), true, null, null);
        goalsWithMultipleActive.add(goal1);
        goalsWithMultipleActive.add(goal2);
        
        // When & Then
        assertThatThrownBy(() -> new User(1L, new Email("test@example.com"), 
                                         new Username("testuser"), new PasswordHash("password123"),
                                         goalsWithMultipleActive, null, null))
            .isInstanceOf(MultipleActiveGoalsException.class)
            .hasMessageContaining("User has multiple active goals");
    }
    
    @Test
    @DisplayName("nullのEmailは拒否される")
    void shouldRejectNullEmail() {
        // Given & When & Then
        assertThatThrownBy(() -> new User(null, new Username("testuser"), new PasswordHash("password123")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Email cannot be null");
    }
    
    @Test
    @DisplayName("nullのUsernameは拒否される")
    void shouldRejectNullUsername() {
        // Given & When & Then
        assertThatThrownBy(() -> new User(new Email("test@example.com"), null, new PasswordHash("password123")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Username cannot be null");
    }
    
    @Test
    @DisplayName("nullのPasswordHashは拒否される")
    void shouldRejectNullPasswordHash() {
        // Given & When & Then
        assertThatThrownBy(() -> new User(new Email("test@example.com"), new Username("testuser"), null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("PasswordHash cannot be null");
    }
    
    @Test
    @DisplayName("同じEmailのUserオブジェクトは等価")
    void shouldBeEqualForSameEmail() {
        // Given
        Email email = new Email("test@example.com");
        User user1 = new User(email, new Username("user1"), new PasswordHash("password123"));
        User user2 = new User(email, new Username("user2"), new PasswordHash("password456"));
        
        // When & Then
        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).isEqualTo(user2.hashCode());
    }
    
    @Test
    @DisplayName("異なるEmailのUserオブジェクトは非等価")
    void shouldNotBeEqualForDifferentEmails() {
        // Given
        User user1 = new User(new Email("test1@example.com"), new Username("testuser"), new PasswordHash("password123"));
        User user2 = new User(new Email("test2@example.com"), new Username("testuser"), new PasswordHash("password123"));
        
        // When & Then
        assertThat(user1).isNotEqualTo(user2);
    }
    
    private User createValidUser() {
        return new User(new Email("test@example.com"), new Username("testuser"), new PasswordHash("password123"));
    }
    
    private User createValidUserWithId() {
        return new User(1L, new Email("test@example.com"), new Username("testuser"), 
                       new PasswordHash("password123"), new ArrayList<>(), 
                       Instant.now(), Instant.now());
    }
}
