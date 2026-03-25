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

        long totalOrders = orderRepository.count();

        long paidOrders = orderRepository.countByPaymentStatus("PAID");

        double totalRevenue = orderRepository.sumPaidRevenue();

        long totalUsers    = userRepository.count();
        long totalProducts = productRepository.count();


        long pendingOrders = totalOrders - paidOrders;


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