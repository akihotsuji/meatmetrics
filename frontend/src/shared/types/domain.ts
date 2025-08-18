/**
 * ドメイン型定義
 * DDDアーキテクチャに基づく
 */

// ===== 値オブジェクト =====

export interface UserId {
  readonly value: number;
}

export interface FoodId {
  readonly value: number;
}

export interface MealRecordId {
  readonly value: number;
}

// ===== エンティティ =====

export interface User {
  id: UserId;
  email: string;
  username: string;
  goals: NutritionalGoals;
  createdAt: Date;
  updatedAt: Date;
}

export interface NutritionalGoals {
  calorieGoal: number;
  proteinGoalG: number;
  fatGoalG: number;
  netCarbsGoalG: number;
}

export interface Food {
  id: FoodId;
  name: string;
  category: FoodCategory;
  tags: string[];
  nutritionPer100g: NutritionalContent;
  createdAt: Date;
  updatedAt: Date;
}

export type FoodCategory = 
  | 'meat'
  | 'fish'
  | 'dairy'
  | 'eggs'
  | 'vegetables'
  | 'seasoning'
  | 'other';

export interface NutritionalContent {
  calories: number;
  proteinG: number;
  fatG: number;
  carbohydratesG: number;
  fiberG?: number;
  netCarbsG: number; // carbohydrates - fiber
}

export interface MealRecord {
  id: MealRecordId;
  userId: UserId;
  foodId: FoodId;
  foodName: string;
  amountG: number;
  mealType: MealType;
  recordedAt: Date;
  calculatedNutrition: CalculatedNutrition;
  createdAt: Date;
  updatedAt: Date;
}

export type MealType = 'breakfast' | 'lunch' | 'dinner' | 'snack';

export interface CalculatedNutrition {
  calories: number;
  proteinG: number;
  fatG: number;
  netCarbsG: number;
}

// ===== 集約 =====

export interface DailyNutritionSummary {
  date: Date;
  userId: UserId;
  totalNutrition: CalculatedNutrition;
  goalAchievement: NutritionGoalAchievement;
  mealBreakdown: MealBreakdown;
}

export interface NutritionGoalAchievement {
  caloriesRatio: number; // 実績 / 目標
  proteinRatio: number;
  fatRatio: number;
  netCarbsRatio: number;
}

export interface MealBreakdown {
  breakfast: CalculatedNutrition;
  lunch: CalculatedNutrition;
  dinner: CalculatedNutrition;
  snack: CalculatedNutrition;
}

// ===== ドメインサービス =====

export interface NutritionCalculationService {
  calculateNutritionForAmount(
    nutritionPer100g: NutritionalContent,
    amountG: number
  ): CalculatedNutrition;
  
  calculateNetCarbs(carbohydratesG: number, fiberG?: number): number;
  
  calculateGoalAchievementRatio(
    actual: CalculatedNutrition,
    goals: NutritionalGoals
  ): NutritionGoalAchievement;
}

// ===== イベント =====

export interface DomainEvent {
  eventId: string;
  occurredAt: Date;
  eventType: string;
}

export interface MealRecordCreatedEvent extends DomainEvent {
  eventType: 'MealRecordCreated';
  mealRecord: MealRecord;
}

export interface MealRecordUpdatedEvent extends DomainEvent {
  eventType: 'MealRecordUpdated';
  mealRecord: MealRecord;
  previousAmountG: number;
}

export interface MealRecordDeletedEvent extends DomainEvent {
  eventType: 'MealRecordDeleted';
  mealRecordId: MealRecordId;
  userId: UserId;
  recordedDate: Date;
}

// ===== 仕様 =====

export interface MealRecordSpecification {
  isSatisfiedBy(mealRecord: MealRecord): boolean;
}

export class SameDayMealRecordSpecification implements MealRecordSpecification {
  private targetDate: Date;
  
  constructor(targetDate: Date) {
    this.targetDate = targetDate;
  }
  
  isSatisfiedBy(mealRecord: MealRecord): boolean {
    return this.isSameDay(mealRecord.recordedAt, this.targetDate);
  }
  
  private isSameDay(date1: Date, date2: Date): boolean {
    return date1.toDateString() === date2.toDateString();
  }
}
