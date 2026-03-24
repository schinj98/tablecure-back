package com.example.tablecure.order.repository;

import com.example.tablecure.entity.Order;
import com.example.tablecure.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUser(User user);
    Optional<Order> findByRazorpayOrderId(String razorpayOrderId);
}