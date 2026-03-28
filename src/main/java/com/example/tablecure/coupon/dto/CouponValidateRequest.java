package com.example.tablecure.coupon.dto;

import lombok.Data;

@Data
public class CouponValidateRequest {
    private String code;
    private Double orderAmount;
}
