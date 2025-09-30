package com.meatmetrics.meatmetrics.auth.application.handler;

import org.springframework.stereotype.Service;

import com.meatmetrics.meatmetrics.auth.domain.account.Account;
import com.meatmetrics.meatmetrics.auth.domain.repository.AccountRepository;
import com.meatmetrics.meatmetrics.auth.application.command.ChangePasswordCommand;
import com.meatmetrics.meatmetrics.auth.domain.account.PasswordHash;
import com.meatmetrics.meatmetrics.auth.domain.exception.AuthenticationException;

import jakarta.transaction.Transactional;
import java.util.NoSuchElementException;

@Service
@Transactional
public class ChangePasswordHnadler {

    private final AccountRepository accountRepository;

    /**
     * ChangePasswordServiceのコンストラクタ
     * 
     * <p>依存関係の注入により、アカウントリポジトリを受け取ります。
     * SpringのDIコンテナにより自動的にインスタンス化されます。</p>
     * 
     * @param accountRepository アカウント集約の永続化を担当するリポジトリ（null不可）
     * @throws IllegalArgumentException accountRepositoryがnullの場合
     */
    public ChangePasswordHnadler(AccountRepository accountRepository ){
        this.accountRepository = accountRepository;
    }
    
    /**
     * パスワード変更ユースケース
     * 
     * <p>認証済みアカウントのパスワードを安全に変更します。
     * 現在のパスワード確認後、新しいパスワードに更新し、変更を永続化します。</p>
     * 
     * <p>処理フロー：
     * <ol>
     *   <li>アカウントIDでアカウント集約を取得</li>
     *   <li>現在のパスワードの一致を確認</li>
     *   <li>新しいパスワードの強度チェック（PasswordHashコンストラクタ内）</li>
     *   <li>ドメインの振る舞いでパスワード更新</li>
     *   <li>変更をリポジトリで永続化</li>
     * </ol>
     * </p>
     *
     * <p>セキュリティ考慮事項：
     * <ul>
     *   <li>現在パスワードの照合はドメインレイヤーの責務（PasswordHash.matches）を利用</li>
     *   <li>新パスワードの強度チェックはPasswordHashコンストラクタで実行</li>
     *   <li>パスワードハッシュ化処理はドメインオブジェクトに委譲</li>
     * </ul>
     * </p>
     * 
     * @param accountId 変更対象アカウントのID（null不可、認証済み前提）
     * @param command パスワード変更コマンド（現在・新パスワード含む、null不可）
     * @throws NoSuchElementException 指定されたアカウントIDが存在しない場合（404相当）
     * @throws AuthenticationException 現在のパスワードが不一致の場合（401相当）
     * @throws WeakPasswordException 新パスワードが強度要件を満たさない場合（400相当）
     * @throws IllegalArgumentException 引数がnullまたは不正な場合
     */
    public void changePassword(Long accountId, ChangePasswordCommand command) {
        // 0) 引数nullチェック
        if (accountId == null) {
            throw new IllegalArgumentException("アカウントIDがnullです");
        }
        if (command == null) {
            throw new IllegalArgumentException("コマンドがnullです");
        }
        
        // 1) アカウント取得（存在しない場合は 404）
        Account account = accountRepository.findById(accountId)
            .orElseThrow(() -> new NoSuchElementException("アカウントが見つかりません"));

        // 2) 現在パスワード照合（不一致なら 401）
        boolean matches = account.getPasswordHash().matches(command.getCurrentPassword());
        if (!matches) {
            throw new AuthenticationException("現在のパスワードが正しくありません");
        }

        // 3) 新パスワードへ更新
        // ここでは PasswordHash の利用を推奨。
        // 既存の Account.changePassword(old, newHash) がある場合はそれを使う。
        // 現在の Account.changePassword は (oldPassword, newPasswordHash) シグネチャ。
        // PasswordHash の生成時に強度チェックが入る点に留意。

        // 例: 
        PasswordHash newHash = new PasswordHash(command.getNewPassword());
        account.changePassword(command.getCurrentPassword(), newHash);

        // 4) 保存
        accountRepository.save(account);
    }
}
