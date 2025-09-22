package com.meatmetrics.meatmetrics.infrastructure.persistence.user.mapper;

import com.meatmetrics.meatmetrics.domain.user.aggregate.User;
import com.meatmetrics.meatmetrics.domain.user.valueobject.Email;
import com.meatmetrics.meatmetrics.domain.user.valueobject.Username;
import com.meatmetrics.meatmetrics.domain.user.valueobject.PasswordHash;
import com.meatmetrics.meatmetrics.infrastructure.persistence.user.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

/**
 * User集約とUserEntityの変換を担当するマッパークラス
 * 
 * <p>DDDアーキテクチャにおけるインフラ層の責務として、
 * ドメインモデル（User集約）とJPAエンティティ（UserEntity）間の
 * データ変換を行います。</p>
 * 
 * <h3>変換の責務:</h3>
 * <ul>
 *   <li>値オブジェクト（Email, Username, PasswordHash）⇔ プリミティブ型</li>
 *   <li>ドメインオブジェクト ⇔ JPAエンティティ</li>
 *   <li>集約の境界維持（UserGoalは別途取得）</li>
 * </ul>
 * 
 * <h3>設計原則:</h3>
 * <ul>
 *   <li>null安全性を保証</li>
 *   <li>ドメインロジックは含めない（純粋な変換のみ）</li>
 *   <li>技術的詳細をドメイン層から隔離</li>
 * </ul>
 * 
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
@Component
public class UserMapper {

    /**
     * JPAエンティティからドメインモデルへの変換
     * 
     * <p>データベースから取得したUserEntityを、ビジネスロジックを持つ
     * User集約ルートに変換します。値オブジェクトの再構築を行い、
     * ドメインモデルとして適切な状態で返却します。</p>
     * 
     * <p><strong>注意:</strong> UserGoalエンティティは別途リポジトリから
     * 取得する必要があります。このメソッドでは空のリストで初期化されます。</p>
     * 
     * @param entity 変換元のJPAエンティティ（null可）
     * @return 変換されたUser集約ルート、entityがnullの場合はnull
     * @throws IllegalArgumentException エンティティの必須フィールドが不正な場合
     */
    public User toDomain(UserEntity entity) {
        if (entity == null) return null;

        return new User(
            entity.getId(),
            new Email(entity.getEmail()),
            new Username(entity.getUsername()),
            PasswordHash.fromHash(entity.getPasswordHash()),
            new ArrayList<>(), // UserGoalは別途取得
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * ドメインモデルからJPAエンティティへの変換
     * 
     * <p>User集約ルートを、データベース永続化用のUserEntityに変換します。
     * 値オブジェクトからプリミティブ型への変換を行い、JPAで扱える
     * 形式に変換します。</p>
     * 
     * <p><strong>新規作成時:</strong> IDが未設定のエンティティを作成<br>
     * <strong>既存更新時:</strong> IDが設定されたエンティティを作成</p>
     * 
     * @param user 変換元のUser集約ルート（null可）
     * @return 変換されたJPAエンティティ、userがnullの場合はnull
     * @throws IllegalArgumentException ユーザーの必須値オブジェクトがnullの場合
     */
    public UserEntity toEntity(User user) {
        if (user == null) return null;

        UserEntity entity = new UserEntity(
            user.getEmail().getValue(),
            user.getUsername().getValue(),
            user.getPasswordHash().getValue()
        );

        // 既存ユーザーの場合はIDを設定
        if (user.getId() != null) {
            entity.setId(user.getId());
        }

        return entity;
    }

    /**
     * 既存エンティティをドメインモデルの値で更新
     * 
     * <p>既存のUserEntityに対して、User集約ルートの現在の状態を
     * 反映させます。IDやタイムスタンプ以外の業務データを更新し、
     * JPA管理下のエンティティを適切に更新します。</p>
     * 
     * <p><strong>更新対象フィールド:</strong></p>
     * <ul>
     *   <li>email: メールアドレス</li>
     *   <li>username: ユーザー名</li>
     *   <li>passwordHash: パスワードハッシュ</li>
     * </ul>
     * 
     * <p><strong>注意:</strong> このメソッドはJPA管理下のエンティティに対して
     * 使用し、更新後にリポジトリでの保存が必要です。</p>
     * 
     * @param entity 更新対象のJPAエンティティ（null不可）
     * @param user 更新内容を持つUser集約ルート（null不可）
     * @throws IllegalArgumentException entity または user がnullの場合
     * @throws IllegalArgumentException userの必須値オブジェクトがnullの場合
     */
    public void updateEntity(UserEntity entity, User user) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null");
        }

        entity.setEmail(user.getEmail().getValue());
        entity.setUsername(user.getUsername().getValue());
        entity.setPasswordHash(user.getPasswordHash().getValue());
    }
}
