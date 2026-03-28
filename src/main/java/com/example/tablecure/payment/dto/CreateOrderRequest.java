package com.example.tablecure.payment.dto;

import com.example.tablecure.entity.OrderItem;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    private List<OrderItem> items;
    private String couponCode; // optional
}
