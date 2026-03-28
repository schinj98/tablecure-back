package com.example.tablecure.payment.controller;

import com.example.tablecure.entity.Order;
import com.example.tablecure.order.OrderStatus;
import com.example.tablecure.order.repository.OrderRepository;
import com.example.tablecure.payment.dto.CreateOrderRequest;
import com.example.tablecure.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.web.bind.annotation.*;
import com.example.tablecure.order.service.OrderService;

import java.security.Principal;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @PostMapping("/create-order")
    public String createOrder(@RequestBody CreateOrderRequest request,
                              Principal principal) throws Exception {

        Order order = orderService.createOrder(
                principal.getName(),
                request.getItems(),
                request.getCouponCode()
        );

        // Use finalAmount (after coupon) as what Razorpay should charge
        int amountInRupees = (int) Math.round(order.getFinalAmount());

        String razorpayOrder = paymentService.createOrder(amountInRupees);
        JSONObject json        = new JSONObject(razorpayOrder);
        String razorpayId      = json.getString("id");
        double confirmedAmount = json.getInt("amount") / 100.0;

        order.setRazorpayOrderId(razorpayId);
        order.setAmount(confirmedAmount);
        order.setPaymentStatus("CREATED");
        orderService.save(order);

        return razorpayOrder;
    }

    @PostMapping("/verify")
    public String verify(@RequestParam String orderId,
                         @RequestParam String paymentId,
                         @RequestParam String signature) throws Exception {

        boolean isValid = paymentService.verifyPayment(orderId, paymentId, signature);

        if (isValid) {
            // Save paymentId — needed later for Razorpay refunds
            Order order = orderRepository.findByRazorpayOrderId(orderId)
                    .orElseThrow(() -> new RuntimeException("Order not found"));
            order.setRazorpayPaymentId(paymentId);
            order.setPaymentStatus("PAID");
            order.setStatus(OrderStatus.CONFIRMED);
            orderRepository.save(order);
            return "Payment Success";
        }

        return "Payment Failed";
    }
}