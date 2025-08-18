/**
 * UI/UX関連の型定義
 */

// ===== コンポーネント共通 =====

export interface ComponentProps {
  className?: string;
  children?: React.ReactNode;
}

export interface LoadingState {
  isLoading: boolean;
  error?: string | null;
}

export interface AsyncState<T> extends LoadingState {
  data?: T | null;
}

// ===== フォーム =====

export interface FormFieldProps extends ComponentProps {
  label?: string;
  error?: string;
  required?: boolean;
  disabled?: boolean;
}

export interface FormState<T> {
  values: T;
  errors: Partial<Record<keyof T, string>>;
  touched: Partial<Record<keyof T, boolean>>;
  isSubmitting: boolean;
  isValid: boolean;
}

export interface ValidationRule<T = any> {
  required?: boolean | string;
  minLength?: { value: number; message: string };
  maxLength?: { value: number; message: string };
  pattern?: { value: RegExp; message: string };
  min?: { value: number; message: string };
  max?: { value: number; message: string };
  custom?: (value: T) => string | undefined;
}

// ===== テーブル =====

export interface TableColumn<T> {
  key: keyof T | string;
  title: string;
  width?: string | number;
  sortable?: boolean;
  render?: (value: any, record: T, index: number) => React.ReactNode;
}

export interface TableProps<T> extends ComponentProps {
  columns: TableColumn<T>[];
  data: T[];
  loading?: boolean;
  emptyText?: string;
  rowKey?: keyof T | ((record: T) => string | number);
  onRowClick?: (record: T, index: number) => void;
}

export interface SortState {
  key: string;
  direction: 'asc' | 'desc';
}

// ===== モーダル/ダイアログ =====

export interface ModalProps extends ComponentProps {
  open: boolean;
  onClose: () => void;
  title?: string;
  size?: 'sm' | 'md' | 'lg' | 'xl';
  closable?: boolean;
}

export interface ConfirmDialogProps extends Omit<ModalProps, 'children'> {
  message: string;
  confirmText?: string;
  cancelText?: string;
  onConfirm: () => void | Promise<void>;
  onCancel?: () => void;
  type?: 'info' | 'warning' | 'error' | 'success';
}

// ===== 通知 =====

export interface NotificationItem {
  id: string;
  type: 'info' | 'success' | 'warning' | 'error';
  title: string;
  message?: string;
  duration?: number;
  actions?: NotificationAction[];
}

export interface NotificationAction {
  label: string;
  action: () => void;
}

// ===== ナビゲーション =====

export interface NavigationItem {
  key: string;
  label: string;
  icon?: React.ComponentType<any>;
  path?: string;
  children?: NavigationItem[];
  disabled?: boolean;
  badge?: string | number;
}

export interface BreadcrumbItem {
  label: string;
  path?: string;
}

// ===== レイアウト =====

export interface LayoutProps extends ComponentProps {
  header?: React.ReactNode;
  sidebar?: React.ReactNode;
  footer?: React.ReactNode;
}

export interface PageProps extends ComponentProps {
  title?: string;
  breadcrumbs?: BreadcrumbItem[];
  actions?: React.ReactNode;
  loading?: boolean;
}

// ===== テーマ =====

export interface ThemeConfig {
  mode: 'light' | 'dark';
  primaryColor: string;
  borderRadius: string;
  fontFamily: string;
}

export interface ColorPalette {
  primary: string;
  secondary: string;
  success: string;
  warning: string;
  error: string;
  info: string;
  background: string;
  surface: string;
  onBackground: string;
  onSurface: string;
}

// ===== リスト表示 =====

export interface ListProps<T> extends ComponentProps {
  items: T[];
  renderItem: (item: T, index: number) => React.ReactNode;
  emptyText?: string;
  loading?: boolean;
  loadMore?: () => void;
  hasMore?: boolean;
}

export interface GridProps<T> extends ComponentProps {
  items: T[];
  renderItem: (item: T, index: number) => React.ReactNode;
  columns?: number;
  gap?: string;
  emptyText?: string;
  loading?: boolean;
}

// ===== 検索/フィルター =====

export interface SearchProps extends ComponentProps {
  placeholder?: string;
  value: string;
  onChange: (value: string) => void;
  onSearch?: (value: string) => void;
  loading?: boolean;
  suggestions?: string[];
}

export interface FilterOption {
  label: string;
  value: string | number;
  count?: number;
}

export interface FilterProps extends ComponentProps {
  title: string;
  options: FilterOption[];
  value: (string | number)[];
  onChange: (value: (string | number)[]) => void;
  multiple?: boolean;
  searchable?: boolean;
}

// ===== チャート =====

export interface ChartDataPoint {
  x: string | number | Date;
  y: number;
  label?: string;
  color?: string;
}

export interface ChartProps extends ComponentProps {
  data: ChartDataPoint[];
  width?: number;
  height?: number;
  type?: 'line' | 'bar' | 'pie' | 'area';
  showLegend?: boolean;
  showTooltip?: boolean;
}

// ===== 入力コンポーネント =====

export interface InputProps extends ComponentProps {
  type?: 'text' | 'email' | 'password' | 'number' | 'tel' | 'url';
  value: string;
  onChange: (value: string) => void;
  placeholder?: string;
  disabled?: boolean;
  error?: string;
  prefix?: React.ReactNode;
  suffix?: React.ReactNode;
}

export interface SelectOption {
  label: string;
  value: string | number;
  disabled?: boolean;
}

export interface SelectProps extends ComponentProps {
  options: SelectOption[];
  value: string | number | (string | number)[];
  onChange: (value: string | number | (string | number)[]) => void;
  placeholder?: string;
  disabled?: boolean;
  error?: string;
  multiple?: boolean;
  searchable?: boolean;
}
