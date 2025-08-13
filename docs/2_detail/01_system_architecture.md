# 01. System Architecture（MVP）

本書は MeatMetrics（カーニボア用栄養計算アプリ）の MVP におけるアーキテクチャを示す。拡張性を重視しつつ、構成は最小限とする。

## 1. 前提・スコープ

- 対象: MVP（食材検索、食事記録、日別サマリー、目標設定）
- 技術: Frontend = React + TypeScript、Backend = Spring Boot (Java)、DB = PostgreSQL
- 認証: JWT（メール確認なし）
- 計算: サーバーアプリ側で実施（DB のストアド/ビューは MVP で未使用）

## 2. コンテキスト図（MVP）

```mermaid
graph LR
  U["User"]
  FE["Frontend: React + TS"]
  BE["Backend: Spring Boot"]
  DB["PostgreSQL"]

  U -->|HTTPS| FE -->|REST JSON| BE -->|JDBC| DB
  BE -.JWT issuance/verify.- BE
```

## 3. 論理アーキテクチャ

```mermaid
graph TD
  subgraph Frontend
    UI["UI 層\nPages/Components"]
    SVC["Services: fetch"]
    STORE["Local State"]
  end

  subgraph Backend
    CTRL["Controllers: REST"]
    APP["Application Services"]
    DOM["Domain: Entities/Logic"]
    INF["Infrastructure: JPA/Repo"]
  end

  DB["PostgreSQL"]

  UI --> SVC --> CTRL
  CTRL --> APP --> DOM
  APP --> INF --> DB
```

## 4. デプロイ構成（開発/本番の最小）

```mermaid
graph LR
  subgraph Dev - Docker Compose
    FE_DEV["Vite Dev Server"]
    BE_DEV["Spring Boot - Dev"]
    DB_DEV["PostgreSQL 16"]
  end

  subgraph Prod
    NGINX["Nginx - TLS Termination"]
    BE_PROD["Spring Boot - Prod"]
    DB_PROD["PostgreSQL 16"]
  end

  FE_DEV --> BE_DEV --> DB_DEV
  NGINX --> BE_PROD --> DB_PROD
```

## 5. 代表ユースケースのフロー

### 5.1 ログイン

```mermaid
sequenceDiagram
  participant U as "User"
  participant FE as "Frontend"
  participant BE as "Backend"
  U->>FE: "email, password"
  FE->>BE: "POST /api/auth/login"
  BE->>BE: "verify credentials, issue JWT"
  BE-->>FE: "{accessToken}"
  FE-->>U: "ログイン完了"
```

### 5.2 食材検索

```mermaid
sequenceDiagram
  participant FE as "Frontend"
  participant BE as "Backend"
  participant DB as "PostgreSQL"
  FE->>BE: "GET /api/foods?q=&category=&tags="
  BE->>DB: "SELECT ... WHERE name ILIKE AND tags @> ..."
  DB-->>BE: "rows"
  BE-->>FE: "foods[]"
```

### 5.3 食事記録と日別サマリー

```mermaid
sequenceDiagram
  participant FE as "Frontend"
  participant BE as "Backend"
  participant DB as "PostgreSQL"
  FE->>BE: "POST /api/meals {date, items[{foodId, amount_g}]}"
  BE->>DB: "read foods, compute totals: calories, protein, fat, net_carbs"
  BE->>DB: "INSERT meal_records (items JSONB, total_nutrition JSONB)"
  DB-->>BE: "id"
  BE-->>FE: "{id, total_nutrition}"
  FE->>BE: "GET /api/summary?date=YYYY-MM-DD"
  BE->>DB: "SELECT totals, goals"
  DB-->>BE: "daily totals + goals"
  BE-->>FE: "{totals, goals, achievement}"
```

## 6. スケーラビリティ/拡張の指針（抜粋）

- DB の正規化/非正規化のバランスは MVP では JSONB 活用で柔軟性を確保
- タグは TEXT[] + GIN で簡便検索。将来はタグマスタへ切り出し可能
- 週/月集計の導入時は集計テーブル/キャッシュレイヤの検討
- メール配信/認証強化（Email 確認/2FA）は別コンポーネントで拡張

---

参照: `02_database.md`, `03_api.md`, `04_security.md`, 既存 `database_design.md`, `feature_specifications.md`
