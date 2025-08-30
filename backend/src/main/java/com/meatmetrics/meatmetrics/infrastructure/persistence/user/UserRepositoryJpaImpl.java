package com.meatmetrics.meatmetrics.infrastructure.persistence.user;

import com.meatmetrics.meatmetrics.domain.user.aggregate.User;
import com.meatmetrics.meatmetrics.domain.user.repository.UserRepository;
import com.meatmetrics.meatmetrics.domain.user.valueobject.Email;
import com.meatmetrics.meatmetrics.domain.user.valueobject.Username;
import com.meatmetrics.meatmetrics.infrastructure.persistence.user.entity.UserEntity;
import com.meatmetrics.meatmetrics.infrastructure.persistence.user.mapper.UserMapper;
import com.meatmetrics.meatmetrics.infrastructure.persistence.user.repository.UserJpaRepository;

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
public class UserRepositoryJpaImpl implements UserRepository {

    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;

    public UserRepositoryJpaImpl(UserJpaRepository userJpaRepository, UserMapper userMapper) {
        this.userJpaRepository = userJpaRepository;
        this.userMapper = userMapper;
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
    public Optional<User> findByEmail(Email email) {
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }

        // Email値オブジェクトから正規化済みの値を取得
        return userJpaRepository.findByEmail(email.getValue())
                .map(userMapper::toDomain);
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
    public Optional<User> findByUsername(Username username) {
        if (username == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }

        // Username値オブジェクトから値を取得
        return userJpaRepository.findByUsername(username.getValue())
                .map(userMapper::toDomain);
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
    public User save(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        UserEntity entity;

        if (user.getId() == null) {
            // 新規作成: ドメインモデルからJPAエンティティを生成
            entity = userMapper.toEntity(user);
        } else {
            // 更新: 既存エンティティを取得して更新
            entity = userJpaRepository.findById(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + user.getId()));
            userMapper.updateEntity(entity, user);
        }

        UserEntity savedEntity = userJpaRepository.save(entity);
        return userMapper.toDomain(savedEntity);
    }
}
