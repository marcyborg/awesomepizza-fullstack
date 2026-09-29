package com.awesome.awesomepizza;

import com.awesome.awesomepizza.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.RepeatedTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderApiIntegrationTest {
    @Autowired TestRestTemplate http;
    @Autowired OrderRepository repository;

    @BeforeEach
    void clearOrders() {
        repository.deleteAll();
    }

    @Test
    void rejectsInvalidTypesAndReturnsStructuredErrors() {
        for (Map<String, String> payload : List.<Map<String, String>>of(
                Map.of(), java.util.Collections.singletonMap("pizzaType", null),
                Map.of("pizzaType", "   "), Map.of("pizzaType", "x".repeat(256)))) {
            ResponseEntity<Map> response = http.postForEntity("/api/orders", payload, Map.class);
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertEquals(400, response.getBody().get("status"));
            assertNotNull(response.getBody().get("detail"));
        }
        ResponseEntity<Map> missing = http.getForEntity("/api/orders/ORD-MISSING", Map.class);
        assertEquals(HttpStatus.NOT_FOUND, missing.getStatusCode());
        assertEquals(404, missing.getBody().get("status"));
        String code = create("Margherita");
        ResponseEntity<Map> conflict = http.exchange("/api/orders/" + code + "/ready",
                HttpMethod.PUT, new HttpEntity<>(Map.of()), Map.class);
        assertEquals(HttpStatus.CONFLICT, conflict.getStatusCode());
        assertEquals(409, conflict.getBody().get("status"));
    }

    @RepeatedTest(5)
    void onlyOneConcurrentAssignmentSucceeds() throws Exception {
        String first = create("Margherita");
        String second = create("Funghi");
        try (var executor = Executors.newFixedThreadPool(2)) {
            CountDownLatch start = new CountDownLatch(1);
            Future<ResponseEntity<Map>> left = executor.submit(() -> assign(first, start));
            Future<ResponseEntity<Map>> right = executor.submit(() -> assign(second, start));
            start.countDown();
            List<Integer> statuses = List.of(left.get(10, java.util.concurrent.TimeUnit.SECONDS).getStatusCode().value(),
                    right.get(10, java.util.concurrent.TimeUnit.SECONDS).getStatusCode().value());
            assertTrue(statuses.containsAll(List.of(200, 409)), "Expected 200 and 409, got " + statuses);
        }
        long active = repository.findAll().stream()
                .filter(order -> order.getStatus().name().equals("IN_PROGRESS")).count();
        assertEquals(1, active);
    }

    @Test
    void transitionsAreAtomicAndReadyOrdersKeepTheStationOccupied() {
        String first = create(" Margherita ");
        String second = create("Funghi");
        assertEquals(40, first.length());
        assertNotEquals(first, second);
        assertEquals("Margherita", http.getForObject("/api/orders/" + first, Map.class).get("pizzaType"));
        assertEquals(HttpStatus.OK, transition(first, "assign").getStatusCode());
        assertEquals(HttpStatus.CONFLICT, transition(first, "assign").getStatusCode());
        assertEquals(HttpStatus.CONFLICT, transition(first, "complete").getStatusCode());
        assertEquals(HttpStatus.OK, transition(first, "ready").getStatusCode());
        assertEquals(HttpStatus.CONFLICT, transition(second, "assign").getStatusCode());
        assertEquals(HttpStatus.OK, transition(first, "complete").getStatusCode());
        assertEquals(HttpStatus.CONFLICT, transition(first, "complete").getStatusCode());
        assertEquals(HttpStatus.OK, transition(second, "assign").getStatusCode());
    }

    @Test
    void allMutationsReturn404ForMissingOrders() {
        for (String action : List.of("assign", "ready", "complete")) {
            assertEquals(HttpStatus.NOT_FOUND, transition("ORD-MISSING", action).getStatusCode());
        }
    }

    @Test
    void malformedJsonReturnsProblemDetails() {
        var headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
        ResponseEntity<Map> response = http.postForEntity("/api/orders",
                new HttpEntity<>("{bad-json", headers), Map.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(400, response.getBody().get("status"));
    }

    private ResponseEntity<Map> transition(String code, String action) {
        return http.exchange("/api/orders/" + code + "/" + action, HttpMethod.PUT,
                new HttpEntity<>(Map.of()), Map.class);
    }

    private ResponseEntity<Map> assign(String code, CountDownLatch start) throws InterruptedException {
        start.await();
        return http.exchange("/api/orders/" + code + "/assign",
                HttpMethod.PUT, new HttpEntity<>(Map.of()), Map.class);
    }

    private String create(String pizzaType) {
        ResponseEntity<Map> response = http.postForEntity("/api/orders",
                Map.of("pizzaType", pizzaType), Map.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        return (String) response.getBody().get("orderCode");
    }
}
