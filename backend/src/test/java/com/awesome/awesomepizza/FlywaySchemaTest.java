package com.awesome.awesomepizza;

import com.awesome.awesomepizza.service.OrderService;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("h2")
class FlywaySchemaTest {
    @Autowired Flyway flyway;
    @Autowired JdbcTemplate jdbc;
    @Autowired OrderService orders;

    @Test
    void migrationsAndChefSeedAreAppliedExactlyOnce() {
        assertEquals(2, flyway.info().applied().length);
        assertEquals(0, flyway.migrate().migrationsExecuted);
        assertEquals(1L, jdbc.queryForObject("select count(*) from chef_station", Long.class));
    }

    @Test
    void databaseRejectsBlankPizzaAndUnknownStatuses() {
        assertThrows(DataIntegrityViolationException.class, () -> insert("   ", "PENDING", "ORD-INVALID"));
        assertThrows(DataIntegrityViolationException.class, () -> insert("Margherita", "UNKNOWN", "ORD-INVALID"));
        assertThrows(DataIntegrityViolationException.class,
                () -> jdbc.update("insert into chef_station(id) values (2)"));
    }

    @Test
    void databaseEnforcesUniquePublicOrderCodes() {
        var order = orders.createOrder("Unique code test");
        assertThrows(DataIntegrityViolationException.class,
                () -> insert("Funghi", "PENDING", order.getOrderCode()));
    }

    private void insert(String pizza, String status, String code) {
        jdbc.update("insert into pizza_order(id,pizza_type,status,order_code,created_at) "
                        + "values (?,?,?,?,CURRENT_TIMESTAMP)",
                UUID.randomUUID(), pizza, status, code);
    }
}
