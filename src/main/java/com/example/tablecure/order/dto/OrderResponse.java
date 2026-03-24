package com.example.tablecure.order.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderResponse {

    private Long id;
    private String status;
    private String orderDate;
    private List<OrderItemResponse> items;
}