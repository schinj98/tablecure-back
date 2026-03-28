package com.example.tablecure.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import com.example.tablecure.order.OrderStatus;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime orderDate;

    private String razorpayOrderId;

    private String razorpayPaymentId;   // ← new: needed to initiate refunds

    private String razorpayRefundId;    // ← new: store refund ID from Razorpay

    private String paymentStatus;       // CREATED | PAID | REFUNDED

    @Enumerated(EnumType.STRING)
    private OrderStatus status;         // PENDING | CONFIRMED | SHIPPED | DELIVERED | CANCELLED

    private Double amount;

    private String couponCode;

    private Double discountAmount;

    private Double finalAmount;

    @ManyToOne
    @JoinColumn(name = "address_id")
    private Address address;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private User user;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;
}