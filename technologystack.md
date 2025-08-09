## 技術スタック

### プロジェクト概要

- **目的**: Spring Boot（Java）＋ React による Web アプリケーション開発
- **構成**: フロントエンド（SPA）／バックエンド（REST API）／RDBMS（PostgreSQL）／インフラ（AWS）
- **開発環境**: Windows（WSL 併用可）

---

## コア技術

- **言語（Backend）**: Java 17
- **言語（Frontend）**: TypeScript ^5.x
- **パッケージ管理**: Maven（Backend）, npm（Frontend）

---

## フロントエンド

- **フレームワーク**: React 19.x
- **ビルドツール**: Vite 5.x
- **UI**: Tailwind CSS 3.x（必須）, shadcn/ui 2.x（推奨・初期加速用）
- **ルーティング**: React Router 6.x
- **フォーム**: React Hook Form 7.x + Zod 3.x
- **サーバー状態**: TanStack Query 5.x（必須）
- **グローバル状態**: Zustand（推奨・軽量・学習コスト低）
- **テスト**: Vitest + Testing Library + Playwright（E2E）
- **Lint/Format**: ESLint 9.x, Prettier
- **品質**: Husky + lint-staged, Commitlint（Conventional Commits）

---

## バックエンド

- **フレームワーク**: Spring Boot 3.x
- **主要スターター**:
  - Spring Web, Spring Data JPA, Spring Validation
  - Spring Security（JWT/OAuth2）
  - Spring Actuator（監視）
- **API ドキュメント**: springdoc-openapi + Swagger UI
- **永続化**: JPA/Hibernate
- **DB マイグレーション**: Flyway
- **テスト**: JUnit 5, Mockito, Testcontainers（PostgreSQL）
- **ビルド/実行**: Maven Wrapper（`mvnw`）

---

## データベース

- **RDBMS**: PostgreSQL 16.x
- **接続**: HikariCP
- **開発用**: Testcontainers（Docker Desktop 前提）
- **インデックス / パフォーマンス**: 必要に応じて設計時に記載
- **バックアップ**: RDS 自動バックアップ（インフラ側で設定）

---

## インフラ（AWS）

- **ホスティング**:
  - Backend: ECS Fargate
  - Frontend: S3 + CloudFront
  - DB: Amazon RDS for PostgreSQL
- **ネットワーク**: VPC, Subnet（Public/Private）, ALB, Security Group, Route 53
- **認証/ID 管理**: Amazon Cognito
- **シークレット管理**: AWS Secrets Manager
- **監視/ログ**: CloudWatch Logs/Metrics
- **IaC**: Terraform
- **コンテナレジストリ**: Amazon ECR

---

## CI/CD

- **CI**: GitHub Actions
  - Lint, Test, Build（Frontend/Backend）
  - Docker イメージ Build & Push（ECR）
- **CD**: GitHub Actions → ECS デプロイ
- **Infra**: Terraform Plan/Apply（環境別ワークスペース）

---

## セキュリティ

- **認証/認可**: Spring Security（JWT/OAuth2）
- **入力検証**: Bean Validation（Backend）, Zod（Frontend）
- **CORS/CSRF**: CORS 設定（REST）, CSRF 保護（必要に応じて）

---

## 環境構成

- **環境**: local / dev / stg / prod
- **設定**:
  - Backend: `application-{profile}.properties`
  - Frontend: `.env.{mode}`（Vite）
- **バージョン戦略**: LTS 優先、セマンティックバージョニング採用

---

## コーディング規約・運用

- **命名規約**: チーム標準（Java/TypeScript の一般規約）
- **コードスタイル**: EditorConfig + Prettier
- **コミット規約**: Conventional Commits
- **ブランチ戦略**: GitHub Flow

---

## 開発環境セットアップ

- **必須ソフトウェア**:
  - Java 21 (OpenJDK)
  - Node.js 18+ / npm
  - Docker Desktop（Windows）
  - Git
  - IDE: IntelliJ IDEA / VS Code
- **推奨ツール**:
  - Postman / Insomnia（API テスト）
  - pgAdmin / DBeaver（DB 管理）
  - AWS CLI（インフラ操作）

---

**最終更新**: 2024 年 12 月
**更新者**: 開発チーム
