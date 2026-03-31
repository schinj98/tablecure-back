package com.example.tablecure.product.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal mrp;         // crossed-out price; null = no strikethrough
    private BigDecimal salePrice;   // discounted price during active sale; null = no sale
    private Integer salePercent;    // e.g. 20 for "20% OFF" badge; null = no sale
    private Integer stock;
    private String imageUrl;


}