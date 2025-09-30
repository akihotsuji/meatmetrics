# ユーザープロフィール機能実装 - 今日の詳細タスクリスト

## 🎯 全体概要

ユーザーコンテキストのプロフィール機能（Profile 集約）を DDD 実装順序で実装する。auth コンテキストの実装パターンを参考に、Domain → Application → Infrastructure → Web 層の順序で進める。

**実装対象**: プロフィール管理の完全実装

- **Domain 層**: `DisplayName` 値オブジェクト、`Profile` 集約ルート、列挙型群
- **Application 層**: `UpdateProfileCommand`、`UpdateProfileHandler`、`GetProfileHandler`
- **Infrastructure 層**: `ProfileEntity`、`ProfileMapper`、`ProfileRepository` 実装
- **Web 層**: `ProfileController`、DTO 群（Request/Response）

**作業時間見積**: 8-10 時間（TDD 込み）  
**完了目標**: 今日中にプロフィール機能の完全実装（`GET /api/users/profile`, `PUT /api/users/profile`）

---

## 📋 詳細タスクリスト

### 🔧 **Phase 1: 事前準備・設計確認** (30 分)

#### ✅ Task 1-1: マイグレーション確認と実行

- [×] **V015\_\_create_user_profiles_table.sql** 作成完了
- [×] **V016\_\_insert_test_user_profiles.sql** 作成完了
- [ ] **マイグレーション実行確認**
  ```bash
  # Docker環境起動
  docker-compose -f infrastructure/docker/dev/docker-compose.yml up -d postgres
  # Spring Boot起動でFlyway自動実行確認
  ```
- [ ] **テーブル作成確認**: `user_profiles` テーブルとテストデータ確認

#### Task 1-2: auth コンテキスト参考実装の確認

- [ ] **参考パターン確認**:
  - `Email`, `Username`, `PasswordHash` 値オブジェクト
  - `Account` 集約ルート
  - `RegisterAccountHandler`, `LoginHandler`
  - `AuthController` 実装パターン
- [ ] **パッケージ構造確認**: `com.meatmetrics.meatmetrics.user` 配下のディレクトリ準備

### 🏗️ **Phase 2: Domain 層実装** (120 分)

#### ⏭️ Task 2-1: 列挙型とドメイン例外実装 (30 分)

##### Subtask 2-1-1: 列挙型群の実装 (20 分)

- [ ] **Gender 列挙型作成**: `com.meatmetrics.meatmetrics.user.domain.profile.Gender`

  ```java
  public enum Gender {
      MALE("male"),
      FEMALE("female"),
      OTHER("other"),
      PREFER_NOT_TO_SAY("prefer_not_to_say");
  ```

- [ ] **CarnivoreLevel 列挙型作成**: `com.meatmetrics.meatmetrics.user.domain.profile.CarnivoreLevel`

  ```java
  public enum CarnivoreLevel {
      BEGINNER("beginner"),
      INTERMEDIATE("intermediate"),
      ADVANCED("advanced");
  ```

- [ ] **ActivityLevel 列挙型作成**: `com.meatmetrics.meatmetrics.user.domain.profile.ActivityLevel`
  ```java
  public enum ActivityLevel {
      SEDENTARY("sedentary"),
      LIGHTLY_ACTIVE("lightly_active"),
      MODERATELY_ACTIVE("moderately_active"),
      VERY_ACTIVE("very_active"),
      EXTREMELY_ACTIVE("extremely_active");
  ```

##### Subtask 2-1-2: ドメイン例外の実装 (10 分)

- [ ] **InvalidDisplayNameException 作成**: `com.meatmetrics.meatmetrics.user.domain.exception.InvalidDisplayNameException`
- [ ] **ProfileNotFoundException 作成**: `com.meatmetrics.meatmetrics.user.domain.exception.ProfileNotFoundException`
- [ ] **InvalidProfileDataException 作成**: `com.meatmetrics.meatmetrics.user.domain.exception.InvalidProfileDataException`

#### ⏭️ Task 2-2: DisplayName 値オブジェクト実装 (30 分)

##### Subtask 2-2-1: DisplayName 基本構造作成 (15 分)

- [ ] **ファイル作成**: `com.meatmetrics.meatmetrics.user.domain.profile.DisplayName`
- [ ] **基本クラス構造実装**:

  ```java
  public class DisplayName {
      private final String value;       // 表示名（3-50文字）

      public DisplayName(String value) {
          validateDisplayName(value);
          this.value = value.trim();
      }
  ```

##### Subtask 2-2-2: DisplayName バリデーション実装 (15 分)

- [ ] **バリデーションメソッド実装**:
  - 3-50 文字チェック
  - 日本語・英数字・一部記号のみ許可
  - XSS 対策用特殊文字制限
- [ ] **例外処理**: `InvalidDisplayNameException` で不正値を拒否
- [ ] **equals/hashCode/toString メソッド**（値オブジェクトの等価性実装）

#### ⏭️ Task 2-3: Profile 集約ルート実装 (60 分)

##### Subtask 2-3-1: Profile 基本構造作成 (20 分)

- [ ] **ファイル作成**: `com.meatmetrics.meatmetrics.user.domain.profile.Profile`
- [ ] **基本クラス構造実装**:
  ```java
  public class Profile {
      private final Long id;
      private final Long userId;
      private DisplayName displayName;
      private String firstName;
      private String lastName;
      private LocalDate dateOfBirth;
      private Gender gender;
      private BigDecimal heightCm;
      private BigDecimal weightKg;
      private ActivityLevel activityLevel;
      private LocalDate carnivoreStartDate;
      private CarnivoreLevel carnivoreLevel;
      private String carnivoreGoal;
      private String measurementUnit;
      private String timezone;
      private String avatarUrl;
      private final Instant createdAt;
      private Instant updatedAt;
  ```

##### Subtask 2-3-2: Profile ファクトリーメソッド実装 (20 分)

- [ ] **新規 Profile 作成**:

  ```java
  public static Profile create(Long userId, DisplayName displayName) {
      return new Profile(null, userId, displayName, null, null, null,
                        Gender.PREFER_NOT_TO_SAY, null, null, null, null,
                        CarnivoreLevel.BEGINNER, null, "metric", "UTC", null,
                        Instant.now(), Instant.now());
  }
  ```

- [ ] **復元用コンストラクタ**: 既存データからの Profile 復元用

##### Subtask 2-3-3: Profile 更新メソッド実装 (20 分)

- [ ] **プロフィール更新メソッド**:

  ```java
  public void updateProfile(DisplayName displayName, String firstName, String lastName,
                           LocalDate dateOfBirth, Gender gender, BigDecimal heightCm,
                           BigDecimal weightKg, ActivityLevel activityLevel,
                           LocalDate carnivoreStartDate, CarnivoreLevel carnivoreLevel,
                           String carnivoreGoal, String measurementUnit, String timezone,
                           String avatarUrl) {
      // 不変条件チェック後、各フィールド更新
      this.updatedAt = Instant.now();
  }
  ```

- [ ] **不変条件バリデーション**: ユーザー ID 必須、表示名必須等
- [ ] **ビジネスルール実装**: 誕生日は過去日付のみ、身長・体重の妥当性等

#### ⏭️ Task 2-4: ProfileRepository インターフェース定義 (0 分)

- [ ] **ファイル作成**: `com.meatmetrics.meatmetrics.user.domain.repository.ProfileRepository`
- [ ] **インターフェース定義**:
  ```java
  public interface ProfileRepository {
      Optional<Profile> findByUserId(Long userId);
      Profile save(Profile profile);
      void deleteByUserId(Long userId);
      boolean existsByUserId(Long userId);
  }
  ```

### 🔧 **Phase 3: Application 層実装** (90 分)

#### ⏭️ Task 3-1: Command と Response 作成 (30 分)

##### Subtask 3-1-1: UpdateProfileCommand 実装 (15 分)

- [ ] **ファイル作成**: `com.meatmetrics.meatmetrics.user.application.command.UpdateProfileCommand`
- [ ] **Command 基本構造**:
  ```java
  public class UpdateProfileCommand {
      private final String firstName;
      private final String lastName;
      private final String displayName;
      private final LocalDate dateOfBirth;
      private final String gender;
      private final BigDecimal heightCm;
      private final BigDecimal weightKg;
      private final String activityLevel;
      private final LocalDate carnivoreStartDate;
      private final String carnivoreLevel;
      private final String carnivoreGoal;
      private final String measurementUnit;
      private final String timezone;
      private final String avatarUrl;
  ```

##### Subtask 3-1-2: ドメインオブジェクト変換メソッド (15 分)

- [ ] **変換メソッド群**:

  ```java
  public DisplayName toDisplayName() {
      return displayName != null ? new DisplayName(displayName) : null;
  }

  public Gender toGender() {
      return gender != null ? Gender.valueOf(gender.toUpperCase()) : null;
  }

  public ActivityLevel toActivityLevel() {
      return activityLevel != null ? ActivityLevel.valueOf(activityLevel.toUpperCase()) : null;
  }

  public CarnivoreLevel toCarnivoreLevel() {
      return carnivoreLevel != null ? CarnivoreLevel.valueOf(carnivoreLevel.toUpperCase()) : null;
  }
  ```

#### ⏭️ Task 3-2: GetProfileHandler 実装 (30 分)

- [ ] **ファイル作成**: `com.meatmetrics.meatmetrics.user.application.handler.GetProfileHandler`
- [ ] **Service アノテーション**: `@Service`, `@Transactional(readOnly = true)`
- [ ] **getProfile メソッド実装**:
  ```java
  public ProfileResponse getProfile(Long userId) {
      Profile profile = profileRepository.findByUserId(userId)
          .orElseThrow(() -> new ProfileNotFoundException(userId));
      return ProfileResponse.from(profile);
  }
  ```

#### ⏭️ Task 3-3: UpdateProfileHandler 実装 (30 分)

- [ ] **ファイル作成**: `com.meatmetrics.meatmetrics.user.application.handler.UpdateProfileHandler`
- [ ] **Service アノテーション**: `@Service`, `@Transactional`
- [ ] **updateProfile メソッド実装**:

  ```java
  public ProfileResponse updateProfile(Long userId, UpdateProfileCommand command) {
      Profile profile = profileRepository.findByUserId(userId)
          .orElse(Profile.create(userId, command.toDisplayName()));

      profile.updateProfile(
          command.toDisplayName(),
          command.getFirstName(),
          command.getLastName(),
          command.getDateOfBirth(),
          command.toGender(),
          command.getHeightCm(),
          command.getWeightKg(),
          command.toActivityLevel(),
          command.getCarnivoreStartDate(),
          command.toCarnivoreLevel(),
          command.getCarnivoreGoal(),
          command.getMeasurementUnit(),
          command.getTimezone(),
          command.getAvatarUrl()
      );

      Profile saved = profileRepository.save(profile);
      return ProfileResponse.from(saved);
  }
  ```

### 🛠️ **Phase 4: Infrastructure 層実装** (90 分)

#### ⏭️ Task 4-1: ProfileEntity 実装 (30 分)

- [ ] **ファイル作成**: `com.meatmetrics.meatmetrics.user.infrastructure.persistence.ProfileEntity`
- [ ] **JPA エンティティ実装**:

  ```java
  @Entity
  @Table(name = "user_profiles")
  public class ProfileEntity {
      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      private Long id;

      @Column(name = "user_id", nullable = false, unique = true)
      private Long userId;

      @Column(name = "first_name", length = 50)
      private String firstName;

      @Column(name = "last_name", length = 50)
      private String lastName;

      @Column(name = "display_name", length = 50)
      private String displayName;

      @Column(name = "date_of_birth")
      private LocalDate dateOfBirth;

      @Enumerated(EnumType.STRING)
      @Column(name = "gender")
      private Gender gender;

      // その他フィールド...
  ```

#### ⏭️ Task 4-2: ProfileMapper 実装 (30 分)

- [ ] **ファイル作成**: `com.meatmetrics.meatmetrics.user.infrastructure.persistence.ProfileMapper`
- [ ] **Domain → Entity 変換**:

  ```java
  public ProfileEntity toEntity(Profile profile) {
      return new ProfileEntity(
          profile.getUserId(),
          profile.getFirstName(),
          profile.getLastName(),
          profile.getDisplayName() != null ? profile.getDisplayName().getValue() : null,
          profile.getDateOfBirth(),
          profile.getGender(),
          // その他フィールド...
      );
  }
  ```

- [ ] **Entity → Domain 変換**:

  ```java
  public Profile toDomain(ProfileEntity entity) {
      DisplayName displayName = entity.getDisplayName() != null ?
          new DisplayName(entity.getDisplayName()) : null;

      return new Profile(
          entity.getId(),
          entity.getUserId(),
          displayName,
          entity.getFirstName(),
          entity.getLastName(),
          // その他フィールド...
      );
  }
  ```

#### ⏭️ Task 4-3: ProfileRepository 実装 (30 分)

- [ ] **ProfileJpaRepository 作成**: `com.meatmetrics.meatmetrics.user.infrastructure.persistence.ProfileJpaRepository`

  ```java
  public interface ProfileJpaRepository extends JpaRepository<ProfileEntity, Long> {
      Optional<ProfileEntity> findByUserId(Long userId);
      void deleteByUserId(Long userId);
      boolean existsByUserId(Long userId);
  }
  ```

- [ ] **ProfileRepositoryImpl 作成**: `com.meatmetrics.meatmetrics.user.infrastructure.persistence.ProfileRepositoryJpaImpl`

  ```java
  @Repository
  public class ProfileRepositoryJpaImpl implements ProfileRepository {
      private final ProfileJpaRepository jpaRepository;
      private final ProfileMapper mapper;

      @Override
      public Optional<Profile> findByUserId(Long userId) {
          return jpaRepository.findByUserId(userId)
                  .map(mapper::toDomain);
      }

      @Override
      public Profile save(Profile profile) {
          ProfileEntity entity = mapper.toEntity(profile);
          ProfileEntity saved = jpaRepository.save(entity);
          return mapper.toDomain(saved);
      }
  }
  ```

### 🌐 **Phase 5: Web 層実装** (90 分)

#### ⏭️ Task 5-1: DTO 作成 (30 分)

##### Subtask 5-1-1: UpdateProfileRequest 実装 (15 分)

- [ ] **ファイル作成**: `com.meatmetrics.meatmetrics.api.user.dto.request.UpdateProfileRequest`
- [ ] **Bean Validation 付き DTO**:

  ```java
  public class UpdateProfileRequest {
      @Size(max = 50, message = "姓は50文字以内で入力してください")
      private String firstName;

      @Size(max = 50, message = "名は50文字以内で入力してください")
      private String lastName;

      @Size(min = 3, max = 50, message = "表示名は3-50文字で入力してください")
      private String displayName;

      @Past(message = "誕生日は過去の日付を入力してください")
      private LocalDate dateOfBirth;

      @Pattern(regexp = "male|female|other|prefer_not_to_say", message = "性別が無効です")
      private String gender;

      @DecimalMin(value = "100.0", message = "身長は100cm以上で入力してください")
      @DecimalMax(value = "250.0", message = "身長は250cm以下で入力してください")
      private BigDecimal heightCm;

      @DecimalMin(value = "30.0", message = "体重は30kg以上で入力してください")
      @DecimalMax(value = "200.0", message = "体重は200kg以下で入力してください")
      private BigDecimal weightKg;

      @Pattern(regexp = "sedentary|lightly_active|moderately_active|very_active|extremely_active", message = "活動レベルが無効です")
      private String activityLevel;

      @Past(message = "カーニボア開始日は過去の日付を入力してください")
      private LocalDate carnivoreStartDate;

      @Pattern(regexp = "beginner|intermediate|advanced", message = "カーニボアレベルが無効です")
      private String carnivoreLevel;

      @Pattern(regexp = "weight_loss|weight_gain|maintenance|health_improvement", message = "カーニボア目標が無効です")
      private String carnivoreGoal;

      @Pattern(regexp = "metric|imperial", message = "単位系が無効です")
      private String measurementUnit;

      @Size(max = 50, message = "タイムゾーンは50文字以内で入力してください")
      private String timezone;

      @URL(message = "有効なURL形式で入力してください")
      @Size(max = 255, message = "アバターURLは255文字以内で入力してください")
      private String avatarUrl;
  ```

##### Subtask 5-1-2: ProfileResponse 実装 (15 分)

- [ ] **ProfileResponse 作成**: `com.meatmetrics.meatmetrics.api.user.dto.response.ProfileResponse`
- [ ] **fromDomain メソッド**:
  ```java
  public static ProfileResponse from(Profile profile) {
      ProfileResponse response = new ProfileResponse();
      response.firstName = profile.getFirstName();
      response.lastName = profile.getLastName();
      response.displayName = profile.getDisplayName() != null ? profile.getDisplayName().getValue() : null;
      response.dateOfBirth = profile.getDateOfBirth();
      response.gender = profile.getGender() != null ? profile.getGender().getValue() : null;
      response.heightCm = profile.getHeightCm();
      response.weightKg = profile.getWeightKg();
      response.activityLevel = profile.getActivityLevel() != null ? profile.getActivityLevel().getValue() : null;
      response.carnivoreStartDate = profile.getCarnivoreStartDate();
      response.carnivoreLevel = profile.getCarnivoreLevel() != null ? profile.getCarnivoreLevel().getValue() : null;
      response.carnivoreGoal = profile.getCarnivoreGoal();
      response.measurementUnit = profile.getMeasurementUnit();
      response.timezone = profile.getTimezone();
      response.avatarUrl = profile.getAvatarUrl();
      response.updatedAt = profile.getUpdatedAt();
      return response;
  }
  ```

#### ⏭️ Task 5-2: ProfileController 実装 (60 分)

##### Subtask 5-2-1: Controller 基本構造作成 (20 分)

- [ ] **ファイル作成**: `com.meatmetrics.meatmetrics.api.user.ProfileController`
- [ ] **Controller アノテーション**:

  ```java
  @RestController
  @RequestMapping("/api/users")
  @PreAuthorize("isAuthenticated()")
  @Validated
  public class ProfileController {

      private final GetProfileHandler getProfileHandler;
      private final UpdateProfileHandler updateProfileHandler;
      private final JwtTokenService jwtTokenService;
  ```

##### Subtask 5-2-2: プロフィール取得エンドポイント実装 (20 分)

- [ ] **GET /api/users/profile エンドポイント**:

  ```java
  @GetMapping("/profile")
  public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(
          Authentication authentication) {

      Long userId = extractUserIdFromAuth(authentication);
      ProfileResponse response = getProfileHandler.getProfile(userId);

      return ResponseEntity.ok(ApiResponse.success("プロフィールを取得しました", response));
  }
  ```

##### Subtask 5-2-3: プロフィール更新エンドポイント実装 (20 分)

- [ ] **PUT /api/users/profile エンドポイント**:

  ```java
  @PutMapping("/profile")
  public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(
          @Valid @RequestBody UpdateProfileRequest request,
          Authentication authentication) {

      Long userId = extractUserIdFromAuth(authentication);
      UpdateProfileCommand command = request.toCommand();
      ProfileResponse response = updateProfileHandler.updateProfile(userId, command);

      return ResponseEntity.ok(ApiResponse.success("プロフィールを更新しました", response));
  }
  ```

### 🧪 **Phase 6: テスト実装** (90 分)

#### ⏭️ Task 6-1: ドメイン層テスト (30 分)

- [ ] **DisplayNameTest 作成**: 値オブジェクトのバリデーションテスト
- [ ] **ProfileTest 作成**: 集約ルートのビジネスロジックテスト
- [ ] **列挙型テスト**: Gender, CarnivoreLevel, ActivityLevel のテスト

#### ⏭️ Task 6-2: アプリケーション層テスト (30 分)

- [ ] **UpdateProfileHandlerTest 作成**: ハンドラーのユニットテスト
- [ ] **GetProfileHandlerTest 作成**: 取得ハンドラーのテスト

#### ⏭️ Task 6-3: Web 層テスト (30 分)

- [ ] **ProfileControllerTest 作成**: MockMvc 統合テスト
- [ ] **UpdateProfileRequestTest 作成**: Bean Validation テスト
- [ ] **ProfileResponseTest 作成**: レスポンス変換テスト

### 📋 **Phase 7: 動作確認・最終調整** (30 分)

#### ⏭️ Task 7-1: 統合動作確認 (20 分)

- [ ] **全テスト実行・Pass 確認**
- [ ] **Postman/cURL での手動確認**:
  - [ ] プロフィール取得: `GET /api/users/profile`
  - [ ] プロフィール更新: `PUT /api/users/profile`
  - [ ] エラーケースの適切なレスポンス確認

#### ⏭️ Task 7-2: ドキュメント更新 (10 分)

- [ ] **API 仕様書の最終確認**: `docs/2_detail/03_api.md`
- [ ] **実装完了のタスク管理更新**: `task_management.md`

---

## 🔗 技術仕様・前提条件

### 📚 使用技術

- **Spring Boot**: 3.x
- **Spring Web**: @RestController, @RequestMapping
- **Spring Security**: JWT 認証、@PreAuthorize
- **Bean Validation**: @Valid, @Size, @Past, @Pattern
- **JPA/Hibernate**: エンティティマッピング
- **PostgreSQL**: user_profiles テーブル

### 🏗️ 設計原則

- **DDD アーキテクチャ**: Domain → Application → Infrastructure → Web 層分離
- **Auth コンテキスト準拠**: `Email`, `Account`, `RegisterAccountHandler` パターン適用
- **値オブジェクト**: `DisplayName` で不変性・バリデーション保証
- **集約境界**: `Profile` 集約でプロフィール情報の整合性維持

### 📐 API 設計

- **RESTful API**: `/api/users/profile` リソース指向
- **HTTP メソッド**: GET（取得）、PUT（更新）
- **認証**: JWT Bearer Token 必須
- **統一レスポンス**: success/message/data/timestamp 形式

---

## ✅ 完了基準

- [ ] **プロフィール関連クラス実装完了**

  - Domain 層: `DisplayName`, `Profile`, 列挙型群
  - Application 層: `UpdateProfileCommand`, Handler 群
  - Infrastructure 層: `ProfileEntity`, `ProfileMapper`, Repository 実装
  - Web 層: `UpdateProfileRequest`, `ProfileResponse`, `ProfileController`

- [ ] **API エンドポイント実装完了**

  - `GET /api/users/profile` - プロフィール取得
  - `PUT /api/users/profile` - プロフィール更新

- [ ] **テスト実装・Pass**（カバレッジ 70%以上）

  - ドメインロジックのユニットテスト
  - Handler のユニットテスト
  - Controller の MockMvc 統合テスト

- [ ] **動作確認完了**
  - 認証付き API 呼び出し成功
  - バリデーションエラーの適切な処理
  - データベース永続化確認

---

## 🚨 注意事項・リスク対策

1. **セキュリティ**:

   - プロフィール情報の認証ユーザー分離
   - 個人情報のログ出力禁止
   - XSS 対策（表示名の特殊文字制限）

2. **データ整合性**:

   - 必須フィールドのバリデーション
   - 日付の妥当性チェック（誕生日は過去のみ）
   - 数値範囲の適切な制限

3. **パフォーマンス**:
   - プロフィール取得の効率化
   - 不要な JOIN の回避

---

**最終更新**: 2025 年 9 月 29 日  
**担当者**: 開発チーム  
**次回フォローアップ**: 明日（ユーザー目標機能との統合、GET /api/users/me 実装）
