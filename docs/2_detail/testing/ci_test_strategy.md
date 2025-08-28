# CI/CD テスト戦略（認証ドメイン完了時点）

## 📊 現在のテスト資産評価

### ✅ **優秀な実装済みテスト**

- `EmailTest.java`: 146 行、包括的バリデーション
- `PasswordHashTest.java`: 170 行、セキュリティ重視
- `UserTest.java`: 305 行、集約ルートの全メソッド
- `UsernameTest.java`: 152 行、ビジネスルール検証

### 📈 **カバレッジ現況**

```
ドメイン層（推定）: 85-90%
- User集約: ほぼ完全カバー
- 値オブジェクト: 全バリデーションパターン網羅
- 例外処理: 異常系含む包括テスト
```

---

## 🎯 CI/CD 段階別テスト戦略

### **Stage 1: JUnit ユニットテスト重視（現在）**

#### 品質ゲート基準

```yaml
必須要件:
  - 全ユニットテスト成功: 100%
  - ドメイン層カバレッジ: 85%以上維持
  - 値オブジェクトテスト: 全バリデーション網羅

実行タイミング:
  - 全PR作成時: 必須実行
  - main push時: 必須実行
  - 失敗時PR: マージブロック
```

#### テスト構成

```java
// 優先度HIGH（CI必須実行）
com.meatmetrics.domain.user.**
  ├── EmailTest           # バリデーション + 正規化
  ├── PasswordHashTest    # セキュリティ + BCrypt
  ├── UserTest           # 集約ルート + ビジネスロジック
  ├── UsernameTest       # ビジネスルール + 制約
  └── Exception**Test    # 全ドメイン例外

// 優先度MEDIUM（CI実行、失敗時警告）
com.meatmetrics.config.**
  └── SecurityConfigTest  # Spring Security基本設定

// 優先度LOW（CI实行可能、失敗無視）
com.meatmetrics.**
  └── ApplicationTests   # Spring Boot起動テスト
```

---

### **Stage 2: 統合テスト追加（Phase 2-3）**

#### 品質ゲート拡張

```yaml
追加要件:
  - 統合テストカバレッジ: 主要シナリオ70%
  - Testcontainers起動: 安定性確認
  - API層テスト: 認証エンドポイント全パターン

実行タイミング:
  - PR時: 統合テスト選択実行
  - main push: 全統合テスト実行
  - nightly: フルテストスイート
```

#### テスト拡張予定

```java
// 統合テスト追加予定
@DataJpaTest + Testcontainers
  ├── UserRepositoryTest     # JPA + PostgreSQL
  ├── UserGoalRepositoryTest # 複雑クエリ + 制約
  └── TransactionTest        # データ整合性

@SpringBootTest + MockMvc
  ├── AuthControllerTest     # 認証API統合
  ├── SecurityTest          # Spring Security統合
  └── ValidationTest        # リクエスト検証統合
```

---

### **Stage 3: E2E テスト（Phase 4 以降）**

#### 品質ゲート完全版

```yaml
最終要件:
  - E2Eカバレッジ: 主要ユーザーストーリー100%
  - パフォーマンス: 応答時間500ms以下
  - セキュリティ: OWASP Top 10 対応確認
```

---

## 📋 テストコマンド標準化

### Maven 実行コマンド（CI 用）

```bash
# Stage 1: 高速ユニットテスト（3-5分）
mvn clean test -B \
  -Dspring.profiles.active=test \
  -Dmaven.test.failure.ignore=false \
  -DincludeUnitTests=true \
  -DexcludeIntegrationTests=true

# Stage 2: 統合テスト追加（10-15分）
mvn clean verify -B \
  -Dspring.profiles.active=test \
  -Dtestcontainers.reuse.enable=false \
  -DincludeIntegrationTests=true

# Stage 3: フルテストスイート（20-30分）
mvn clean verify -B \
  -Dspring.profiles.active=test \
  -DincludeE2ETests=true \
  -Djacoco.destFile=target/jacoco.exec
```

### 失敗時対応ルール

```yaml
ユニットテスト失敗:
  - 即座にPRブロック
  - マージ禁止
  - 修正必須

統合テスト失敗:
  - PR警告表示
  - レビュー時要確認
  - 理由記載でマージ可能

E2Eテスト失敗:
  - nightly通知
  - 次回スプリントで対応
  - mainマージには影響なし
```

---

## 🚀 パフォーマンス目標

### CI 実行時間（Stage 別）

```
Stage 1（現在）:
  📊 Backend Unit Tests: 2-3分
  🏗️ Backend Build: 1-2分
  🎨 Frontend Check: 1-2分
  🔒 Security Scan: 2-3分
  ────────────────────────
  💡 合計: 6-10分

Stage 2（Phase 2-3）:
  追加: 統合テスト 5-8分
  💡 合計: 11-18分

Stage 3（Phase 4以降）:
  追加: E2Eテスト 10-15分
  💡 合計: 21-33分
```

### 最適化施策

```yaml
キャッシュ戦略:
  - Maven依存関係: ~/.m2/repository
  - Node.js: node_modules
  - Docker: layer caching

並列実行:
  - Backend/Frontend 並列
  - テストクラス並列実行
  - Testcontainers並列起動

テストスライシング:
  - 重要度別実行
  - 変更差分テスト
  - smoke test vs full test
```

---

## 📈 カバレッジ監視

### 目標カバレッジ（段階的）

```
Stage 1（現在）:
  ├── ドメイン層: 85%+ ✅
  ├── 値オブジェクト: 95%+ ✅
  ├── 集約ルート: 90%+ ✅
  └── 例外処理: 100% ✅

Stage 2（Phase 2-3）:
  ├── アプリケーション層: 80%+
  ├── インフラ層: 70%+
  └── Web層: 75%+

Stage 3（Phase 4以降）:
  ├── 統合シナリオ: 90%+
  ├── API コントラクト: 100%
  └── セキュリティ: 100%
```

### レポート生成

```bash
# JaCoCo coverage report
mvn jacoco:report
open target/site/jacoco/index.html

# CI用カバレッジ出力
mvn jacoco:report jacoco:check \
  -Djacoco.haltOnFailure=true \
  -Djacoco.minimumLineCoverage=0.85
```

---

## ⚙️ CI 設定最適化

### Maven 設定（.github/workflows/ci.yml 用）

```yaml
env:
  MAVEN_OPTS: >-
    -Dmaven.repo.local=.m2/repository
    -Xmx1024m
    -XX:+UseG1GC
    -Djava.awt.headless=true

test-args: >-
  -B 
  -Dspring.profiles.active=test
  -Dmaven.test.failure.ignore=false
  -Duser.timezone=UTC
  -Djunit.jupiter.execution.parallel.enabled=true
```

### Spring Test 設定

```properties
# src/test/resources/application-test.properties（CI用）
spring.jpa.hibernate.ddl-auto=create-drop
spring.datasource.url=jdbc:h2:mem:testdb
spring.test.database.replace=none
logging.level.org.springframework.test=WARN
logging.level.com.meatmetrics=DEBUG
```

---

**更新履歴**:

- 2025-08-17: 認証ドメイン完了時点での初期戦略策定
- 予定: Phase 2 完了時の統合テスト戦略更新
