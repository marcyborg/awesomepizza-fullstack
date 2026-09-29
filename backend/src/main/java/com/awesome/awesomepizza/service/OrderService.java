package com.awesome.awesomepizza.service;

import com.awesome.awesomepizza.domain.Order;
import com.awesome.awesomepizza.domain.OrderStatus;
import com.awesome.awesomepizza.repository.ChefStationRepository;
import com.awesome.awesomepizza.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class OrderService {
    private final OrderRepository repository;
    private final ChefStationRepository chefStationRepository;

    public OrderService(OrderRepository repository, ChefStationRepository chefStationRepository) {
        this.repository = repository;
        this.chefStationRepository = chefStationRepository;
    }

    public Order createOrder(String pizzaType) {
        if (pizzaType == null || pizzaType.isBlank() || pizzaType.length() > 255) {
            throw new IllegalArgumentException("Pizza type is required and must be at most 255 characters");
        }
        return repository.save(new Order(pizzaType.trim()));
    }

    @Transactional(readOnly = true)
    public List<Order> getPendingOrders() {
        return repository.findAllByStatusOrderByCreatedAtAsc(OrderStatus.PENDING);
    }

    @Transactional
    public Order assignOrder(String orderCode) {
        lockStation();
        Order order = findOrder(orderCode);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OrderConflictException("Order is not available for assignment");
        }
        if (!repository.findAllByStatusIn(List.of(OrderStatus.IN_PROGRESS, OrderStatus.READY)).isEmpty()) {
            throw new OrderConflictException("There is already an active order");
        }
        order.setStatus(OrderStatus.IN_PROGRESS);
        return repository.saveAndFlush(order);
    }

    @Transactional
    public Order markReady(String orderCode) {
        lockStation();
        Order order = findOrder(orderCode);
        if (order.getStatus() != OrderStatus.IN_PROGRESS) {
            throw new OrderConflictException("Order must be IN_PROGRESS to become READY");
        }
        order.setStatus(OrderStatus.READY);
        return repository.save(order);
    }

    @Transactional
    public Order completeOrder(String orderCode) {
        lockStation();
        Order order = findOrder(orderCode);
        if (order.getStatus() != OrderStatus.READY) {
            throw new OrderConflictException("Order must be READY to become COMPLETED");
        }
        order.setStatus(OrderStatus.COMPLETED);
        return repository.save(order);
    }

    @Transactional(readOnly = true)
    public Order getOrderStatus(String orderCode) {
        return findOrder(orderCode);
    }

    @Transactional(readOnly = true)
    public List<Order> getAllOrdersForChef() {
        return repository.findAllOrderByCreatedAtAsc();
    }

    private Order findOrder(String orderCode) {
        return repository.findByOrderCode(orderCode).orElseThrow(OrderNotFoundException::new);
    }

    private void lockStation() {
        if (chefStationRepository.lockStation(1L) == null) {
            throw new IllegalStateException("Chef station is unavailable");
        }
    }
}
