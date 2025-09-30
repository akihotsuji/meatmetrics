package com.meatmetrics.meatmetrics.auth.application.handler;

import org.springframework.stereotype.Service;

import com.meatmetrics.meatmetrics.api.auth.dto.response.RefreshResponse;
import com.meatmetrics.meatmetrics.auth.application.command.RefreshCommand;
import com.meatmetrics.meatmetrics.auth.domain.exception.AuthenticationException;
import com.meatmetrics.meatmetrics.auth.domain.repository.AccountRepository;
import com.meatmetrics.meatmetrics.auth.infrastructure.security.JwtTokenService;
import com.meatmetrics.meatmetrics.auth.domain.account.Account;

/**
 * トークン更新サービス
 * 
 * <p>リフレッシュトークンを使用して新しいアクセストークンを発行するアプリケーションサービス。</p>
 * 
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
@Service
public class TokenRefreshHandler {

    private final AccountRepository accountRepository;
    private final JwtTokenService jwtTokenService;

    // コンストラクタ
    public TokenRefreshHandler(AccountRepository accountRepository, JwtTokenService jwtTokenService) {
        this.accountRepository = accountRepository;
        this.jwtTokenService = jwtTokenService;
    }

    public RefreshResponse refresh(RefreshCommand command) {
        String refreshToken = command.getRefreshToken();
    
        if (!jwtTokenService.validateToken(refreshToken)) {
            throw new AuthenticationException("無効なトークンです");
        }
    
        Long userId = jwtTokenService.extractUserId(refreshToken);
        Account account = accountRepository.findById(userId)
            .orElseThrow(() -> new AuthenticationException("無効なトークンです"));
    
        String newAccess = jwtTokenService.generateAccessToken(account);
        String newRefresh = jwtTokenService.generateRefreshToken(account); 
    
        Long expiresIn = jwtTokenService.getAccessTokenExpirationSeconds();
        return RefreshResponse.from(account, newAccess, newRefresh, expiresIn);
    }
}
