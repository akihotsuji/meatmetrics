package com.meatmetrics.meatmetrics.auth.service;

import org.springframework.stereotype.Service;

import com.meatmetrics.meatmetrics.auth.command.LoginCommand;
import com.meatmetrics.meatmetrics.api.auth.dto.response.LoginResponse;
import com.meatmetrics.meatmetrics.domain.user.aggregate.User;
import com.meatmetrics.meatmetrics.domain.user.repository.UserRepository;
import com.meatmetrics.meatmetrics.domain.user.valueobject.Email;

import jakarta.transaction.Transactional;

import com.meatmetrics.meatmetrics.auth.exception.AuthenticationException;

/**
 * ログインサービス
 * 
 * <p>ユーザー認証とJWTトークン発行を担当するアプリケーションサービス。</p>
 * 
 * <h3>実装ヒント:</h3>
 * <ul>
 *   <li>login(): command.toEmail() → userRepository.findByEmail() → user.login() → JWT生成</li>
 *   <li>refreshToken(): validateToken() → extractUserId() → findById() → 新JWT生成</li>
 *   <li>認証失敗時は AuthenticationException をスロー</li>
 *   <li>User.login(plainPassword) でパスワード照合</li>
 *   <li>LoginResult.from() でレスポンス生成</li>
 * </ul>
 * 
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
@Service
@Transactional
public class LoginService {
    
    private final UserRepository userRepository;
    private final JwtTokenService jwtTokenService;
    
    /**
     * コンストラクタ
     * 
     * @param userRepository ユーザーリポジトリ
     * @param jwtTokenService JWTトークンサービス
     */
    public LoginService(UserRepository userRepository, JwtTokenService jwtTokenService) {
        this.userRepository = userRepository;
        this.jwtTokenService = jwtTokenService;
    }
    
    /**
     * ユーザーログイン
     * 
     * @param command ログインコマンド  
     * @return ログイン結果（JWTトークン含む）
     * @throws AuthenticationException 認証失敗時
     */
    // public LoginResult login(LoginCommand command) { ... }
    public LoginResponse login(LoginCommand command){
        // ステップ1: バリデーション済みのコマンドから値オブジェクトに変換
        Email email = command.toEmail();
        String plainPassword = command.getPassword();

        // ステップ2: メールアドレスでユーザーを検索
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new AuthenticationException("メールアドレスまたはパスワードが不正です"));

        // ステップ3: パスワード認証（User集約のloginメソッド使用）
        if (!user.login(plainPassword)){
            throw new AuthenticationException("メールアドレスまたはパスワードが不正です");
        }

        // ステップ4: JWT生成（アクセストークン + リフレッシュトークン）
        String accessToken = jwtTokenService.generateAccessToken(user);
        String refreshToken = jwtTokenService.generateRefreshToken(user);
        long expirationSeconds  = jwtTokenService.getAccessTokenExpirationSeconds();

        // ステップ5: レスポンス用DTOを生成（LoginResultクラスのstaticメソッドであるfrom()内部で生成されたインスタンスを返却）
        return LoginResponse.from(user, accessToken, refreshToken, expirationSeconds);
    }
    
}
