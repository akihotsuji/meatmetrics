package com.meatmetrics.meatmetrics.auth.infrastructure.persistence;

import com.meatmetrics.meatmetrics.auth.domain.account.Account;
import com.meatmetrics.meatmetrics.auth.domain.repository.AccountRepository;
import com.meatmetrics.meatmetrics.sharedkernel.domain.common.Email;
import com.meatmetrics.meatmetrics.sharedkernel.domain.common.Username;

import java.util.Optional;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * UserRepositoryのJPA実装
 * 
 * <p>ドメイン層のUserRepositoryインターフェースを、
 * Spring Data JPAを使用して実装します。</p>
 * 
 * <h3>責務:</h3>
 * <ul>
 *   <li>値オブジェクト → プリミティブ型への変換</li>
 *   <li>ドメインモデル ↔ JPAエンティティの変換</li>
 *   <li>データベースアクセスの実行</li>
 * </ul>
 * 
 * <h3>設計原則:</h3>
 * <ul>
 *   <li>値オブジェクトのバリデーション・正規化を信頼</li>
 *   <li>純粋な技術的変換のみを担当</li>
 *   <li>ドメインロジックは含めない</li>
 * </ul>
 * 
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
@Repository
@Transactional
public class AccountRepositoryJpaImpl implements AccountRepository {

    private final AccountJpaRepository accountJpaRepository;
    private final AccountMapper accountMapper;

    public AccountRepositoryJpaImpl(AccountJpaRepository accountJpaRepository, AccountMapper accountMapper) {
        this.accountJpaRepository = accountJpaRepository;
        this.accountMapper = accountMapper;
    }

    /**
     * Email値オブジェクトでユーザーを検索
     * 
     * <p>Email値オブジェクトが既にバリデーション・正規化済みであることを前提とし、
     * 純粋にデータベース検索とドメインモデル変換のみを行います。</p>
     * 
     * @param email 検索対象のEmail値オブジェクト（null不可）
     * @return 見つかったUser集約、存在しない場合は{@code Optional.empty()}
     * @throws IllegalArgumentException email がnullの場合
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Account> findByEmail(Email email) {
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }

        // Email値オブジェクトから正規化済みの値を取得
        return accountJpaRepository.findByEmail(email.getValue())
                .map(accountMapper::toDomain);
    }

    /**
     * Username値オブジェクトでユーザーを検索
     * 
     * <p>Username値オブジェクトが既にバリデーション済みであることを前提とし、
     * 純粋にデータベース検索とドメインモデル変換のみを行います。</p>
     * 
     * @param username 検索対象のUsername値オブジェクト（null不可）
     * @return 見つかったUser集約、存在しない場合は{@code Optional.empty()}
     * @throws IllegalArgumentException username がnullの場合
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<Account> findByUsername(Username username) {
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }

        // Username値オブジェクトから値を取得
        return accountJpaRepository.findByUsername(username.getValue())
                .map(accountMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> findById(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("UserId cannot be null");
        }

        // Username値オブジェクトから値を取得
        return accountJpaRepository.findById(userId)
                .map(accountMapper::toDomain);
    }

    /**
     * User集約を永続化
     * 
     * <p>User集約の整合性が既に保証されていることを前提とし、
     * JPAエンティティへの変換と永続化のみを行います。</p>
     * 
     * @param user 保存するUser集約（null不可）
     * @return 保存されたUser集約（IDが付与される）
     * @throws IllegalArgumentException user がnullの場合
     */
    @Override
    @Transactional
    public Account save(Account account) {
        if (account == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        AccountEntity entity;

        if (account.getId() == null) {
            // 新規作成: ドメインモデルからJPAエンティティを生成
            entity = accountMapper.toEntity(account);
        } else {
            // 更新: 既存エンティティを取得して更新
            entity = accountJpaRepository.findById(account.getId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + account.getId()));
            accountMapper.updateEntity(entity, account);
        }

        AccountEntity savedEntity = accountJpaRepository.save(entity);
        return accountMapper.toDomain(savedEntity);
    }
}
