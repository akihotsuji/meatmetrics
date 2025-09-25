package com.meatmetrics.meatmetrics.auth.service;

import org.springframework.stereotype.Service;

import com.meatmetrics.meatmetrics.api.auth.dto.response.RefreshResponse;
import com.meatmetrics.meatmetrics.auth.command.RefreshCommand;
import com.meatmetrics.meatmetrics.auth.exception.AuthenticationException;
import com.meatmetrics.meatmetrics.domain.user.aggregate.User;
import com.meatmetrics.meatmetrics.domain.user.repository.UserRepository;

/**
 * トークン更新サービス
 * 
 * <p>リフレッシュトークンを使用して新しいアクセストークンを発行するアプリケーションサービス。</p>
 * 
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
@Service
public class TokenRefreshService {

    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;

    // コンストラクタ
    public TokenRefreshService(UserRepository userRepository, JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
    }

    public RefreshResponse refresh(RefreshCommand command) {
        String refreshToken = command.getRefreshToken();
    
        if (!jwtTokenService.validateToken(refreshToken)) {
            throw new AuthenticationException("無効なトークンです");
        }
    
        Long userId = jwtTokenService.extractUserId(refreshToken);
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new AuthenticationException("無効なトークンです"));
    
        String newAccess = jwtTokenService.generateAccessToken(user);
        String newRefresh = jwtTokenService.generateRefreshToken(user); 
    
        Long expiresIn = jwtTokenService.getAccessTokenExpirationSeconds();
        return RefreshResponse.from(user, newAccess, newRefresh, expiresIn);
    }
}
