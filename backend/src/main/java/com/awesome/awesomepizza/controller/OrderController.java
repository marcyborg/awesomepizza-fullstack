package com.awesome.awesomepizza.controller;

import io.swagger.v3.oas.annotations.Operation;
import com.awesome.awesomepizza.domain.Order;
import com.awesome.awesomepizza.dto.OrderResponse;
import com.awesome.awesomepizza.service.OrderService;
import com.awesome.awesomepizza.dto.OrderRequest;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing pizza orders.
 * Provides endpoints for creating orders, checking status, and managing the chef's queue.
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {
    private final OrderService service;

    /**
     * Constructor for OrderController.
     * 
     * @param service the order service dependency
     */
    public OrderController(OrderService service) {
        this.service = service;
    }

    /**
     * Creates a new pizza order.
     * 
     * @param request the order request containing pizza type
     * @return order response with generated order code
     */
    @PostMapping
    @Operation(summary = "Create new pizza order")
    public OrderResponse createOrder(@RequestBody OrderRequest request) {
        Order order = service.createOrder(request.pizzaType());
        return new OrderResponse(order.getOrderCode());
    }

    /**
     * Retrieves all orders for the pizza chef's queue.
     * 
     * @return list of orders ordered by creation time
     */
    @GetMapping("/queue")
    @Operation(summary = "Orders queue for pizza chef")
    public List<Order> getQueue() {
        return service.getAllOrdersForChef();
    }

    /**
     * Gets the status of a specific order by its code.
     * 
     * @param code the order code
     * @return the order with current status
     */
    @GetMapping("/{code}")
    @Operation(summary = "Status order")
    public Order getStatus(@PathVariable String code) {
        return service.getOrderStatus(code);
    }

    /**
     * Assigns an order to the chef (sets status to IN_PROGRESS).
     * Only one order can be in progress at a time.
     * 
     * @param code the order code
     * @return the updated order
     */
    @PutMapping("/{code}/assign")
    @Operation(summary = "Take charge")
    public Order assign(@PathVariable String code) {
        return service.assignOrder(code);
    }

    /**
     * Marks an order as ready (sets status to READY).
     * Order must be in IN_PROGRESS status.
     * 
     * @param code the order code
     * @return the updated order
     */
    @PutMapping("/{code}/ready")
    @Operation(summary = "Order ready")
    public Order ready(@PathVariable String code) {
        return service.markReady(code);
    }

    /**
     * Completes an order (sets status to COMPLETED).
     * Order must be in READY status.
     * 
     * @param code the order code
     * @return the updated order
     */
    @PutMapping("/{code}/complete")
    @Operation(summary = "Order completed")
    public Order complete(@PathVariable String code) {
        return service.completeOrder(code);
    }
}
