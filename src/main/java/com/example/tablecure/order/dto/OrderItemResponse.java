package com.example.tablecure.order.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponse {

    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
}