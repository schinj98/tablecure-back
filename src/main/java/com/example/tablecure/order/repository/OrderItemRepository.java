package com.example.tablecure.order.repository;

import com.example.tablecure.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Modifying
    @Query("UPDATE OrderItem oi SET oi.product = null WHERE oi.product.id = :productId")
    void detachProduct(@Param("productId") Long productId);
}