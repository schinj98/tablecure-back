package com.example.tablecure.order.repository;

import com.example.tablecure.entity.Order;
import com.example.tablecure.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);
    Optional<Order> findByRazorpayOrderId(String razorpayOrderId);
    boolean existsByUserAndOrderItems_Product_Id(User user, Long productId);

    List<Order> findAll();
    long countByPaymentStatus(String status);
    @Query("SELECT SUM(o.amount) FROM Order o WHERE o.paymentStatus = 'PAID'")
    Double sumPaidRevenue();

    int countByUser(User user);
}