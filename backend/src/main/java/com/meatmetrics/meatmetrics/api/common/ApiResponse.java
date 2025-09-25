package com.meatmetrics.meatmetrics.api.common;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * API共通レスポンスDTO
 * 
 * <p>全てのAPIエンドポイントで統一されたレスポンス形式を提供します。</p>
 * 
 * <h3>レスポンス形式:</h3>
 * <pre>
 * {
 *   "success": true,
 *   "message": "操作が完了しました",
 *   "data": { ... },
 *   "timestamp": "2025-09-22T10:00:00.123"
 * }
 * </pre>
 * 
 * <h3>使用例:</h3>
 * <pre>
 * // 成功レスポンス
 * return ResponseEntity.ok(ApiResponse.success("登録完了", userResult));
 * 
 * // エラーレスポンス
 * return ResponseEntity.badRequest().body(ApiResponse.error("バリデーションエラー"));
 * </pre>
 * 
 * @param <T> レスポンスデータの型
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    /** 操作成功フラグ */
    private boolean success;
    
    /** メッセージ（成功・エラー両方で使用） */
    private String message;
    
    /** レスポンスデータ（成功時のみ） */
    private T data;
    
    /** レスポンス生成時刻 */
    private LocalDateTime timestamp;
    
    /**
     * プライベートコンストラクタ
     * 
     * @param success 成功フラグ
     * @param message メッセージ
     * @param data データ
     */
    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }
    
    /**
     * 成功レスポンス（データ付き）を作成
     * 
     * @param <T> データ型
     * @param message 成功メッセージ
     * @param data レスポンスデータ
     * @return 成功レスポンス
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }
    
    /**
     * 成功レスポンス（データなし）を作成
     * 
     * @param message 成功メッセージ
     * @return 成功レスポンス
     */
    public static ApiResponse<Void> success(String message) {
        return new ApiResponse<>(true, message, null);
    }
    
    /**
     * エラーレスポンスを作成
     * 
     * @param message エラーメッセージ
     * @return エラーレスポンス
     */
    public static ApiResponse<Void> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
    
    // Getters
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public LocalDateTime getTimestamp() { return timestamp; }
    
    // Setters（Jackson用）
    public void setSuccess(boolean success) { this.success = success; }
    public void setMessage(String message) { this.message = message; }
    public void setData(T data) { this.data = data; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
}
