package com.javaprojects.moonwalk.repository;

import com.javaprojects.moonwalk.model.OrderStatus;
import com.javaprojects.moonwalk.model.order.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    @Query("SELECT o FROM Order o WHERE o.orderStatus = :status AND o.orderDate < :dateTime")
    List<Order> findPendingOlderThan(@Param("status") OrderStatus status,
                                     @Param("dateTime") LocalDateTime dateTime);
}
