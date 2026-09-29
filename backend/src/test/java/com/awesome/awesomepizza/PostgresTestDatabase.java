package com.awesome.awesomepizza;

import java.util.Objects;

/** Test fixtures must never point to the application's live database. */
final class PostgresTestDatabase {
    private PostgresTestDatabase() {}

    static String url() {
        String url = required("POSTGRES_IT_URL");
        if (!url.matches("jdbc:postgresql://[^/]+/awesomepizza_test(?:\\?.*)?")) {
            throw new IllegalArgumentException("PostgreSQL tests require a database named awesomepizza_test");
        }
        return url;
    }

    static String user() {
        return required("POSTGRES_IT_USER");
    }

    static String password() {
        return required("POSTGRES_IT_PASSWORD");
    }

    private static String required(String key) {
        return Objects.requireNonNull(System.getenv(key), "Missing test environment variable " + key);
    }
}
