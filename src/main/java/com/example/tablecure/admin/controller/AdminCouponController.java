package com.example.tablecure.admin.controller;

import com.example.tablecure.coupon.dto.CouponRequest;
import com.example.tablecure.coupon.service.CouponService;
import com.example.tablecure.entity.Coupon;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponService couponService;

    @GetMapping
    public List<Coupon> listAll() {
        return couponService.listAll();
    }

    @PostMapping
    public Coupon create(@RequestBody CouponRequest request) {
        return couponService.create(request);
    }

    @PutMapping("/{id}")
    public Coupon update(@PathVariable Long id, @RequestBody CouponRequest request) {
        return couponService.update(id, request);
    }

    @PatchMapping("/{id}/toggle")
    public String toggle(@PathVariable Long id) {
        couponService.toggle(id);
        return "Coupon status toggled";
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        couponService.delete(id);
        return "Coupon deleted";
    }
}
