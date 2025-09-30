package com.meatmetrics.meatmetrics.user.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserEntity用のSpring Data JPAリポジトリ
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
 * @see UserEntity
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {

    /**
     * メールアドレスでユーザーエンティティを検索
     * 
     * <p>自動生成クエリ: {@code SELECT * FROM users WHERE email = ?}</p>
     * 
     * @param email 検索対象のメールアドレス
     * @return 見つかったユーザーエンティティ、存在しない場合は{@code Optional.empty()}
     */
    Optional<UserEntity> findByEmail(String email);

    /**
     * ユーザー名でユーザーエンティティを検索
     * 
     * <p>自動生成クエリ: {@code SELECT * FROM users WHERE username = ?}</p>
     * 
     * @param username 検索対象のユーザー名
     * @return 見つかったユーザーエンティティ、存在しない場合は{@code Optional.empty()}
     */
    Optional<UserEntity> findByUsername(String username);

    /**
     * 指定メールアドレスのユーザーが存在するかチェック
     * 
     * <p>自動生成クエリ: {@code SELECT COUNT(*) > 0 FROM users WHERE email = ?}</p>
     * <p>重複チェックに使用。COUNT(*)を使うため効率的。</p>
     * 
     * @param email チェック対象のメールアドレス
     * @return 存在する場合true、存在しない場合false
     */
    boolean existsByEmail(String email);

    /**
     * 指定ユーザー名のユーザーが存在するかチェック
     * 
     * <p>自動生成クエリ: {@code SELECT COUNT(*) > 0 FROM users WHERE username = ?}</p>
     * <p>重複チェックに使用。COUNT(*)を使うため効率的。</p>
     * 
     * @param username チェック対象のユーザー名
     * @return 存在する場合true、存在しない場合false
     */
    boolean existsByUsername(String username);
}
