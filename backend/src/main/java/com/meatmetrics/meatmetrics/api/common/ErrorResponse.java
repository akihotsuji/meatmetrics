package com.meatmetrics.meatmetrics.api.common;

public record ErrorResponse(String timestamp, String path, String message, String code) {}


