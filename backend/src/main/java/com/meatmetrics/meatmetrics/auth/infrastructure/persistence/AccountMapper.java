package com.meatmetrics.meatmetrics.auth.infrastructure.persistence;

import com.meatmetrics.meatmetrics.auth.domain.account.Account;
import com.meatmetrics.meatmetrics.auth.domain.account.PasswordHash;
import com.meatmetrics.meatmetrics.sharedkernel.domain.common.Email;
import com.meatmetrics.meatmetrics.sharedkernel.domain.common.Username;

import org.springframework.stereotype.Component;


/**
 * Account集約とAccountEntityの変換を担当するマッパークラス
 * 
 * <p>DDDアーキテクチャにおけるインフラ層の責務として、
 * ドメインモデル（Account集約）とJPAエンティティ（AccountEntity）間の
 * データ変換を行います。</p>
 * 
 * <h3>変換の責務:</h3>
 * <ul>
 *   <li>値オブジェクト（Email, Username, PasswordHash）⇔ プリミティブ型</li>
 *   <li>ドメインオブジェクト ⇔ JPAエンティティ</li>
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
public class AccountMapper {

    /**
     * JPAエンティティからドメインモデルへの変換
     * 
     * <p>データベースから取得したAccountEntityを、ビジネスロジックを持つ
     * Account集約ルートに変換します。値オブジェクトの再構築を行い、
     * ドメインモデルとして適切な状態で返却します。</p>
     * 
     * @param entity 変換元のJPAエンティティ（null可）
     * @return 変換されたAccount集約ルート、entityがnullの場合はnull
     * @throws IllegalArgumentException エンティティの必須フィールドが不正な場合
     */
    public Account toDomain(AccountEntity entity) {
        if (entity == null) return null;

        return new Account(
            entity.getId(),
            new Email(entity.getEmail()),
            new Username(entity.getUsername()),
            PasswordHash.fromHash(entity.getPasswordHash()),
            entity.getCreatedAt(),
            entity.getUpdatedAt()
        );
    }

    /**
     * ドメインモデルからJPAエンティティへの変換
     * 
     * <p>Account集約ルートを、データベース永続化用のAccountEntityに変換します。
     * 値オブジェクトからプリミティブ型への変換を行い、JPAで扱える
     * 形式に変換します。</p>
     * 
     * <p><strong>新規作成時:</strong> IDが未設定のエンティティを作成<br>
     * <strong>既存更新時:</strong> IDが設定されたエンティティを作成</p>
     * 
     * @param account 変換元のAccount集約ルート（null可）
     * @return 変換されたJPAエンティティ、accountがnullの場合はnull
     * @throws IllegalArgumentException アカウントの必須値オブジェクトがnullの場合
     */
    public AccountEntity toEntity(Account account) {
        if (account == null) return null;

        AccountEntity entity = new AccountEntity(
            account.getEmail().getValue(),
            account.getUsername().getValue(),
            account.getPasswordHash().getValue()
        );

        // 既存アカウントの場合はIDを設定
        if (account.getId() != null) {
            entity.setId(account.getId());
        }

        return entity;
    }

    /**
     * 既存エンティティをドメインモデルの値で更新
     * 
     * <p>既存のAccountEntityに対して、Account集約ルートの現在の状態を
     * 反映させます。IDやタイムスタンプ以外の業務データを更新し、
     * JPA管理下のエンティティを適切に更新します。</p>
     * 
     * <p><strong>更新対象フィールド:</strong></p>
     * <ul>
     *   <li>email: メールアドレス</li>
     *   <li>username: アカウント名</li>
     *   <li>passwordHash: パスワードハッシュ</li>
     * </ul>
     * 
     * <p><strong>注意:</strong> このメソッドはJPA管理下のエンティティに対して
     * 使用し、更新後にリポジトリでの保存が必要です。</p>
     * 
     * @param entity 更新対象のJPAエンティティ（null不可）
     * @param account 更新内容を持つAccount集約ルート（null不可）
     * @throws IllegalArgumentException entity または account がnullの場合
     * @throws IllegalArgumentException accountの必須値オブジェクトがnullの場合
     */
    public void updateEntity(AccountEntity entity, Account account) {
        if (entity == null) {
            throw new IllegalArgumentException("Entity cannot be null");
        }
        if (account == null) {
            throw new IllegalArgumentException("Account cannot be null");
        }

        entity.setEmail(account.getEmail().getValue());
        entity.setUsername(account.getUsername().getValue());
        entity.setPasswordHash(account.getPasswordHash().getValue());
    }
}
