package com.example.tablecure.coupon.dto;

import com.example.tablecure.coupon.DiscountType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CouponValidateResponse {
    private String code;
    private DiscountType discountType;
    private Double discountValue;   // e.g. 10 for 10% or ₹100 flat
    private Double discountAmount;  // actual rupees saved on this order
    private Double finalAmount;     // orderAmount - discountAmount
}
