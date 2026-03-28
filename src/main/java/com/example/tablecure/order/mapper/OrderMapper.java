package com.example.tablecure.order.mapper;

import com.example.tablecure.entity.*;
import com.example.tablecure.order.dto.OrderItemResponse;
import com.example.tablecure.order.dto.OrderResponse;

import java.util.List;
import java.util.stream.Collectors;

public class OrderMapper {

    public static OrderResponse toResponse(Order order) {

        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setStatus(order.getStatus().name());
        response.setOrderDate(order.getOrderDate().toString());

        List<OrderItemResponse> items = order.getOrderItems()
                .stream()
                .map(item -> {
                    OrderItemResponse dto = new OrderItemResponse();
                    dto.setProductId(item.getProduct().getId());
                    dto.setProductName(item.getProduct().getName());
                    dto.setQuantity(item.getQuantity());
                    dto.setPrice(item.getPrice());
                    return dto;
                })
                .collect(Collectors.toList());

        response.setItems(items);
        response.setCouponCode(order.getCouponCode());
        response.setDiscountAmount(order.getDiscountAmount());
        response.setFinalAmount(order.getFinalAmount());

        return response;
    }
}