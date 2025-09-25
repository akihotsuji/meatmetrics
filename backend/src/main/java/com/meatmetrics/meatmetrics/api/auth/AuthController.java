package com.meatmetrics.meatmetrics.api.auth;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import com.meatmetrics.meatmetrics.api.common.ApiResponse;
import com.meatmetrics.meatmetrics.auth.command.ChangePasswordCommand;
import com.meatmetrics.meatmetrics.auth.command.LoginCommand;
import com.meatmetrics.meatmetrics.auth.command.RefreshCommand;
import com.meatmetrics.meatmetrics.auth.command.RegisterUserCommand;
import com.meatmetrics.meatmetrics.api.auth.dto.request.ChangePasswordRequest;
import com.meatmetrics.meatmetrics.api.auth.dto.request.LoginRequest;
import com.meatmetrics.meatmetrics.api.auth.dto.request.RefreshRequest;
import com.meatmetrics.meatmetrics.api.auth.dto.request.RegisterRequest;
import com.meatmetrics.meatmetrics.api.auth.dto.response.LoginResponse;
import com.meatmetrics.meatmetrics.api.auth.dto.response.RefreshResponse;
import com.meatmetrics.meatmetrics.api.auth.dto.response.RegisterResponse;
import com.meatmetrics.meatmetrics.auth.service.ChangePasswordService;
import com.meatmetrics.meatmetrics.auth.service.JwtTokenService;
import com.meatmetrics.meatmetrics.auth.service.LoginService;
import com.meatmetrics.meatmetrics.auth.service.RegisterUserService;
import com.meatmetrics.meatmetrics.auth.service.TokenRefreshService;

import jakarta.validation.Valid;

/**
 * 認証API Controller
 * 
 * <p>ユーザー認証に関連するAPIエンドポイントを提供します。</p>
 * 
 * <h3>提供エンドポイント:</h3>
 * <ul>
 *   <li>POST /api/auth/register - ユーザー登録</li>
 *   <li>POST /api/auth/login - ログイン</li>
 *   <li>POST /api/auth/logout - ログアウト</li>
 *   <li>POST /api/auth/refresh - トークン更新</li>
 *   <li>POST /api/auth/change-password - パスワード変更</li>
 * </ul>
 * 
 * <h3>セキュリティ設定:</h3>
 * <ul>
 *   <li>register, login, refresh: 認証不要（SecurityConfig設定済み）</li>
 *   <li>logout, change-password: 認証必要</li>
 *   <li>CORS: プロキシ設定で解決（Vite開発環境、Nginx本番環境）</li>
 * </ul>
 * 
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final RegisterUserService registerUserService;
    private final LoginService loginService;
    private final ChangePasswordService changePasswordService;
    private final JwtTokenService jwtTokenService;
    private final TokenRefreshService tokenRefreshService;
    
    /**
     * コンストラクタインジェクション
     * 
     * @param registerUserService ユーザー登録サービス
     * @param loginService ログインサービス
     * @param changePasswordService パスワード変更サービス
     * @param jwtTokenService JWTトークンサービス
     * @param tokenRefreshService トークン更新サービス
     */
    public AuthController(
            RegisterUserService registerUserService,
            LoginService loginService,
            ChangePasswordService changePasswordService,
            JwtTokenService jwtTokenService,
            TokenRefreshService tokenRefreshService) {
        this.registerUserService = registerUserService;
        this.loginService = loginService;
        this.changePasswordService = changePasswordService;
        this.jwtTokenService = jwtTokenService;
        this.tokenRefreshService = tokenRefreshService;
    }
    
    /**
     * ユーザー登録API
     * 
     * <p>新規ユーザーの登録を行います。メールアドレスとユーザー名の重複チェック、
     * パスワードの強度検証を経て、ユーザー情報をデータベースに永続化します。</p>
     * 
     * <h3>処理フロー:</h3>
     * <ol>
     *   <li>リクエストDTOのバリデーション（@Valid による Bean Validation）</li>
     *   <li>RegisterRequestからRegisterUserCommandへの変換（正規化含む）</li>
     *   <li>RegisterUserServiceによるビジネスロジック実行</li>
     *   <li>登録結果をRegisterResponseに変換してAPIレスポンス形式で返却</li>
     * </ol>
     * 
     * <h3>エラーケース:</h3>
     * <ul>
     *   <li>400 Bad Request - バリデーションエラー（必須項目未入力、形式不正等）</li>
     *   <li>409 Conflict - メールアドレスまたはユーザー名の重複</li>
     *   <li>500 Internal Server Error - システムエラー</li>
     * </ul>
     * 
     * @param request ユーザー登録リクエスト（email, password, username）
     * @return 201 Created - 登録成功時のレスポンス（ユーザーID、メール、ユーザー名、登録日時）
     * @throws DuplicateEmailException メールアドレスが既に存在する場合
     * @throws DuplicateUsernameException ユーザー名が既に存在する場合
     * @throws WeakPasswordException パスワードが強度要件を満たさない場合
     * @see RegisterRequest
     * @see RegisterResponse
     * @see RegisterUserService#register(RegisterUserCommand)
     * @since 1.0.0
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@Valid @RequestBody RegisterRequest request) {
        // Requestをユーザー登録用Commandに変換（正規化：trim、メール小文字化）
        RegisterUserCommand command = request.toCommand();
        
        // サービス層でビジネスロジック実行（重複チェック、ドメインモデル生成、永続化）
        RegisterResponse response = registerUserService.register(command);
        
        // 201 Created でAPIレスポンス形式に変換して返却
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("登録完了", response));
    }
    
    /**
     * ログインAPI
     * 
     * <p>ユーザー認証を実行し、成功時にJWTトークンを発行します。
     * メールアドレスとパスワードによる認証を行い、アクセストークンと
     * リフレッシュトークンを返却します。</p>
     * 
     * <h3>処理フロー:</h3>
     * <ol>
     *   <li>リクエストDTOのバリデーション（@Valid による Bean Validation）</li>
     *   <li>LoginRequestからLoginCommandへの変換（正規化含む）</li>
     *   <li>LoginServiceによる認証処理実行</li>
     *   <li>JWTトークン生成と返却</li>
     * </ol>
     * 
     * <h3>エラーケース:</h3>
     * <ul>
     *   <li>400 Bad Request - バリデーションエラー（必須項目未入力、形式不正等）</li>
     *   <li>401 Unauthorized - 認証失敗（メール・パスワード不一致）</li>
     *   <li>500 Internal Server Error - システムエラー</li>
     * </ul>
     * 
     * @param request ログインリクエスト（email, password）
     * @return 200 OK - ログイン成功時のレスポンス（アクセストークン、リフレッシュトークン等）
     * @throws InvalidCredentialsException 認証情報が不正な場合
     * @see LoginRequest
     * @see LoginResponse
     * @see LoginService#login(LoginCommand)
     * @since 1.0.0
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request){
        // Requestをログイン用Commandに変換（正規化：trim、メール小文字化）
        LoginCommand command = request.toCommand();
        
        // サービス層で認証処理実行（パスワード照合、JWT生成）
        LoginResponse response = loginService.login(command);
        
        // 200 OK でAPIレスポンス形式に変換して返却
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("ログイン完了", response));
    }

    /**
     * ログアウトAPI
     * 
     * <p>現在のセッションを終了します。現時点では単純な成功レスポンスを返し、
     * 将来的にトークンブラックリスト機能を追加予定です。</p>
     * 
     * <h3>処理フロー:</h3>
     * <ol>
     *   <li>Authorization Header からトークン取得</li>
     *   <li>トークンの有効性を検証</li>
     *   <li>成功レスポンスを返却（現時点では無効化処理なし）</li>
     * </ol>
     * 
     * <h3>エラーケース:</h3>
     * <ul>
     *   <li>401 Unauthorized - トークンが無効または未提供</li>
     *   <li>500 Internal Server Error - システムエラー</li>
     * </ul>
     * 
     * @param request HTTPリクエスト（Authorization Header からトークン取得用）
     * @return 200 OK - ログアウト成功時のレスポンス
     * @since 1.0.0
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        // Authorization Header からトークンを取得
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("認証トークンが見つかりません"));
        }
        
        // Bearer プレフィックスを除去してトークンを取得
        String token = authorizationHeader.substring(7);
        
        // トークンの有効性を確認
        if (!jwtTokenService.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("無効なトークンです"));
        }
        
        // 現時点では単純な成功レスポンスを返却
        // 将来的にトークンブラックリスト機能を追加予定
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("ログアウト完了", null));
    }

    /**
     * パスワード変更API
     * 
     * <p>認証済みユーザーのパスワードを変更します。現在のパスワード確認後、
     * 新しいパスワードに更新し、セキュリティを保持します。</p>
     * 
     * <h3>処理フロー:</h3>
     * <ol>
     *   <li>リクエストDTOのバリデーション（@Valid による Bean Validation）</li>
     *   <li>Authorization Header からユーザーID取得</li>
     *   <li>ChangePasswordRequestからChangePasswordCommandへの変換</li>
     *   <li>ChangePasswordServiceによるパスワード変更処理実行</li>
     * </ol>
     * 
     * <h3>エラーケース:</h3>
     * <ul>
     *   <li>400 Bad Request - バリデーションエラー（パスワード形式不正等）</li>
     *   <li>401 Unauthorized - 現在パスワード不一致・認証失敗</li>
     *   <li>500 Internal Server Error - システムエラー</li>
     * </ul>
     * 
     * @param request パスワード変更リクエスト（currentPassword, newPassword）
     * @param servletRequest HTTPリクエスト（認証トークン取得用）
     * @return 200 OK - パスワード変更成功時のレスポンス
     * @throws AuthenticationException 現在パスワード不一致・認証失敗の場合
     * @see ChangePasswordRequest
     * @see ChangePasswordService#changePassword(Long, ChangePasswordCommand)
     * @since 1.0.0
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest servletRequest) {
        
        // Authorization Header からトークンを取得
        String authorizationHeader = servletRequest.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("認証トークンが見つかりません"));
        }
        
        // Bearer プレフィックスを除去してトークンを取得
        String token = authorizationHeader.substring(7);
        
        // トークンの有効性を確認し、ユーザーIDを取得
        if (!jwtTokenService.validateToken(token)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("無効なトークンです"));
        }
        
        Long userId = jwtTokenService.extractUserId(token);
        
        // RequestをChangePasswordCommandに変換
        ChangePasswordCommand command = request.toCommand();
        
        // サービス層でパスワード変更処理実行
        changePasswordService.changePassword(userId, command);
        
        // 200 OK でAPIレスポンス形式に変換して返却
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("パスワード変更完了", null));
    }

    /**
     * トークン更新API
     * 
     * <p>リフレッシュトークンを使用して新しいアクセストークンとリフレッシュトークンを発行します。</p>
     * 
     * <h3>処理フロー:</h3>
     * <ol>
     *   <li>リクエストDTOのバリデーション（@Valid による Bean Validation）</li>
     *   <li>RefreshRequestからRefreshCommandへの変換</li>
     *   <li>TokenRefreshServiceによるトークン更新処理実行</li>
     *   <li>新しいトークン情報をRefreshResponseで返却</li>
     * </ol>
     * 
     * <h3>エラーケース:</h3>
     * <ul>
     *   <li>400 Bad Request - バリデーションエラー（リフレッシュトークン未指定等）</li>
     *   <li>401 Unauthorized - リフレッシュトークンが無効または期限切れ</li>
     *   <li>500 Internal Server Error - システムエラー</li>
     * </ul>
     * 
     * @param request トークン更新リクエスト（refreshToken）
     * @return 200 OK - トークン更新成功時のレスポンス（新しいアクセストークン、リフレッシュトークン等）
     * @throws AuthenticationException リフレッシュトークンが無効な場合
     * @see RefreshRequest
     * @see RefreshResponse
     * @see TokenRefreshService#refresh(RefreshCommand)
     * @since 1.0.0
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshResponse>> refresh(@Valid @RequestBody RefreshRequest request) {
        // RequestをRefreshCommandに変換
        RefreshCommand command = request.toCommand();
        
        // サービス層でトークン更新処理実行
        RefreshResponse response = tokenRefreshService.refresh(command);
        
        // 200 OK でAPIレスポンス形式に変換して返却
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.success("トークン更新完了", response));
    }
}
