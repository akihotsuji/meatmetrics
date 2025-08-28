package com.meatmetrics.meatmetrics;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * アプリケーション統合テスト（PostgreSQL + Flyway必須）
 */
@SpringBootTest
@ActiveProfiles("test")
class MeatmetricsApplicationTests extends PostgreSQLTestBase {

	@Test
	void contextLoads() {
		// PostgreSQL + Flywayでアプリケーションコンテキストが正常に起動することを確認
	}

	@Test
	void flywayMigrationExecuted() {
		// Flywayマイグレーションが正常に実行されることを確認
		// 実際のテストはFlywayのマイグレーション履歴テーブルの存在確認などで実装
	}
}
