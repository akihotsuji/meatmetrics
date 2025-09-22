package com.meatmetrics.meatmetrics.auth.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.meatmetrics.meatmetrics.auth.command.RegisterUserCommand;
import com.meatmetrics.meatmetrics.auth.dto.UserRegisteredResult;
import com.meatmetrics.meatmetrics.domain.user.aggregate.User;
import com.meatmetrics.meatmetrics.domain.user.exception.DuplicateEmailException;
import com.meatmetrics.meatmetrics.domain.user.exception.DuplicateUsernameException;
import com.meatmetrics.meatmetrics.domain.user.repository.UserRepository;
import com.meatmetrics.meatmetrics.domain.user.valueobject.Email;
import com.meatmetrics.meatmetrics.domain.user.valueobject.PasswordHash;
import com.meatmetrics.meatmetrics.domain.user.valueobject.Username;

/**
 * ユーザー登録サービス
 * 
 * <p>新規ユーザーの登録処理を担当します。
 * 重複チェック、パスワードハッシュ化、ドメインモデル生成、永続化を行います。</p>
 * 
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
@Service
@Transactional
public class RegisterUserService {
    private final UserRepository userRepository;

    /**
     * コンストラクタ
     * 
     * @param userRepository ユーザーリポジトリ
     */
    public RegisterUserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    /**
     * ユーザー登録処理
     * 
     * <p>以下の処理を順次実行します：</p>
     * <ol>
     *   <li>メールアドレス重複チェック</li>
     *   <li>ユーザー名重複チェック</li>
     *   <li>パスワードハッシュ化</li>
     *   <li>Userドメインモデル生成</li>
     *   <li>データベース永続化</li>
     * </ol>
     * 
     * @param command ユーザー登録コマンド
     * @return 登録結果DTO（ユーザーID、メール、ユーザー名、登録日時）
     * @throws DuplicateEmailException メールアドレスが既に存在する場合
     * @throws DuplicateUsernameException ユーザー名が既に存在する場合
     * @throws WeakPasswordException パスワードが強度要件を満たさない場合
     */
    public UserRegisteredResult register(RegisterUserCommand command){
        // 1. コマンドからドメイン値オブジェクトに変換
        Email email = command.toEmail();
        Username username = command.toUsername();
        
        // 2. メールアドレス重複チェック
        Optional<User> existingUserByEmail = userRepository.findByEmail(email);
        if(existingUserByEmail.isPresent()){
            throw new DuplicateEmailException(email.getValue());
        }

        // 3. ユーザー名重複チェック
        Optional<User> existingUserByUsername = userRepository.findByUsername(username);
        if(existingUserByUsername.isPresent()){
            throw new DuplicateUsernameException(username.getValue());
        }

        // 4. パスワードハッシュ化（PasswordHashコンストラクタ内で自動実行）
        PasswordHash passwordHash = new PasswordHash(command.getPassword());

        // 5. Userドメインモデル生成（ファクトリメソッド使用）
        User newUser = User.register(email, username, passwordHash);

        // 6. データベースに永続化（IDが自動採番される）
        User savedUser = userRepository.save(newUser);
        
        // 7. ドメインモデルをDTOに変換してレスポンス用に準備
        return UserRegisteredResult.from(savedUser);
    }
}
