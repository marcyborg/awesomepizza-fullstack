package com.awesome.awesomepizza;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for AwesomePizza backend.
 * Spring Boot application for managing pizza orders with REST APIs.
 */
@SpringBootApplication
public class AwesomePizzaApplication {

	/**
	 * Main method to start the AwesomePizza application.
	 * 
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		SpringApplication.run(AwesomePizzaApplication.class, args);
	}

}
