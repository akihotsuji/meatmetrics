/**
 * API関連の型定義
 * 設計書 docs/2_detail/api/ に基づく
 */

// ===== 共通型 =====

export interface ApiErrorResponse {
  code: 'VALIDATION_ERROR' | 'UNAUTHORIZED' | 'NOT_FOUND' | 'CONFLICT' | 'INTERNAL_ERROR';
  message: string;
  details: Record<string, unknown>;
}

export interface PaginationParams {
  page?: number;
  limit?: number;
}

export interface PaginationResponse {
  total: number;
  page: number;
  limit: number;
  hasNext: boolean;
}

// ===== 認証 API =====

export interface AuthRegisterRequest {
  email: string;
  password: string;
  username: string;
}

export interface AuthRegisterResponse {
  userId: number;
}

export interface AuthLoginRequest {
  email: string;
  password: string;
}

export interface AuthLoginResponse {
  accessToken: string;
  expiresIn: number;
}

export interface AuthRefreshResponse {
  accessToken: string;
  expiresIn: number;
}

// ===== 栄養成分 =====

export interface NutritionPer100g {
  calories: number;
  protein: number;
  fat: number;
  carbohydrates: number;
  fiber?: number;
}

// ===== 食材 API =====

export interface FoodSearchParams {
  q?: string;
  category?: string;
  tags?: string;
}

export interface FoodApiResponse {
  id: number;
  name: string;
  category: string;
  tags: string[];
  nutrition_per_100g: NutritionPer100g;
}

export type FoodListResponse = FoodApiResponse[];

export type FoodDetailResponse = FoodApiResponse;

// ===== 食事記録 API =====

export interface MealRecordRequest {
  food_id: number;
  amount_g: number;
  meal_type: 'breakfast' | 'lunch' | 'dinner' | 'snack';
  recorded_at: string; // YYYY-MM-DD HH:mm:ss
}

export interface MealRecordApiResponse {
  id: number;
  user_id: number;
  food_id: number;
  food_name: string;
  amount_g: number;
  meal_type: 'breakfast' | 'lunch' | 'dinner' | 'snack';
  recorded_at: string;
  created_at: string;
  updated_at: string;
  calculated_nutrition: {
    calories: number;
    protein: number;
    fat: number;
    net_carbs: number;
  };
}

export interface MealRecordUpdateRequest {
  amount_g?: number;
  meal_type?: 'breakfast' | 'lunch' | 'dinner' | 'snack';
  recorded_at?: string;
}

export interface MealRecordListParams {
  date?: string; // YYYY-MM-DD
  meal_type?: 'breakfast' | 'lunch' | 'dinner' | 'snack';
}

export type MealRecordListResponse = MealRecordApiResponse[];

// ===== 日別サマリー API =====

export interface DailySummaryParams {
  date: string; // YYYY-MM-DD
}

export interface DailySummary {
  date: string;
  total_nutrition: {
    calories: number;
    protein: number;
    fat: number;
    net_carbs: number;
  };
  goal_achievement: {
    calories_ratio: number;
    protein_ratio: number;
    fat_ratio: number;
    net_carbs_ratio: number;
  };
  meal_breakdown: {
    breakfast: { calories: number; protein: number; fat: number; net_carbs: number };
    lunch: { calories: number; protein: number; fat: number; net_carbs: number };
    dinner: { calories: number; protein: number; fat: number; net_carbs: number };
    snack: { calories: number; protein: number; fat: number; net_carbs: number };
  };
}

// ===== ユーザー API =====

export interface UserGoals {
  calorie_goal: number;
  protein_goal_g: number;
  fat_goal_g: number;
  net_carbs_goal_g: number;
}

export interface UserGoalsUpdateRequest extends Partial<UserGoals> {}

export interface UserPasswordChangeRequest {
  current_password: string;
  new_password: string;
}

// ===== API レスポンス型（共通ラッパー） =====

export type ApiResponse<T> = {
  success: true;
  data: T;
} | {
  success: false;
  error: ApiErrorResponse;
};

export type ApiListResponse<T> = ApiResponse<T[]>;

export type ApiPaginatedResponse<T> = ApiResponse<{
  items: T[];
  pagination: PaginationResponse;
}>;
