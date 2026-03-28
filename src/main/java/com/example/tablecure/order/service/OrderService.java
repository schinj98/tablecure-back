package com.example.tablecure.order.service;

import com.example.tablecure.coupon.service.CouponService;
import com.example.tablecure.entity.*;
import com.example.tablecure.order.OrderStatus;
import com.example.tablecure.order.repository.OrderRepository;
import com.example.tablecure.product.repository.ProductRepository;
import com.example.tablecure.auth.repository.UserRepository;
import com.example.tablecure.address.repository.AddressRepository;
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
    private final AddressRepository addressRepository;
    private final CouponService couponService;

    @Transactional
    public Order createOrder(String email, List<OrderItem> items, String couponCode) {

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

        double subtotal = items.stream()
                .mapToDouble(i -> i.getPrice().doubleValue() * i.getQuantity())
                .sum();

        double discountAmount = 0;
        if (couponCode != null && !couponCode.isBlank()) {
            // applyAndRecord is @Transactional — runs within this transaction,
            // so a rollback here also undoes the coupon usage records.
            discountAmount = couponService.applyAndRecord(couponCode, subtotal, user);
            order.setCouponCode(couponCode.trim().toUpperCase());
            order.setDiscountAmount(discountAmount);
        }
        order.setFinalAmount(subtotal - discountAmount);

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