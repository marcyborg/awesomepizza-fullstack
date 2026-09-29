package com.awesome.awesomepizza.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a new pizza order.
 *
 * @param pizzaType the type of pizza to order (e.g., "Margherita", "Tuna")
 */
public record OrderRequest(@NotBlank @Size(max = 255) String pizzaType) {}
