package com.example.tablecure.coupon.service;

import com.example.tablecure.auth.repository.UserRepository;
import com.example.tablecure.common.exception.CouponException;
import com.example.tablecure.coupon.DiscountType;
import com.example.tablecure.coupon.dto.CouponRequest;
import com.example.tablecure.coupon.dto.CouponValidateResponse;
import com.example.tablecure.coupon.repository.CouponRepository;
import com.example.tablecure.coupon.repository.CouponUsageRepository;
import com.example.tablecure.entity.Coupon;
import com.example.tablecure.entity.CouponUsage;
import com.example.tablecure.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;
    private final UserRepository userRepository;

    /**
     * Preview: validates the coupon and returns discount info WITHOUT recording usage.
     * Called from the validate endpoint so users can see their savings before placing the order.
     */
    public CouponValidateResponse preview(String rawCode, Double orderAmount, String userEmail) {
        String code = normalize(rawCode);
        Coupon coupon = couponRepository.findByCode(code)
                .orElseThrow(() -> new CouponException("Invalid coupon code"));

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new CouponException("User not found"));

        validateRules(coupon, orderAmount, user);

        double discountAmount = calculateDiscount(coupon, orderAmount);
        return new CouponValidateResponse(
                code,
                coupon.getDiscountType(),
                coupon.getDiscountValue(),
                discountAmount,
                orderAmount - discountAmount
        );
    }

    /**
     * Apply: validates the coupon and records usage atomically.
     * Must be called inside the order-creation transaction so that a failed order
     * rolls back the usage records too.
     *
     * Uses a pessimistic write lock on the coupon row to prevent two concurrent
     * requests from over-redeeming a limited coupon.
     *
     * @return the discount amount in rupees
     */
    @Transactional
    public double applyAndRecord(String rawCode, Double orderAmount, User user) {
        String code = normalize(rawCode);

        // Pessimistic lock — only one transaction can hold this at a time
        Coupon coupon = couponRepository.findByCodeForUpdate(code)
                .orElseThrow(() -> new CouponException("Invalid coupon code"));

        validateRules(coupon, orderAmount, user);
        double discountAmount = calculateDiscount(coupon, orderAmount);

        // Increment global usage counter
        coupon.setUsageCount(coupon.getUsageCount() + 1);
        couponRepository.save(coupon);

        // Increment per-user usage counter
        CouponUsage usage = couponUsageRepository.findByCouponAndUser(coupon, user)
                .orElse(CouponUsage.builder().coupon(coupon).user(user).usageCount(0).build());
        usage.setUsageCount(usage.getUsageCount() + 1);
        couponUsageRepository.save(usage);

        return discountAmount;
    }

    // ── Admin operations ──────────────────────────────────────────────────────

    @Transactional
    public Coupon create(CouponRequest req) {
        validateRequest(req);
        String code = normalize(req.getCode());
        if (couponRepository.findByCode(code).isPresent()) {
            throw new CouponException("Coupon code already exists");
        }
        Coupon coupon = Coupon.builder()
                .code(code)
                .discountType(req.getDiscountType())
                .discountValue(req.getDiscountValue())
                .minOrderAmount(req.getMinOrderAmount())
                .maxDiscount(req.getMaxDiscount())
                .usageLimit(req.getUsageLimit())
                .usageCount(0)
                .perUserLimit(req.getPerUserLimit() != null ? req.getPerUserLimit() : 1)
                .active(true)
                .expiryDate(req.getExpiryDate())
                .build();
        return couponRepository.save(coupon);
    }

    @Transactional
    public Coupon update(Long id, CouponRequest req) {
        validateRequest(req);
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new CouponException("Coupon not found"));

        String code = normalize(req.getCode());
        if (!coupon.getCode().equals(code)) {
            couponRepository.findByCode(code).ifPresent(c -> {
                throw new CouponException("Coupon code already exists");
            });
            coupon.setCode(code);
        }
        coupon.setDiscountType(req.getDiscountType());
        coupon.setDiscountValue(req.getDiscountValue());
        coupon.setMinOrderAmount(req.getMinOrderAmount());
        coupon.setMaxDiscount(req.getMaxDiscount());
        coupon.setUsageLimit(req.getUsageLimit());
        coupon.setPerUserLimit(req.getPerUserLimit() != null ? req.getPerUserLimit() : 1);
        coupon.setExpiryDate(req.getExpiryDate());
        return couponRepository.save(coupon);
    }

    @Transactional
    public void toggle(Long id) {
        Coupon coupon = couponRepository.findById(id)
                .orElseThrow(() -> new CouponException("Coupon not found"));
        coupon.setActive(!coupon.getActive());
        couponRepository.save(coupon);
    }

    public List<Coupon> listAll() {
        return couponRepository.findAll();
    }

    @Transactional
    public void delete(Long id) {
        couponRepository.findById(id)
                .orElseThrow(() -> new CouponException("Coupon not found"));
        couponRepository.deleteById(id);
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private String normalize(String code) {
        if (code == null || code.isBlank()) {
            throw new CouponException("Coupon code cannot be empty");
        }
        return code.trim().toUpperCase();
    }

    private void validateRules(Coupon coupon, Double orderAmount, User user) {
        if (!coupon.getActive()) {
            throw new CouponException("This coupon is no longer active");
        }
        if (coupon.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new CouponException("This coupon has expired");
        }
        if (orderAmount < coupon.getMinOrderAmount()) {
            throw new CouponException(
                    "Minimum order amount of ₹" + coupon.getMinOrderAmount().intValue() + " required for this coupon");
        }
        if (coupon.getUsageLimit() != null && coupon.getUsageCount() >= coupon.getUsageLimit()) {
            throw new CouponException("This coupon has been fully redeemed");
        }
        int userUsage = couponUsageRepository.findByCouponAndUser(coupon, user)
                .map(CouponUsage::getUsageCount)
                .orElse(0);
        if (userUsage >= coupon.getPerUserLimit()) {
            throw new CouponException("You have already used this coupon");
        }
    }

    private double calculateDiscount(Coupon coupon, Double orderAmount) {
        if (coupon.getDiscountType() == DiscountType.FLAT) {
            // Flat discount cannot exceed the order amount itself
            return Math.min(coupon.getDiscountValue(), orderAmount);
        }
        // Percentage discount
        double discount = orderAmount * coupon.getDiscountValue() / 100.0;
        if (coupon.getMaxDiscount() != null) {
            discount = Math.min(discount, coupon.getMaxDiscount());
        }
        return discount;
    }

    private void validateRequest(CouponRequest req) {
        if (req.getCode() == null || req.getCode().isBlank()) {
            throw new CouponException("Code is required");
        }
        if (req.getDiscountType() == null) {
            throw new CouponException("Discount type is required");
        }
        if (req.getDiscountValue() == null || req.getDiscountValue() <= 0) {
            throw new CouponException("Discount value must be greater than 0");
        }
        if (req.getDiscountType() == DiscountType.PERCENTAGE && req.getDiscountValue() > 100) {
            throw new CouponException("Percentage discount cannot exceed 100");
        }
        if (req.getMinOrderAmount() == null || req.getMinOrderAmount() < 0) {
            throw new CouponException("Minimum order amount must be 0 or greater");
        }
        if (req.getExpiryDate() == null || req.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new CouponException("Expiry date must be in the future");
        }
    }
}
