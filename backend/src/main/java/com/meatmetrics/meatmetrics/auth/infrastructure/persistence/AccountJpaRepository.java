package com.meatmetrics.meatmetrics.auth.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * AccountEntity用のSpring Data JPAリポジトリ
 * 
 * <p>Spring Data JPAの「Query by Method Names」機能を使用して、
 * メソッド名から自動的にSQLクエリを生成します。</p>
 * 
 * <h3>自動生成されるクエリ例:</h3>
 * <ul>
 *   <li>{@code findByEmail} → {@code SELECT * FROM users WHERE email = ?}</li>
 *   <li>{@code findByUsername} → {@code SELECT * FROM users WHERE username = ?}</li>
 *   <li>{@code existsByEmail} → {@code SELECT COUNT(*) > 0 FROM users WHERE email = ?}</li>
 * </ul>
 * 
 * <h3>メソッド命名規則:</h3>
 * <ul>
 *   <li>{@code findBy[PropertyName]} - エンティティを検索</li>
 *   <li>{@code existsBy[PropertyName]} - 存在確認（boolean）</li>
 *   <li>{@code countBy[PropertyName]} - 件数取得</li>
 *   <li>{@code deleteBy[PropertyName]} - 削除</li>
 * </ul>
 * 
 * @see AccountEntity
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
@Repository
public interface AccountJpaRepository extends JpaRepository<AccountEntity, Long> {

    /**
     * メールアドレスでアカウントエンティティを検索
     * 
     * <p>自動生成クエリ: {@code SELECT * FROM users WHERE email = ?}</p>
     * 
     * @param email 検索対象のメールアドレス
     * @return 見つかったアカウントエンティティ、存在しない場合は{@code Optional.empty()}
     */
    Optional<AccountEntity> findByEmail(String email);

    /**
     * ユーザー名でアカウントエンティティを検索
     * 
     * <p>自動生成クエリ: {@code SELECT * FROM users WHERE username = ?}</p>
     * 
     * @param username 検索対象のユーザー名
     * @return 見つかったアカウントエンティティ、存在しない場合は{@code Optional.empty()}
     */
    Optional<AccountEntity> findByUsername(String username);

    /**
     * 指定メールアドレスのアカウントが存在するかチェック
     * 
     * <p>自動生成クエリ: {@code SELECT COUNT(*) > 0 FROM users WHERE email = ?}</p>
     * <p>重複チェックに使用。COUNT(*)を使うため効率的。</p>
     * 
     * @param email チェック対象のメールアドレス
     * @return 存在する場合true、存在しない場合false
     */
    boolean existsByEmail(String email);

    /**
     * 指定ユーザー名のアカウントが存在するかチェック
     * 
     * <p>自動生成クエリ: {@code SELECT COUNT(*) > 0 FROM users WHERE username = ?}</p>
     * <p>重複チェックに使用。COUNT(*)を使うため効率的。</p>
     * 
     * @param username チェック対象のユーザー名
     * @return 存在する場合true、存在しない場合false
     */
    boolean existsByUsername(String username);
}
