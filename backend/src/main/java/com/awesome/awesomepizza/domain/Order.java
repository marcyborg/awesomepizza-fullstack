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

    @Column(nullable = false, length = 255)
    private String pizzaType;
    @Enumerated(EnumType.STRING)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false, unique = true, length = 40)
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
        this.orderCode = "ORD-" + UUID.randomUUID().toString().toUpperCase();
    }
}
