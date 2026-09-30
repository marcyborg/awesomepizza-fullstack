package com.awesome.awesomepizza;

import com.awesome.awesomepizza.domain.Order;
import com.awesome.awesomepizza.domain.OrderStatus;
import com.awesome.awesomepizza.service.OrderService;
import com.awesome.awesomepizza.service.OrderConflictException;
import com.awesome.awesomepizza.repository.OrderRepository;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.junit.jupiter.api.Assertions.*;

class PostgresPersistenceIT {
    @Test
    void twoApplicationInstancesCannotAssignTwoOrdersAtOnce() throws Exception {
        try (ConfigurableApplicationContext first = start();
             ConfigurableApplicationContext second = start();
             var executor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            first.getBean(OrderRepository.class).deleteAll();
            OrderService firstService = first.getBean(OrderService.class);
            OrderService secondService = second.getBean(OrderService.class);
            String leftCode = firstService.createOrder("First instance").getOrderCode();
            String rightCode = secondService.createOrder("Second instance").getOrderCode();
            var start = new java.util.concurrent.CountDownLatch(1);
            var left = executor.submit(() -> assign(firstService, leftCode, start));
            var right = executor.submit(() -> assign(secondService, rightCode, start));
            start.countDown();
            boolean leftWon = left.get(10, java.util.concurrent.TimeUnit.SECONDS);
            boolean rightWon = right.get(10, java.util.concurrent.TimeUnit.SECONDS);
            assertNotEquals(leftWon, rightWon, "Exactly one instance must succeed");
            assertEquals(1, firstService.getAllOrdersForChef().stream()
                    .filter(order -> order.getStatus() == OrderStatus.IN_PROGRESS).count());
        }
    }

    @Test
    void migrationsRunOnceAndAnOrderSurvivesApplicationRestart() {
        String code;
        int applied;
        try (ConfigurableApplicationContext first = start()) {
            Flyway flyway = first.getBean(Flyway.class);
            flyway.validate();
            applied = flyway.info().applied().length;
            assertEquals(2, applied);
            JdbcTemplate jdbc = first.getBean(JdbcTemplate.class);
            assertEquals("awesomepizza_test", jdbc.queryForObject("select current_database()", String.class));
            assertEquals(1L, jdbc.queryForObject("select count(*) from chef_station where id = 1", Long.class));
            Order order = first.getBean(OrderService.class).createOrder("Persistence regression");
            code = order.getOrderCode();
        }
        try (ConfigurableApplicationContext second = start()) {
            Order stored = second.getBean(OrderService.class).getOrderStatus(code);
            assertEquals("Persistence regression", stored.getPizzaType());
            assertEquals(OrderStatus.PENDING, stored.getStatus());
            assertEquals(applied, second.getBean(Flyway.class).info().applied().length);
            second.getBean(Flyway.class).validate();
        }
    }

    private ConfigurableApplicationContext start() {
        return new SpringApplicationBuilder(AwesomePizzaApplication.class)
                .web(WebApplicationType.NONE)
                .profiles("postgres")
                .run("--spring.datasource.url=" + PostgresTestDatabase.url(),
                        "--spring.datasource.username=" + PostgresTestDatabase.user(),
                        "--spring.datasource.password=" + PostgresTestDatabase.password());
    }

    private boolean assign(OrderService service, String code,
                           java.util.concurrent.CountDownLatch start) throws InterruptedException {
        start.await();
        try {
            service.assignOrder(code);
            return true;
        } catch (OrderConflictException expected) {
            return false;
        }
    }
}
