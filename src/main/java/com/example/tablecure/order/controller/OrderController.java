package com.example.tablecure.order.controller;

import com.example.tablecure.order.dto.OrderResponse;
import com.example.tablecure.entity.Order;
import com.example.tablecure.order.mapper.OrderMapper;
import com.example.tablecure.entity.OrderItem;
import com.example.tablecure.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public OrderResponse createOrder(@RequestBody List<OrderItem> items,
                                     Principal principal) {
        Order order = orderService.createOrder(principal.getName(), items);
        return OrderMapper.toResponse(order);
    }

    @GetMapping
    public List<OrderResponse> getOrders(Principal principal) {

        return orderService.getUserOrders(principal.getName())
                .stream()
                .map(OrderMapper::toResponse)
                .toList();
    }

    @GetMapping("/has-purchased/{productId}")
    public boolean hasPurchased(@PathVariable Long productId, Principal principal) {

        return orderService.hasUserPurchasedProduct(principal.getName(), productId);
    }
}