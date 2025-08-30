package com.meatmetrics.meatmetrics.domain.user.repository;

import com.meatmetrics.meatmetrics.domain.user.aggregate.User;
import com.meatmetrics.meatmetrics.domain.user.valueobject.Email;
import com.meatmetrics.meatmetrics.domain.user.valueobject.Username;
import java.util.Optional;

/**
 * Userリポジトリインターフェース
 * 
 * <p>DDDにおけるドメイン層のリポジトリ抽象化です。
 * 値オブジェクトを使用して型安全性とビジネスルールの適用を保証します。</p>
 * 
 * <h3>設計原則:</h3>
 * <ul>
 *   <li>値オブジェクト（Email, Username）を使用した型安全性</li>
 *   <li>ドメインロジックの保護（バリデーション・正規化は値オブジェクト内）</li>
 *   <li>技術的詳細への非依存</li>
 * </ul>
 * 
 * @author MeatMetrics Development Team
 * @since 1.0.0
 */
public interface UserRepository {

    /**
     * メールアドレスでユーザーを検索
     * 
     * <p>Email値オブジェクトを使用することで：</p>
     * <ul>
     *   <li>型安全性を保証（不正な文字列の受け入れを防止）</li>
     *   <li>自動的なバリデーションとフォーマット正規化</li>
     *   <li>ドメインルールの一元管理</li>
     * </ul>
     * 
     * @param email 検索対象のEmail値オブジェクト（null不可）
     * @return 見つかったユーザー、存在しない場合は{@code Optional.empty()}
     * @throws IllegalArgumentException email がnullの場合
     */
    Optional<User> findByEmail(Email email);

    /**
     * ユーザー名でユーザーを検索
     * 
     * <p>Username値オブジェクトを使用することで：</p>
     * <ul>
     *   <li>型安全性を保証（不正な文字列の受け入れを防止）</li>
     *   <li>自動的なバリデーション</li>
     *   <li>ドメインルールの一元管理</li>
     * </ul>
     * 
     * @param username 検索対象のUsername値オブジェクト（null不可）
     * @return 見つかったユーザー、存在しない場合は{@code Optional.empty()}
     * @throws IllegalArgumentException username がnullの場合
     */
    Optional<User> findByUsername(Username username);

    /**
     * ユーザーを保存（新規作成・更新）
     * 
     * <p>User集約全体の整合性を保持して永続化します。</p>
     * <ul>
     *   <li>新規作成: IDがnullの場合、新しいIDを採番</li>
     *   <li>既存更新: IDが設定済みの場合、既存レコードを更新</li>
     *   <li>不変条件: 集約内のビジネスルールを保証</li>
     * </ul>
     * 
     * @param user 保存するUser集約（null不可）
     * @return 保存されたUser集約（IDが付与される）
     * @throws IllegalArgumentException user がnullの場合
     * @throws IllegalStateException 集約の不変条件に違反している場合
     */
    User save(User user);
}