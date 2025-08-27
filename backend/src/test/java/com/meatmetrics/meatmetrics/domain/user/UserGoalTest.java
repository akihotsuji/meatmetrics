package com.meatmetrics.meatmetrics.domain.user;

import com.meatmetrics.meatmetrics.domain.user.exception.InvalidGoalValueException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@DisplayName("UserGoalエンティティのテスト")
class UserGoalTest {

    @Test
    @DisplayName("有効な目標値でUserGoalが作成できる")
    void shouldCreateValidUserGoal() {
        // Given
        Long userId = 1L;
        Integer calorieGoal = 2000;
        BigDecimal proteinGoal = new BigDecimal("150.00");
        BigDecimal fatGoal = new BigDecimal("120.00");
        BigDecimal netCarbsGoal = new BigDecimal("20.00");
        LocalDate effectiveDate = LocalDate.now();
        
        // When
        UserGoal goal = new UserGoal(userId, calorieGoal, proteinGoal, fatGoal, netCarbsGoal, effectiveDate);
        
        // Then
        assertThat(goal.getUserId()).isEqualTo(userId);
        assertThat(goal.getDailyCalorieGoal()).isEqualTo(calorieGoal);
        assertThat(goal.getProteinGoalG()).isEqualTo(proteinGoal);
        assertThat(goal.getFatGoalG()).isEqualTo(fatGoal);
        assertThat(goal.getNetCarbsGoalG()).isEqualTo(netCarbsGoal);
        assertThat(goal.getEffectiveDate()).isEqualTo(effectiveDate);
        assertThat(goal.getIsActive()).isTrue();
    }
    
    @Test
    @DisplayName("ファクトリメソッドcreateActiveGoalで新規アクティブ目標が作成できる")
    void shouldCreateActiveGoalUsingFactoryMethod() {
        // Given
        Long userId = 1L;
        Integer calorieGoal = 2000;
        BigDecimal proteinGoal = new BigDecimal("150.00");
        BigDecimal fatGoal = new BigDecimal("120.00");
        BigDecimal netCarbsGoal = new BigDecimal("20.00");
        LocalDate effectiveDate = LocalDate.now();
        
        // When
        UserGoal goal = UserGoal.createActiveGoal(userId, calorieGoal, proteinGoal, fatGoal, netCarbsGoal, effectiveDate);
        
        // Then
        assertThat(goal.getIsActive()).isTrue();
        assertThat(goal.isWithinValidRange()).isTrue();
    }
    
    @Test
    @DisplayName("目標をアクティブ・非アクティブに切り替えできる")
    void shouldActivateAndDeactivateGoal() {
        // Given
        UserGoal goal = createValidGoal();
        
        // When & Then - deactivate
        goal.deactivate();
        assertThat(goal.getIsActive()).isFalse();
        
        // When & Then - activate
        goal.activate();
        assertThat(goal.getIsActive()).isTrue();
    }
    
    @Test
    @DisplayName("nullのuserIdは拒否される")
    void shouldRejectNullUserId() {
        // Given & When & Then
        assertThatThrownBy(() -> new UserGoal(
            null, 2000, new BigDecimal("150"), new BigDecimal("120"), new BigDecimal("20"), LocalDate.now()
        )).isInstanceOf(InvalidGoalValueException.class)
          .hasMessageContaining("userId");
    }
    
    @Test
    @DisplayName("0以下のuserIdは拒否される")
    void shouldRejectInvalidUserId() {
        // Given & When & Then
        assertThatThrownBy(() -> new UserGoal(
            0L, 2000, new BigDecimal("150"), new BigDecimal("120"), new BigDecimal("20"), LocalDate.now()
        )).isInstanceOf(InvalidGoalValueException.class)
          .hasMessageContaining("userId");
    }
    
    @Test
    @DisplayName("カロリー目標の範囲外の値は拒否される")
    void shouldRejectInvalidCalorieGoal() {
        // Given & When & Then
        // 最小値未満
        assertThatThrownBy(() -> new UserGoal(
            1L, 799, new BigDecimal("150"), new BigDecimal("120"), new BigDecimal("20"), LocalDate.now()
        )).isInstanceOf(InvalidGoalValueException.class)
          .hasMessageContaining("dailyCalorieGoal");
          
        // 最大値超過
        assertThatThrownBy(() -> new UserGoal(
            1L, 5001, new BigDecimal("150"), new BigDecimal("120"), new BigDecimal("20"), LocalDate.now()
        )).isInstanceOf(InvalidGoalValueException.class)
          .hasMessageContaining("dailyCalorieGoal");
          
        // null
        assertThatThrownBy(() -> new UserGoal(
            1L, null, new BigDecimal("150"), new BigDecimal("120"), new BigDecimal("20"), LocalDate.now()
        )).isInstanceOf(InvalidGoalValueException.class)
          .hasMessageContaining("dailyCalorieGoal");
    }
    
    @Test
    @DisplayName("タンパク質目標の範囲外の値は拒否される")
    void shouldRejectInvalidProteinGoal() {
        // Given & When & Then
        // 最小値未満
        assertThatThrownBy(() -> new UserGoal(
            1L, 2000, new BigDecimal("49"), new BigDecimal("120"), new BigDecimal("20"), LocalDate.now()
        )).isInstanceOf(InvalidGoalValueException.class)
          .hasMessageContaining("proteinGoalG");
          
        // 最大値超過
        assertThatThrownBy(() -> new UserGoal(
            1L, 2000, new BigDecimal("501"), new BigDecimal("120"), new BigDecimal("20"), LocalDate.now()
        )).isInstanceOf(InvalidGoalValueException.class)
          .hasMessageContaining("proteinGoalG");
    }
    
    @Test
    @DisplayName("脂質目標の範囲外の値は拒否される")
    void shouldRejectInvalidFatGoal() {
        // Given & When & Then
        // 最小値未満
        assertThatThrownBy(() -> new UserGoal(
            1L, 2000, new BigDecimal("150"), new BigDecimal("29"), new BigDecimal("20"), LocalDate.now()
        )).isInstanceOf(InvalidGoalValueException.class)
          .hasMessageContaining("fatGoalG");
          
        // 最大値超過
        assertThatThrownBy(() -> new UserGoal(
            1L, 2000, new BigDecimal("150"), new BigDecimal("401"), new BigDecimal("20"), LocalDate.now()
        )).isInstanceOf(InvalidGoalValueException.class)
          .hasMessageContaining("fatGoalG");
    }
    
    @Test
    @DisplayName("糖質目標の範囲外の値は拒否される")
    void shouldRejectInvalidNetCarbsGoal() {
        // Given & When & Then
        // 最小値未満
        assertThatThrownBy(() -> new UserGoal(
            1L, 2000, new BigDecimal("150"), new BigDecimal("120"), new BigDecimal("-1"), LocalDate.now()
        )).isInstanceOf(InvalidGoalValueException.class)
          .hasMessageContaining("netCarbsGoalG");
          
        // 最大値超過
        assertThatThrownBy(() -> new UserGoal(
            1L, 2000, new BigDecimal("150"), new BigDecimal("120"), new BigDecimal("151"), LocalDate.now()
        )).isInstanceOf(InvalidGoalValueException.class)
          .hasMessageContaining("netCarbsGoalG");
    }
    
    @Test
    @DisplayName("nullの有効日は拒否される")
    void shouldRejectNullEffectiveDate() {
        // Given & When & Then
        assertThatThrownBy(() -> new UserGoal(
            1L, 2000, new BigDecimal("150"), new BigDecimal("120"), new BigDecimal("20"), null
        )).isInstanceOf(InvalidGoalValueException.class)
          .hasMessageContaining("effectiveDate");
    }
    
    @Test
    @DisplayName("1年を超える未来の有効日は拒否される")
    void shouldRejectTooFutureEffectiveDate() {
        // Given
        LocalDate tooFutureDate = LocalDate.now().plusYears(1).plusDays(1);
        
        // When & Then
        assertThatThrownBy(() -> new UserGoal(
            1L, 2000, new BigDecimal("150"), new BigDecimal("120"), new BigDecimal("20"), tooFutureDate
        )).isInstanceOf(InvalidGoalValueException.class)
          .hasMessageContaining("effectiveDate");
    }
    
    @Test
    @DisplayName("isWithinValidRangeメソッドによる妥当性チェック")
    void shouldValidateGoalValuesCorrectly() {
        // Given
        UserGoal validGoal = createValidGoal();
        
        // When & Then
        assertThat(validGoal.isWithinValidRange()).isTrue();
    }
    
    @Test
    @DisplayName("同じIDのUserGoalオブジェクトは等価")
    void shouldBeEqualForSameId() {
        // Given
        UserGoal goal1 = new UserGoal(1L, 1L, 2000, new BigDecimal("150"), new BigDecimal("120"), 
                                     new BigDecimal("20"), LocalDate.now(), true, null, null);
        UserGoal goal2 = new UserGoal(1L, 1L, 2000, new BigDecimal("150"), new BigDecimal("120"), 
                                     new BigDecimal("20"), LocalDate.now(), true, null, null);
        
        // When & Then
        assertThat(goal1).isEqualTo(goal2);
        assertThat(goal1.hashCode()).isEqualTo(goal2.hashCode());
    }
    
    private UserGoal createValidGoal() {
        return new UserGoal(1L, 2000, new BigDecimal("150.00"), new BigDecimal("120.00"), 
                           new BigDecimal("20.00"), LocalDate.now());
    }
}
