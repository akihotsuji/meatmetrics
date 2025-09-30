package com.meatmetrics.meatmetrics.user.domain.profile;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

import com.meatmetrics.meatmetrics.user.domain.exception.InvalidGoalValueException;

/**
 * UserGoalエンティティ
 * ユーザーの栄養目標値を管理する。
 * 時系列で管理され、1ユーザーにつき1つのアクティブな目標を持つ。
 */
public class UserGoal {
    
    // 栄養目標値の妥当性範囲
    private static final int MIN_CALORIE_GOAL = 800;
    private static final int MAX_CALORIE_GOAL = 5000;
    private static final BigDecimal MIN_PROTEIN_GOAL = new BigDecimal("50");
    private static final BigDecimal MAX_PROTEIN_GOAL = new BigDecimal("500");
    private static final BigDecimal MIN_FAT_GOAL = new BigDecimal("30");
    private static final BigDecimal MAX_FAT_GOAL = new BigDecimal("400");
    private static final BigDecimal MIN_NET_CARBS_GOAL = new BigDecimal("0");
    private static final BigDecimal MAX_NET_CARBS_GOAL = new BigDecimal("150");
    
    private final Long id;
    private final Long userId;
    private final Integer dailyCalorieGoal;
    private final BigDecimal proteinGoalG;
    private final BigDecimal fatGoalG;
    private final BigDecimal netCarbsGoalG;
    private final LocalDate effectiveDate;
    private Boolean isActive;
    private final Instant createdAt;
    private Instant updatedAt;
    
    /**
     * 新規UserGoalを作成する（IDなし）
     */
    public UserGoal(Long userId, Integer dailyCalorieGoal, BigDecimal proteinGoalG, 
                   BigDecimal fatGoalG, BigDecimal netCarbsGoalG, LocalDate effectiveDate) {
        this(null, userId, dailyCalorieGoal, proteinGoalG, fatGoalG, netCarbsGoalG, 
             effectiveDate, true, Instant.now(), Instant.now());
    }
    
    /**
     * 既存UserGoalを復元する（IDあり）
     */
    public UserGoal(Long id, Long userId, Integer dailyCalorieGoal, BigDecimal proteinGoalG,
                   BigDecimal fatGoalG, BigDecimal netCarbsGoalG, LocalDate effectiveDate,
                   Boolean isActive, Instant createdAt, Instant updatedAt) {
        
        // バリデーション
        validateUserId(userId);
        validateGoalValues(dailyCalorieGoal, proteinGoalG, fatGoalG, netCarbsGoalG);
        validateEffectiveDate(effectiveDate);
        
        this.id = id;
        this.userId = userId;
        this.dailyCalorieGoal = dailyCalorieGoal;
        this.proteinGoalG = proteinGoalG;
        this.fatGoalG = fatGoalG;
        this.netCarbsGoalG = netCarbsGoalG;
        this.effectiveDate = effectiveDate;
        this.isActive = isActive != null ? isActive : true;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : Instant.now();
    }
    
    /**
     * ファクトリメソッド：新規アクティブ目標を作成
     */
    public static UserGoal createActiveGoal(Long userId, Integer dailyCalorieGoal, 
                                          BigDecimal proteinGoalG, BigDecimal fatGoalG, 
                                          BigDecimal netCarbsGoalG, LocalDate effectiveDate) {
        return new UserGoal(userId, dailyCalorieGoal, proteinGoalG, fatGoalG, 
                           netCarbsGoalG, effectiveDate);
    }
    
    /**
     * 目標をアクティブにする
     */
    public void activate() {
        this.isActive = true;
        this.updatedAt = Instant.now();
    }
    
    /**
     * 目標を非アクティブにする
     */
    public void deactivate() {
        this.isActive = false;
        this.updatedAt = Instant.now();
    }
    
    /**
     * 目標値が妥当性範囲内かチェックする
     */
    public boolean isWithinValidRange() {
        try {
            validateGoalValues(dailyCalorieGoal, proteinGoalG, fatGoalG, netCarbsGoalG);
            return true;
        } catch (InvalidGoalValueException e) {
            return false;
        }
    }
    
    private void validateUserId(Long userId) {
        if (userId == null || userId <= 0) {
            throw new InvalidGoalValueException("userId", userId);
        }
    }
    
    private void validateGoalValues(Integer dailyCalorieGoal, BigDecimal proteinGoalG,
                                  BigDecimal fatGoalG, BigDecimal netCarbsGoalG) {
        
        // カロリー目標のバリデーション
        if (dailyCalorieGoal == null || dailyCalorieGoal < MIN_CALORIE_GOAL || dailyCalorieGoal > MAX_CALORIE_GOAL) {
            throw new InvalidGoalValueException("dailyCalorieGoal", dailyCalorieGoal);
        }
        
        // タンパク質目標のバリデーション
        if (proteinGoalG == null || proteinGoalG.compareTo(MIN_PROTEIN_GOAL) < 0 || proteinGoalG.compareTo(MAX_PROTEIN_GOAL) > 0) {
            throw new InvalidGoalValueException("proteinGoalG", proteinGoalG);
        }
        
        // 脂質目標のバリデーション
        if (fatGoalG == null || fatGoalG.compareTo(MIN_FAT_GOAL) < 0 || fatGoalG.compareTo(MAX_FAT_GOAL) > 0) {
            throw new InvalidGoalValueException("fatGoalG", fatGoalG);
        }
        
        // 糖質目標のバリデーション
        if (netCarbsGoalG == null || netCarbsGoalG.compareTo(MIN_NET_CARBS_GOAL) < 0 || netCarbsGoalG.compareTo(MAX_NET_CARBS_GOAL) > 0) {
            throw new InvalidGoalValueException("netCarbsGoalG", netCarbsGoalG);
        }
    }
    
    private void validateEffectiveDate(LocalDate effectiveDate) {
        if (effectiveDate == null) {
            throw new InvalidGoalValueException("effectiveDate", null);
        }
        
        // 有効日は未来日付も許可（目標の事前設定のため）
        // ただし、あまりに遠い未来日付は制限
        LocalDate maxFutureDate = LocalDate.now().plusYears(1);
        if (effectiveDate.isAfter(maxFutureDate)) {
            throw new InvalidGoalValueException("effectiveDate", "Cannot be more than 1 year in the future");
        }
    }
    
    // Getters
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Integer getDailyCalorieGoal() { return dailyCalorieGoal; }
    public BigDecimal getProteinGoalG() { return proteinGoalG; }
    public BigDecimal getFatGoalG() { return fatGoalG; }
    public BigDecimal getNetCarbsGoalG() { return netCarbsGoalG; }
    public LocalDate getEffectiveDate() { return effectiveDate; }
    public Boolean getIsActive() { return isActive; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserGoal userGoal = (UserGoal) o;
        // IDが両方nullでない場合はIDで比較
        if (id != null && userGoal.id != null) {
            return Objects.equals(id, userGoal.id);
        }
        // IDがない場合は内容で比較
        return Objects.equals(userId, userGoal.userId) &&
               Objects.equals(effectiveDate, userGoal.effectiveDate);
    }
    
    @Override
    public int hashCode() {
        if (id != null) {
            return Objects.hash(id);
        }
        return Objects.hash(userId, effectiveDate);
    }
    
    @Override
    public String toString() {
        return "UserGoal{" +
                "id=" + id +
                ", userId=" + userId +
                ", dailyCalorieGoal=" + dailyCalorieGoal +
                ", proteinGoalG=" + proteinGoalG +
                ", fatGoalG=" + fatGoalG +
                ", netCarbsGoalG=" + netCarbsGoalG +
                ", effectiveDate=" + effectiveDate +
                ", isActive=" + isActive +
                '}';
    }
}
