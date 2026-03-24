package com.example.tablecure.admin.service;

import com.example.tablecure.admin.dto.AdminDashboardResponse;
import com.example.tablecure.entity.Order;
import com.example.tablecure.order.repository.OrderRepository;
import com.example.tablecure.product.repository.ProductRepository;
import com.example.tablecure.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public AdminDashboardResponse getStats() {

        List<Order> allOrders = orderRepository.findAll();

        long totalOrders   = allOrders.size();
        long totalUsers    = userRepository.count();
        long totalProducts = productRepository.count();

        long paidOrders = allOrders.stream()
                .filter(o -> "PAID".equals(o.getPaymentStatus()))
                .count();

        long pendingOrders = totalOrders - paidOrders;

        // Revenue = sum of confirmed amounts on PAID orders only
        double totalRevenue = allOrders.stream()
                .filter(o -> "PAID".equals(o.getPaymentStatus()))
                .filter(o -> o.getAmount() != null)
                .mapToDouble(Order::getAmount)
                .sum();

        return AdminDashboardResponse.builder()
                .totalOrders(totalOrders)
                .paidOrders(paidOrders)
                .pendingOrders(pendingOrders)
                .totalRevenue(totalRevenue)
                .totalUsers(totalUsers)
                .totalProducts(totalProducts)
                .build();
    }
}