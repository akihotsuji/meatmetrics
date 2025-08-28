package com.meatmetrics.meatmetrics;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * PostgreSQL専用テスト基底クラス
 * すべてのテストでPostgreSQL + Flywayマイグレーションを使用
 */
@Testcontainers
public abstract class PostgreSQLTestBase {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("meatmetrics_test")
            .withUsername("meatmetrics")
            .withPassword("meatmetrics123")
            .withReuse(true);

    @BeforeAll
    static void configureProperties() {
        postgres.start();
    }

    @DynamicPropertySource
    static void configureTestProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL接続設定
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
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
}
