package com.awesome.awesomepizza.dto;

/**
 * Response DTO for pizza order creation.
 * 
 * @param orderCode the generated unique order code (e.g., "ORD-AB12CD34")
 */
public record OrderResponse(String orderCode) {}
