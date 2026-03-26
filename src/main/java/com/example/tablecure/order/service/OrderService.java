package com.example.tablecure.order.service;

import com.example.tablecure.entity.*;
import com.example.tablecure.order.OrderStatus;
import com.example.tablecure.order.repository.OrderRepository;
import com.example.tablecure.product.repository.ProductRepository;
import com.example.tablecure.auth.repository.UserRepository;
import com.example.tablecure.address.repository.AddressRepository; // ← import this
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository; // ← add this

    @Transactional
    public Order createOrder(String email, List<OrderItem> items) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Address> addresses = addressRepository.findByUser(user);
        Address address = addresses.isEmpty() ? null : addresses.get(addresses.size() - 1);

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(OrderStatus.PENDING);
        order.setPaymentStatus("CREATED");
        order.setAddress(address);

        for (OrderItem item : items) {
            Product product = productRepository.findById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            item.setProduct(product);
            item.setOrder(order);
            item.setPrice(product.getPrice());
        }

        order.setOrderItems(items);
        return orderRepository.save(order);
    }

    public boolean hasUserPurchasedProduct(String email, Long productId) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return orderRepository.existsByUserAndOrderItems_Product_Id(user, productId);
    }

    public Order save(Order order) {
        return orderRepository.save(order);
    }

    public void markOrderPaid(String razorpayOrderId) {
        Order order = orderRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setPaymentStatus("PAID");
        order.setStatus(OrderStatus.CONFIRMED);

        orderRepository.save(order);
    }

    public List<Order> getUserOrders(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByUser(user);
    }
}