package com.awesome.awesomepizza.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a pizza order.
 * Stores order information including pizza type, status, and timestamps.
 */
@Entity
@Table(name = "pizza_order")
@Getter
@Setter
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String pizzaType;
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    private String orderCode;
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Default constructor for JPA.
     */
    public Order() {}

    /**
     * Constructor for creating a new order with specified pizza type.
     * Automatically generates an order code and sets creation time.
     * 
     * @param pizzaType the type of pizza for this order
     */
    public Order(String pizzaType) {
        this.pizzaType = pizzaType;
        this.orderCode = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
