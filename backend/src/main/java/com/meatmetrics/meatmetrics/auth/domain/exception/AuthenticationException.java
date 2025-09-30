package com.meatmetrics.meatmetrics.auth.domain.exception;

/**
 * 認証失敗を表すアプリケーション例外。
 * ユーザー資格情報の不一致やトークン不正時に送出する。
 */
public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) { super(message); }
    public AuthenticationException(String message, Throwable cause) { super(message, cause); }
}