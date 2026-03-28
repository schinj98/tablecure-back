package com.example.tablecure.coupon.repository;

import com.example.tablecure.entity.Coupon;
import com.example.tablecure.entity.CouponUsage;
import com.example.tablecure.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {

    Optional<CouponUsage> findByCouponAndUser(Coupon coupon, User user);
}
