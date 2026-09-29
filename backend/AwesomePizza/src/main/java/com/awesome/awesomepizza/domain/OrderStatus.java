package com.awesome.awesomepizza.domain;

/**
 * Enum representing the possible states of a pizza order.
 * Orders progress through these states in sequence during the pizza preparation workflow.
 */
public enum OrderStatus {
    /** Order has been created and is waiting to be assigned to a chef */
    PENDING,

    /** Order is currently being prepared by the chef */
    IN_PROGRESS,

    /** Order is finished and ready for pickup/delivery */
    READY,

    /** Order has been delivered or picked up by the customer */
    COMPLETED
}