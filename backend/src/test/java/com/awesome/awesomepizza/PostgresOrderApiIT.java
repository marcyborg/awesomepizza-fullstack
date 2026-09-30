package com.awesome.awesomepizza;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/** Re-runs the full API regression suite, including concurrency, on PostgreSQL. */
@ActiveProfiles(value = "postgres", inheritProfiles = false)
class PostgresOrderApiIT extends OrderApiIntegrationTest {
    @DynamicPropertySource
    static void postgresProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", PostgresTestDatabase::url);
        registry.add("spring.datasource.username", PostgresTestDatabase::user);
        registry.add("spring.datasource.password", PostgresTestDatabase::password);
    }
}
