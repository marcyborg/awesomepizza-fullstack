package com.awesome.awesomepizza.service;

import com.awesome.awesomepizza.domain.Order;
import com.awesome.awesomepizza.domain.OrderStatus;
import com.awesome.awesomepizza.repository.OrderRepository;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service class for managing pizza order business logic.
 * Handles order creation, status transitions, and queue management.
 */
@Service
public class OrderService {
    private final OrderRepository repository;

    /**
     * Constructor for OrderService.
     * 
     * @param repository the order repository dependency
     */
    public OrderService(OrderRepository repository) {
        this.repository = repository;
    }

    /**
     * Creates a new pizza order with PENDING status.
     * 
     * @param pizzaType the type of pizza to order
     * @return the created order with generated order code
     */
    public Order createOrder(String pizzaType) {
        Order order = new Order(pizzaType);
        return repository.save(order);
    }

    /**
     * Retrieves all pending orders sorted by creation time.
     * 
     * @return list of orders with PENDING status
     */
    public List<Order> getPendingOrders() {
        return repository.findAllByStatusOrderByCreatedAtAsc(OrderStatus.PENDING);
    }

    /**
     * Assigns a pending order to the chef (sets status to IN_PROGRESS).
     * Only one order can be in progress or ready at a time.
     * 
     * @param orderCode the order code to assign
     * @return the updated order
     * @throws IllegalStateException if another order is already active or order is not pending
     * @throws IllegalArgumentException if order is not found
     */
    public Order assignOrder(String orderCode) {
        List<Order> activeOrders = repository.findAllByStatusIn(
                List.of(OrderStatus.IN_PROGRESS, OrderStatus.READY)
        );
        if (!activeOrders.isEmpty()) {
            throw new IllegalStateException("There is already an order IN_PROGRESS or READY");
        }

        Order order = repository.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Order not available for assignment");
        }

        order.setStatus(OrderStatus.IN_PROGRESS);
        return repository.save(order);
    }


    /**
     * Marks an order as ready (sets status to READY).
     * Order must be in IN_PROGRESS status.
     * 
     * @param orderCode the order code to mark as ready
     * @return the updated order
     * @throws IllegalStateException if order is not in IN_PROGRESS status
     * @throws IllegalArgumentException if order is not found
     */
    public Order markReady(String orderCode) {
        Order order = repository.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new IllegalStateException("Order must be IN_PROGRESS to become READY");
        }
        order.setStatus(OrderStatus.READY);
        return repository.save(order);
    }

    /**
     * Completes an order (sets status to COMPLETED).
     * Order must be in READY status.
     * 
     * @param orderCode the order code to complete
     * @return the updated order
     * @throws IllegalStateException if order is not in READY status
     * @throws IllegalArgumentException if order is not found
     */
    public Order completeOrder(String orderCode) {
        Order order = repository.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (order.getStatus() != OrderStatus.READY) {
            throw new IllegalStateException("Order must be READY to become COMPLETED");
        }
        order.setStatus(OrderStatus.COMPLETED);
        return repository.save(order);
    }

    /**
     * Retrieves an order by its order code.
     * 
     * @param orderCode the order code to search for
     * @return the order with current status
     * @throws IllegalArgumentException if order is not found
     */
    public Order getOrderStatus(String orderCode) {
        return repository.findByOrderCode(orderCode)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
    }

    /**
     * Retrieves all orders for the chef's queue, sorted by creation time.
     * 
     * @return list of all orders ordered by creation time
     */
    public List<Order> getAllOrdersForChef() {
        return repository.findAllOrderByCreatedAtAsc();
    }

}
