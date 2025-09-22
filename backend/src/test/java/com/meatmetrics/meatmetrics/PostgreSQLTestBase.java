package com.meatmetrics.meatmetrics;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * PostgreSQL専用テスト基底クラス
 * 
 * <p>環境に応じて接続先を切替：</p>
 * <ul>
 *   <li>デフォルト: Testcontainers(PostgreSQL) を起動し接続</li>
 *   <li>環境変数 TEST_DB_MODE=compose の場合: 既存の docker-compose の postgres へ直結</li>
 * </ul>
 */
public abstract class PostgreSQLTestBase {

    private static final boolean USE_COMPOSE_DB =
        "compose".equalsIgnoreCase(System.getenv("TEST_DB_MODE"));

    private static PostgreSQLContainer<?> postgres;

    @BeforeAll
    @SuppressWarnings("resource")
    static void configureProperties() {
        if (!USE_COMPOSE_DB) {
            postgres = new PostgreSQLContainer<>("postgres:16")
                .withDatabaseName("meatmetrics_test")
                .withUsername("meatmetrics")
                .withPassword("meatmetrics123")
                .withReuse(true);
            postgres.start();
        }
    }

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL接続設定
        if (USE_COMPOSE_DB) {
            // docker-compose の postgres サービスへ直結
            registry.add("spring.datasource.url", () -> "jdbc:postgresql://postgres:5432/meatmetrics");
            registry.add("spring.datasource.username", () -> "meatmetrics");
            registry.add("spring.datasource.password", () -> "meatmetrics123");
        } else {
            registry.add("spring.datasource.url", postgres::getJdbcUrl);
            registry.add("spring.datasource.username", postgres::getUsername);
            registry.add("spring.datasource.password", postgres::getPassword);
        }
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");

        // JPA/Hibernate設定（Flyway管理のためDDL無効）
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");

        // Flyway設定（必須）
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
        registry.add("spring.flyway.baseline-on-migrate", () -> "false");
        registry.add("spring.flyway.validate-on-migrate", () -> "true");
        registry.add("spring.flyway.clean-disabled", () -> "false");
    }

    @AfterAll
    static void tearDown() {
        if (!USE_COMPOSE_DB && postgres != null) {
            try {
                postgres.stop();
            } catch (Exception ignored) {
            }
        }
    }
}
