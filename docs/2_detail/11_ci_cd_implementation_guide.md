# CI/CD 実装ガイド（認証ドメイン完了時点）

## 🎯 実装概要

認証ドメインモデル実装完了を受けて、**JUnit 重視の段階的 CI/CD**を導入します。Docker 開発環境[[memory:6416796]]を活用し、段階的に機能を拡張していく戦略を採用します。

---

## 🚀 即座実装：基盤 CI 構築

### Step 1: GitHub Actions ワークフロー有効化

```bash
# 1. ワークフローファイルの配置確認
ls -la .github/workflows/ci.yml

# 2. GitHubリポジトリでActions有効化
# GitHub Web UI: Settings > Actions > General > "Allow all actions"

# 3. 初回実行テスト（プッシュまたはPR作成）
git add .github/workflows/ci.yml
git commit -m "feat: 認証ドメイン対応CI/CD基盤構築

- JUnit 5 + Spring Boot Test自動実行
- Maven依存関係キャッシュ最適化
- PR品質ゲート設定（テスト必須通過）
- 段階的拡張設計（Phase 2-4対応準備）"
git push origin feature/cicd-setup
```

### Step 2: CI 実行確認と調整

```bash
# GitHub Actions実行状況確認
# Web UI: Actions タブで実行ログ確認

# ローカルでCI環境再現（デバッグ用）
cd backend
mvn clean test -B \
  -Dspring.profiles.active=test \
  -Dmaven.test.failure.ignore=false

# 期待結果の確認
echo "
✅ 期待される実行結果:
- EmailTest: 146行すべて成功
- PasswordHashTest: 170行すべて成功
- UserTest: 305行すべて成功
- UsernameTest: 152行すべて成功
- ビルド成功: target/*.jar生成確認
"
```

---

## 📋 段階的拡張ロードマップ

### Phase 2: 認証システム完了時（2025 年 1 月上旬）

#### 追加予定要素

```yaml
統合テスト追加:
  - UserRepositoryTest: @DataJpaTest + Testcontainers
  - AuthControllerTest: @SpringBootTest + MockMvc
  - JWT統合テスト: JwtService + Spring Security

実装コマンド:
  mvn clean verify -DincludeIntegrationTests=true
```

#### CI 拡張設定

```yaml
# .github/workflows/ci.yml 追加ジョブ
integration-test:
  needs: backend-test
  services:
    postgres:
      image: postgres:16
      env:
        POSTGRES_PASSWORD: testpass
      options: >-
        --health-cmd pg_isready
        --health-interval 10s
        --health-timeout 5s
        --health-retries 5
```

### Phase 3-4: 食材・食事システム完了時（2025 年 1 月下旬）

#### 本格 CI/CD 導入

```yaml
追加要素:
  - Docker: マルチステージビルド
  - Security: 依存関係脆弱性スキャン
  - Performance: レスポンス時間監視
  - Deployment: staging環境自動デプロイ
```

---

## 🔧 設定詳細

### Maven 設定最適化

```xml
<!-- pom.xml に追加推奨（Phase 2以降） -->
<plugin>
  <groupId>org.jacoco</groupId>
  <artifactId>jacoco-maven-plugin</artifactId>
  <version>0.8.8</version>
  <executions>
    <execution>
      <goals>
        <goal>prepare-agent</goal>
      </goals>
    </execution>
    <execution>
      <id>report</id>
      <phase>test</phase>
      <goals>
        <goal>report</goal>
      </goals>
    </execution>
    <execution>
      <id>check</id>
      <goals>
        <goal>check</goal>
      </goals>
      <configuration>
        <rules>
          <rule>
            <element>CLASS</element>
            <limits>
              <limit>
                <counter>LINE</counter>
                <value>COVEREDRATIO</value>
                <minimum>0.85</minimum>
              </limit>
            </limits>
          </rule>
        </rules>
      </configuration>
    </execution>
  </executions>
</plugin>
```

### GitHub Secrets 設定（Phase 2 以降）

```bash
# GitHub Web UI: Settings > Secrets and variables > Actions

# Phase 2で必要
JWT_SECRET: "your-jwt-secret-key-here"
DB_PASSWORD: "your-test-db-password"

# Phase 3-4で必要
DOCKER_USERNAME: "your-docker-hub-username"
DOCKER_PASSWORD: "your-docker-hub-token"
AWS_ACCESS_KEY_ID: "your-aws-access-key"
AWS_SECRET_ACCESS_KEY: "your-aws-secret-key"
```

---

## 🧪 テスト実行パターン

### 開発者ローカル実行

```bash
# 高速テスト（日常開発用）
mvn test -Dtest="*Test" -DfailIfNoTests=false

# 認証ドメインのみ実行
mvn test -Dtest="com.meatmetrics.domain.user.*Test"

# 包括テスト（PR前）
mvn clean verify -B

# CI環境再現
mvn clean test -B \
  -Dspring.profiles.active=test \
  -Dmaven.test.failure.ignore=false \
  -Djunit.jupiter.execution.parallel.enabled=true
```

### CI 環境での実行パターン

```bash
# PR時: 高速フィードバック（5-8分）
- backend-test: ユニットテストのみ
- backend-build: JARビルド確認
- frontend-check: 基本チェックのみ

# main push時: 安定性確認（8-12分）
- 上記 + security-scan
- アーティファクト保存
- 通知システム連携

# nightly: 完全検証（Phase 2以降、15-30分）
- 統合テスト全実行
- パフォーマンステスト
- セキュリティフルスキャン
```

---

## 📊 品質メトリクス監視

### 現在のベースライン（認証ドメイン）

```bash
# カバレッジ測定
mvn jacoco:report
open target/site/jacoco/index.html

# 期待カバレッジ
echo "
🎯 認証ドメイン現在値:
- Email: 95%+
- PasswordHash: 98%+
- User: 92%+
- Username: 96%+
- 全体平均: 90%+
"
```

### CI 監視アラート設定

```yaml
# GitHub Actions通知設定
カバレッジ低下: 85%未満でSlack通知
ビルド失敗: 即座にメール通知
テスト実行時間: 10分超過で警告
依存関係脆弱性: HIGH以上で即座通知
```

---

## 🔍 トラブルシューティング

### よくある問題と対処法

#### 1. Maven 依存関係の問題

```bash
# キャッシュクリア
mvn dependency:purge-local-repository
mvn clean compile

# 依存関係ツリー確認
mvn dependency:tree
```

#### 2. テスト実行エラー

```bash
# 詳細ログ有効化
mvn test -X -Dspring.profiles.active=test

# Spring Bootテスト専用
mvn test -Dtest="*SpringBootTest" \
  -Dspring.test.context.cache.maxSize=1
```

#### 3. CI 実行時間超過

```bash
# 並列実行有効化
mvn test -Djunit.jupiter.execution.parallel.enabled=true \
  -Djunit.jupiter.execution.parallel.mode.default=concurrent

# 不要テスト除外
mvn test -DexcludeGroups="slow,integration"
```

#### 4. Docker 環境での問題 [[memory:6416796]]

```bash
# CI環境でのDocker利用は避け、ネイティブ実行を推奨
# 開発時のみDocker Compose使用
docker-compose -f infrastructure/docker/dev/docker-compose.yml up -d postgres
```

---

## ✅ 検証チェックリスト

### CI 導入完了確認

```markdown
□ .github/workflows/ci.yml 配置完了
□ GitHub Actions 有効化完了
□ 初回 PR 作成と CI 実行成功
□ 全ユニットテスト成功（4 テストクラス）
□ Maven 依存関係キャッシュ動作確認
□ アーティファクト生成確認（JAR）
□ 失敗時の PR ブロック動作確認
□ テスト結果レポート表示確認
```

### Phase 2 準備確認

```markdown
□ JaCoCo 設定準備完了
□ Testcontainers 依存関係追加準備
□ GitHub Secrets 設定準備
□ 統合テスト実行環境準備
□ CI 実行時間ベースライン計測完了
```

---

## 📞 サポート・エスカレーション

### 導入支援

```bash
# 実装中の問題は以下で対応
1. GitHub Issues: 技術的な問題
2. Discussion: 設計・戦略相談
3. Wiki: 設定例とベストプラクティス

# 緊急時対応
CI完全停止: ワークフロー無効化 -> 手動テスト実行
テスト大量失敗: 特定テストクラス一時除外
パフォーマンス劣化: 並列実行無効化で安定化
```

---

**実装責任者**: 開発チーム  
**完了予定**: 2025 年 8 月 18 日  
**次回レビュー**: Phase 2 認証システム完了時（2025 年 1 月上旬）
