package com.awesome.awesomepizza.repository;

import com.awesome.awesomepizza.domain.Order;
import com.awesome.awesomepizza.domain.OrderStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Order entity operations.
 * Provides data access methods for pizza order management.
 */
public interface OrderRepository extends JpaRepository<Order, UUID> {

    /**
     * Finds all orders with a specific status, ordered by creation time.
     * 
     * @param status the order status to filter by
     * @return list of orders with the specified status
     */
    List<Order> findAllByStatusOrderByCreatedAtAsc(OrderStatus status);

    /**
     * Finds an order by its unique order code.
     * 
     * @param orderCode the order code to search for
     * @return optional containing the order if found
     */
    Optional<Order> findByOrderCode(String orderCode);

    /**
     * Finds all orders with statuses in the provided list.
     * 
     * @param statuses list of order statuses to filter by
     * @return list of orders matching any of the specified statuses
     */
    List<Order> findAllByStatusIn(List<OrderStatus> statuses);

    /**
     * Finds all orders sorted by creation time in ascending order.
     * 
     * @return list of all orders ordered by creation time
     */
    default List<Order> findAllOrderByCreatedAtAsc() {
        return findAll(Sort.by(Sort.Direction.ASC, "createdAt"));
    }
}
