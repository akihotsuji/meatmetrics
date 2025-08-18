/**
 * 環境変数の型定義
 */

export interface AppEnvironment {
  VITE_APP_NAME: string;
  VITE_APP_VERSION: string;
  VITE_APP_ENVIRONMENT: 'development' | 'production' | 'test';
  
  // API設定
  VITE_API_BASE_URL: string;
  VITE_PROXY_TARGET: string;
  
  // 認証設定
  VITE_JWT_TOKEN_KEY: string;
  VITE_JWT_REFRESH_KEY: string;
  
  // 機能フラグ
  VITE_FEATURE_FOOD_CATALOG: string;
  VITE_FEATURE_MEAL_TRACKING: string;
  VITE_FEATURE_DAILY_SUMMARY: string;
  VITE_FEATURE_GOAL_SETTING: string;
  VITE_FEATURE_DATA_ANALYSIS: string;
  
  // UI設定
  VITE_APP_THEME: 'light' | 'dark';
  VITE_DEFAULT_LOCALE: string;
  
  // デバッグ設定
  VITE_DEBUG_MODE: string;
  VITE_LOG_LEVEL: 'debug' | 'info' | 'warn' | 'error';
}

/**
 * 環境変数のランタイム型チェックとパース
 */
export const getEnvironment = (): AppEnvironment => {
  const env = import.meta.env;
  
  // 必須環境変数の検証
  const requiredVars = [
    'VITE_APP_NAME',
    'VITE_APP_VERSION', 
    'VITE_APP_ENVIRONMENT',
    'VITE_API_BASE_URL',
    'VITE_JWT_TOKEN_KEY',
    'VITE_JWT_REFRESH_KEY',
  ];
  
  for (const varName of requiredVars) {
    if (!env[varName]) {
      throw new Error(`Missing required environment variable: ${varName}`);
    }
  }
  
  return {
    VITE_APP_NAME: env.VITE_APP_NAME,
    VITE_APP_VERSION: env.VITE_APP_VERSION,
    VITE_APP_ENVIRONMENT: env.VITE_APP_ENVIRONMENT || 'development',
    
    VITE_API_BASE_URL: env.VITE_API_BASE_URL,
    VITE_PROXY_TARGET: env.VITE_PROXY_TARGET || 'http://backend:8080',
    
    VITE_JWT_TOKEN_KEY: env.VITE_JWT_TOKEN_KEY,
    VITE_JWT_REFRESH_KEY: env.VITE_JWT_REFRESH_KEY,
    
    VITE_FEATURE_FOOD_CATALOG: env.VITE_FEATURE_FOOD_CATALOG || 'true',
    VITE_FEATURE_MEAL_TRACKING: env.VITE_FEATURE_MEAL_TRACKING || 'true',
    VITE_FEATURE_DAILY_SUMMARY: env.VITE_FEATURE_DAILY_SUMMARY || 'true',
    VITE_FEATURE_GOAL_SETTING: env.VITE_FEATURE_GOAL_SETTING || 'true',
    VITE_FEATURE_DATA_ANALYSIS: env.VITE_FEATURE_DATA_ANALYSIS || 'false',
    
    VITE_APP_THEME: env.VITE_APP_THEME || 'light',
    VITE_DEFAULT_LOCALE: env.VITE_DEFAULT_LOCALE || 'ja',
    
    VITE_DEBUG_MODE: env.VITE_DEBUG_MODE || 'false',
    VITE_LOG_LEVEL: env.VITE_LOG_LEVEL || 'info',
  };
};

/**
 * 機能フラグの判定
 */
export const isFeatureEnabled = (featureFlag: string): boolean => {
  return featureFlag.toLowerCase() === 'true';
};

/**
 * 開発環境かどうかの判定
 */
export const isDevelopment = (): boolean => {
  return getEnvironment().VITE_APP_ENVIRONMENT === 'development';
};

/**
 * 本番環境かどうかの判定
 */
export const isProduction = (): boolean => {
  return getEnvironment().VITE_APP_ENVIRONMENT === 'production';
};

/**
 * デバッグモードかどうかの判定
 */
export const isDebugMode = (): boolean => {
  return getEnvironment().VITE_DEBUG_MODE.toLowerCase() === 'true';
};
