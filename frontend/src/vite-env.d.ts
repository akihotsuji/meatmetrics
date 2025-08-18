/// <reference types="vite/client" />

interface ImportMetaEnv {
  // アプリケーション基本設定
  readonly VITE_APP_NAME: string;
  readonly VITE_APP_VERSION: string;
  readonly VITE_APP_ENVIRONMENT: 'development' | 'production' | 'test';
  
  // API設定
  readonly VITE_API_BASE_URL: string;
  readonly VITE_PROXY_TARGET: string;
  
  // 認証設定
  readonly VITE_JWT_TOKEN_KEY: string;
  readonly VITE_JWT_REFRESH_KEY: string;
  
  // 機能フラグ
  readonly VITE_FEATURE_FOOD_CATALOG: string;
  readonly VITE_FEATURE_MEAL_TRACKING: string;
  readonly VITE_FEATURE_DAILY_SUMMARY: string;
  readonly VITE_FEATURE_GOAL_SETTING: string;
  readonly VITE_FEATURE_DATA_ANALYSIS: string;
  
  // UI設定
  readonly VITE_APP_THEME: 'light' | 'dark';
  readonly VITE_DEFAULT_LOCALE: string;
  
  // デバッグ設定
  readonly VITE_DEBUG_MODE: string;
  readonly VITE_LOG_LEVEL: 'debug' | 'info' | 'warn' | 'error';
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}
