package com.awesome.awesomepizza.dto;

/**
 * Request DTO for creating a new pizza order.
 * 
 * @param pizzaType the type of pizza to order (e.g., "Margherita", "Tuna")
 */
public record OrderRequest(String pizzaType) {}
