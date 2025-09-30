package com.meatmetrics.meatmetrics.auth.application.handler;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.meatmetrics.meatmetrics.auth.application.command.RegisterAccountCommand;
import com.meatmetrics.meatmetrics.auth.domain.account.PasswordHash;
import com.meatmetrics.meatmetrics.auth.domain.exception.DuplicateEmailException;
import com.meatmetrics.meatmetrics.auth.domain.exception.DuplicateUsernameException;
import com.meatmetrics.meatmetrics.api.auth.dto.response.RegisterResponse;
import com.meatmetrics.meatmetrics.sharedkernel.domain.common.Email;
import com.meatmetrics.meatmetrics.sharedkernel.domain.common.Username;
import com.meatmetrics.meatmetrics.auth.domain.account.Account;
import com.meatmetrics.meatmetrics.auth.domain.repository.AccountRepository;

/**
 * アカウント登録サービス
 * 
 * <p>新規アカウントの登録処理を担当します。
 * 重複チェック、パスワードハッシュ化、ドメインモデル生成、永続化を行います。</p>
 * 
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
@Service
@Transactional
public class RegisterAccountHandler {
    private final AccountRepository accountRepository;

    /**
     * コンストラクタ
     * 
     * @param accountRepository アカウントリポジトリ
     */
    public RegisterAccountHandler(AccountRepository accountRepository){
        this.accountRepository = accountRepository;
    }

    /**
     * アカウント登録処理
     * 
     * <p>以下の処理を順次実行します：</p>
     * <ol>
     *   <li>メールアドレス重複チェック</li>
     *   <li>アカウント名重複チェック</li>
     *   <li>パスワードハッシュ化</li>
     *   <li>Accountドメインモデル生成</li>
     *   <li>データベース永続化</li>
     * </ol>
     * 
     * @param command アカウント登録コマンド
     * @return 登録結果DTO（アカウントID、メール、アカウント名、登録日時）
     * @throws DuplicateEmailException メールアドレスが既に存在する場合
     * @throws DuplicateUsernameException アカウント名が既に存在する場合
     * @throws WeakPasswordException パスワードが強度要件を満たさない場合
     */
    public RegisterResponse register(RegisterAccountCommand command){
        // 1. コマンドからドメイン値オブジェクトに変換
        Email email = command.toEmail();
        Username username = command.toUsername();
        
        // 2. メールアドレス重複チェック
        Optional<Account> existingAccountByEmail = accountRepository.findByEmail(email);
        if(existingAccountByEmail.isPresent()){
            throw new DuplicateEmailException(email.getValue());
        }

        // 3. アカウント名重複チェック
        Optional<Account> existingAccountByUsername = accountRepository.findByUsername(username);
        if(existingAccountByUsername.isPresent()){
            throw new DuplicateUsernameException(username.getValue());
        }

        // 4. パスワードハッシュ化（PasswordHashコンストラクタ内で自動実行）
        PasswordHash passwordHash = new PasswordHash(command.getPassword());

        // 5. Accountドメインモデル生成（ファクトリメソッド使用）
        Account newAccount = Account.register(email, username, passwordHash);

        // 6. データベースに永続化（IDが自動採番される）
        Account savedAccount = accountRepository.save(newAccount);
        
        // 7. ドメインモデルをDTOに変換してレスポンス用に準備
        return RegisterResponse.from(savedAccount);
    }
}
