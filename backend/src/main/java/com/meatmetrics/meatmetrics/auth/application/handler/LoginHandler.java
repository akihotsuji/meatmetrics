package com.meatmetrics.meatmetrics.auth.application.handler;

import org.springframework.stereotype.Service;

import com.meatmetrics.meatmetrics.api.auth.dto.response.LoginResponse;
import com.meatmetrics.meatmetrics.auth.domain.account.Account;
import com.meatmetrics.meatmetrics.auth.domain.repository.AccountRepository;

import jakarta.transaction.Transactional;

import com.meatmetrics.meatmetrics.auth.application.command.LoginCommand;
import com.meatmetrics.meatmetrics.auth.domain.exception.AuthenticationException;
import com.meatmetrics.meatmetrics.auth.infrastructure.security.JwtTokenService;
import com.meatmetrics.meatmetrics.sharedkernel.domain.common.Email;

/**
 * ログインサービス
 * 
 * <p>アカウント認証とJWTトークン発行を担当するアプリケーションサービス。</p>
 * 
 * <h3>実装ヒント:</h3>
 * <ul>
 *   <li>login(): command.toEmail() → AccountRepository.findByEmail() → account.login() → JWT生成</li>
 *   <li>refreshToken(): validateToken() → extractAccountId() → findById() → 新JWT生成</li>
 *   <li>認証失敗時は AuthenticationException をスロー</li>
 *   <li>Account.login(plainPassword) でパスワード照合</li>
 *   <li>LoginResult.from() でレスポンス生成</li>
 * </ul>
 * 
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
@Service
@Transactional
public class LoginHandler {
    
    private final AccountRepository accountRepository;
    private final JwtTokenService jwtTokenService;
    
    /**
     * コンストラクタ
     * 
     * @param accountRepository アカウントリポジトリ
     * @param jwtTokenService JWTトークンサービス
     */
    public LoginHandler(AccountRepository AccountRepository, JwtTokenService jwtTokenService) {
        this.accountRepository = AccountRepository;
        this.jwtTokenService = jwtTokenService;
    }
    
    /**
     * アカウントログイン
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

        // ステップ2: メールアドレスでアカウントを検索
        Account account = accountRepository.findByEmail(email)
            .orElseThrow(() -> new AuthenticationException("メールアドレスまたはパスワードが不正です"));

        // ステップ3: パスワード認証（Account集約のloginメソッド使用）
        if (!account.login(plainPassword)){
            throw new AuthenticationException("メールアドレスまたはパスワードが不正です");
        }

        // ステップ4: JWT生成（アクセストークン + リフレッシュトークン）
        String accessToken = jwtTokenService.generateAccessToken(account);
        String refreshToken = jwtTokenService.generateRefreshToken(account);
        long expirationSeconds  = jwtTokenService.getAccessTokenExpirationSeconds();

        // ステップ5: レスポンス用DTOを生成（LoginResultクラスのstaticメソッドであるfrom()内部で生成されたインスタンスを返却）
        return LoginResponse.from(account, accessToken, refreshToken, expirationSeconds);
    }
    
}
