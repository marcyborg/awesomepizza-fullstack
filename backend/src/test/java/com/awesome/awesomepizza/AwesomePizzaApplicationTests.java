package com.awesome.awesomepizza;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * Integration tests for AwesomePizza application.
 * Verifies that the Spring Boot application context loads correctly.
 */
@SpringBootTest
@ActiveProfiles("h2")
class AwesomePizzaApplicationTests {

	/**
	 * Tests that the Spring application context loads without errors.
	 * This is a smoke test to verify basic application configuration.
	 */
	@Test
	void contextLoads() {
	}

}
