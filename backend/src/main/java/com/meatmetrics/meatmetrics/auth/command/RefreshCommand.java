package com.meatmetrics.meatmetrics.auth.command;

public class RefreshCommand {
    private final String refreshToken;

    public RefreshCommand(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }
}
