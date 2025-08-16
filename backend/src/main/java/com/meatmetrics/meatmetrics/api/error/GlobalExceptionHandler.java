package com.meatmetrics.meatmetrics.api.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.time.Instant;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ErrorResponse body(HttpServletRequest request, String message, ApiErrorCode code) {
        return new ErrorResponse(Instant.now().toString(), request.getRequestURI(), message, code.name());
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MissingServletRequestParameterException.class})
    public ResponseEntity<ErrorResponse> handleBadRequest(Exception exception, HttpServletRequest request) {
        log.warn("Bad request: {} {} - {}", request.getMethod(), request.getRequestURI(), exception.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(body(request, "リクエストが不正です。", ApiErrorCode.BAD_REQUEST));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(HttpServletRequest request) {
        log.warn("Method not allowed: {} {}", request.getMethod(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(body(request, "許可されていないメソッドです。", ApiErrorCode.METHOD_NOT_ALLOWED));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, ConstraintViolationException.class})
    public ResponseEntity<ErrorResponse> handleValidation(Exception exception, HttpServletRequest request) {
        log.warn("Validation error: {} {} - {}", request.getMethod(), request.getRequestURI(), exception.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(body(request, "入力値が不正です。", ApiErrorCode.VALIDATION_ERROR));
    }

    @ExceptionHandler({NoSuchElementException.class})
    public ResponseEntity<ErrorResponse> handleNotFound(NoSuchElementException exception, HttpServletRequest request) {
        log.warn("Not found: {} {} - {}", request.getMethod(), request.getRequestURI(), exception.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(body(request, "指定されたリソースが見つかりません。", ApiErrorCode.NOT_FOUND));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleConflict(DataIntegrityViolationException exception, HttpServletRequest request) {
        log.warn("Conflict: {} {} - {}", request.getMethod(), request.getRequestURI(), exception.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(body(request, "リソースの整合性制約に違反しました。", ApiErrorCode.CONFLICT));
    }

    @ExceptionHandler({SQLException.class, DataAccessException.class})
    public ResponseEntity<ErrorResponse> handleDbError(HttpServletRequest request) {
        log.error("Database error: {} {}", request.getMethod(), request.getRequestURI());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body(request, "データベース処理でエラーが発生しました。", ApiErrorCode.DB_ERROR));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAnyException(Exception exception, HttpServletRequest request) {
        log.error("Unhandled exception: {} {} - {}", request.getMethod(), request.getRequestURI(), exception.getClass().getSimpleName());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body(request, "サーバー内部でエラーが発生しました。", ApiErrorCode.INTERNAL_ERROR));
    }
}


