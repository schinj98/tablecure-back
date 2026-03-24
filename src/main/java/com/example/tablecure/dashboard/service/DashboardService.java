package com.example.tablecure.dashboard.service;

import com.example.tablecure.dashboard.dto.DashboardResponse;
import com.example.tablecure.entity.User;
import com.example.tablecure.entity.Address;
import com.example.tablecure.entity.Order;
import com.example.tablecure.auth.repository.UserRepository;
import com.example.tablecure.address.repository.AddressRepository;
import com.example.tablecure.order.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final AddressRepository addressRepository;
    private final OrderRepository orderRepository;

    public DashboardResponse getDashboard(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Address> addresses = addressRepository.findByUser(user);
        List<Order> orders = orderRepository.findByUser(user);

        return DashboardResponse.builder()
                .user(user)
                .addresses(addresses)
                .orders(orders)
                .build();
    }
}