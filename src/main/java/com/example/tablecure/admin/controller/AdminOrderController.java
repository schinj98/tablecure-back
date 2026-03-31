package com.example.tablecure.admin.controller;

import com.example.tablecure.email.EmailService;
import com.example.tablecure.entity.Order;
import com.example.tablecure.entity.User;
import com.example.tablecure.order.OrderStatus;
import com.example.tablecure.order.repository.OrderRepository;
import com.example.tablecure.auth.repository.UserRepository;

import lombok.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import com.razorpay.RazorpayClient;
import org.json.JSONObject;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderRepository orderRepository;
    private final UserRepository  userRepository;
    private final EmailService    emailService;

    @Value("${razorpay.key}")
    private String razorpayKey;

    @Value("${razorpay.secret}")
    private String razorpaySecret;

    // ── GET ALL ORDERS ──────────────────────────────────────────
    @GetMapping
    public Page<OrderSummary> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::toSummary);
    }

    // ── GET SINGLE ORDER ────────────────────────────────────────
    @GetMapping("/{id}")
    public OrderSummary getOrder(@PathVariable Long id) {
        return toSummary(orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found")));
    }

    // ── GET ALL ORDERS OF A USER (by userId) ────────────────────
    @GetMapping("/user/{userId}")
    public List<OrderSummary> getOrdersByUser(@PathVariable Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByUser(user).stream()
                .map(this::toSummary).collect(Collectors.toList());
    }

    // ── GET ALL ORDERS OF A USER (by email) ─────────────────────
    @GetMapping("/user/email/{email}")
    public List<OrderSummary> getOrdersByEmail(@PathVariable String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return orderRepository.findByUser(user).stream()
                .map(this::toSummary).collect(Collectors.toList());
    }

    // ── UPDATE ORDER STATUS ─────────────────────────────────────
    // PENDING | CONFIRMED | SHIPPED | DELIVERED | CANCELLED
    @PutMapping("/{id}/status")
    public OrderSummary updateStatus(@PathVariable Long id,
                                     @RequestParam OrderStatus status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        order.setStatus(status);
        Order saved = orderRepository.save(order);
        emailService.sendOrderStatusEmail(saved, status);
        return toSummary(saved);
    }

    // ── PROCESS ORDER (advance pipeline) ────────────────────────
    // CONFIRMED → SHIPPED → DELIVERED
    @PostMapping("/{id}/process")
    public OrderSummary processOrder(@PathVariable Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.CONFIRMED) {
            order.setStatus(OrderStatus.SHIPPED);
        } else if (order.getStatus() == OrderStatus.SHIPPED) {
            order.setStatus(OrderStatus.DELIVERED);
        } else {
            throw new RuntimeException(
                    "Cannot process order in status: " + order.getStatus());
        }
        Order saved = orderRepository.save(order);
        emailService.sendOrderStatusEmail(saved, saved.getStatus());
        return toSummary(saved);
    }

    // ── CANCEL ORDER ────────────────────────────────────────────
    @PostMapping("/{id}/cancel")
    public OrderSummary cancelOrder(@PathVariable Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new RuntimeException("Cannot cancel a delivered order");
        }
        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        emailService.sendOrderStatusEmail(saved, OrderStatus.CANCELLED);
        return toSummary(saved);
    }

    // ── FULL REFUND via Razorpay ─────────────────────────────────
    @PostMapping("/{id}/refund")
    public OrderSummary refundOrder(@PathVariable Long id) throws Exception {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"PAID".equals(order.getPaymentStatus()))
            throw new RuntimeException("Order is not PAID — cannot refund");
        if (order.getRazorpayPaymentId() == null)
            throw new RuntimeException("No Razorpay payment ID on this order");

        RazorpayClient client = new RazorpayClient(razorpayKey, razorpaySecret);
        JSONObject req = new JSONObject();
        if (order.getAmount() != null)
            req.put("amount", (int)(order.getAmount() * 100)); // paise
        req.put("speed", "normal");
        req.put("notes", new JSONObject().put("reason", "Admin initiated full refund"));

        com.razorpay.Refund refund = client.payments.refund(
                order.getRazorpayPaymentId(), req);

        order.setRazorpayRefundId(refund.get("id"));
        order.setPaymentStatus("REFUNDED");
        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);
        emailService.sendOrderStatusEmail(saved, OrderStatus.REFUNDED);
        return toSummary(saved);
    }

    // ── PARTIAL REFUND via Razorpay ──────────────────────────────
    @PostMapping("/{id}/refund/partial")
    public OrderSummary partialRefund(@PathVariable Long id,
                                      @RequestParam double amount) throws Exception {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!"PAID".equals(order.getPaymentStatus()))
            throw new RuntimeException("Order is not PAID");
        if (order.getRazorpayPaymentId() == null)
            throw new RuntimeException("No Razorpay payment ID on this order");

        RazorpayClient client = new RazorpayClient(razorpayKey, razorpaySecret);
        JSONObject req = new JSONObject();
        req.put("amount", (int)(amount * 100));
        req.put("speed", "normal");
        req.put("notes", new JSONObject().put("reason", "Partial refund by admin"));

        com.razorpay.Refund refund = client.payments.refund(
                order.getRazorpayPaymentId(), req);

        order.setRazorpayRefundId(refund.get("id"));
        order.setPaymentStatus("PARTIALLY_REFUNDED");
        return toSummary(orderRepository.save(order));
    }

    // ── HELPER: Order entity → flat DTO ─────────────────────────
    private OrderSummary toSummary(Order o) {
        return new OrderSummary(
                o.getId(),
                o.getOrderDate() != null ? o.getOrderDate().toString() : null,
                o.getStatus()    != null ? o.getStatus().name()        : null,
                o.getPaymentStatus(),
                o.getAmount(),
                o.getRazorpayOrderId(),
                o.getRazorpayPaymentId(),
                o.getRazorpayRefundId(),
                o.getUser() != null ? o.getUser().getId()    : null,
                o.getUser() != null ? o.getUser().getName()  : null,
                o.getUser() != null ? o.getUser().getEmail() : null,
                o.getAddress() != null
                        ? o.getAddress().getStreet() + ", "
                        + o.getAddress().getCity() + ", "
                        + o.getAddress().getState() + " - "
                        + o.getAddress().getPincode()
                        : null,
                o.getOrderItems() != null
                        ? o.getOrderItems().stream().map(i -> new ItemSummary(
                        i.getProduct() != null ? i.getProduct().getId()   : null,
                        i.getProduct() != null ? i.getProduct().getName() : null,
                        i.getQuantity(),
                        i.getPrice() != null ? i.getPrice().doubleValue() : 0
                )).collect(Collectors.toList())
                        : List.of()
        );
    }

    // ── DTOs ─────────────────────────────────────────────────────
    @Getter @AllArgsConstructor
    public static class OrderSummary {
        private Long   id;
        private String orderDate;
        private String status;
        private String paymentStatus;
        private Double amount;
        private String razorpayOrderId;
        private String razorpayPaymentId;
        private String razorpayRefundId;
        private Long   userId;
        private String userName;
        private String userEmail;
        private String address;
        private List<ItemSummary> items;
    }

    @Getter @AllArgsConstructor
    public static class ItemSummary {
        private Long   productId;
        private String productName;
        private int    quantity;
        private double price;
    }
}