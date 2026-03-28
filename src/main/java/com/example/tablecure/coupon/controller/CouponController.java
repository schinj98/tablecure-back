package com.example.tablecure.coupon.controller;

import com.example.tablecure.coupon.dto.CouponValidateRequest;
import com.example.tablecure.coupon.dto.CouponValidateResponse;
import com.example.tablecure.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/validate")
    public CouponValidateResponse validate(@RequestBody CouponValidateRequest request,
                                           Principal principal) {
        return couponService.preview(request.getCode(), request.getOrderAmount(), principal.getName());
    }
}
