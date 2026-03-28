package com.example.tablecure.coupon.dto;

import com.example.tablecure.coupon.DiscountType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CouponRequest {
    private String code;
    private DiscountType discountType;
    private Double discountValue;
    private Double minOrderAmount;
    private Double maxDiscount;     // optional — cap for PERCENTAGE coupons
    private Integer usageLimit;     // optional — null means unlimited
    private Integer perUserLimit;   // optional — defaults to 1
    private LocalDateTime expiryDate;
}
