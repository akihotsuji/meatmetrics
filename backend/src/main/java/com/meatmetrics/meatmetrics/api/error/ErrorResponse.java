package com.meatmetrics.meatmetrics.api.error;

public record ErrorResponse(String timestamp, String path, String message, String code) {}


