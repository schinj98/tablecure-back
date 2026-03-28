package com.example.tablecure.entity;

import com.example.tablecure.coupon.DiscountType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "coupons")
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType discountType;

    // Percentage (0-100) or flat amount in rupees
    @Column(nullable = false)
    private Double discountValue;

    // Minimum cart value required to use this coupon
    @Column(nullable = false)
    private Double minOrderAmount;

    // Cap on the discount for PERCENTAGE type (null = no cap)
    private Double maxDiscount;

    // Total number of times this coupon can be used across all users (null = unlimited)
    private Integer usageLimit;

    @Column(nullable = false)
    private Integer usageCount = 0;

    // Max times a single user can use this coupon
    @Column(nullable = false)
    private Integer perUserLimit = 1;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(nullable = false)
    private LocalDateTime expiryDate;
}
