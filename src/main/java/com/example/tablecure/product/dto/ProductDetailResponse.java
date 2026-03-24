package com.example.tablecure.product.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Builder
@Getter
@Setter
public class ProductDetailResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private String imageUrl;

    private List<String> features;
    private Map<String, String> specifications;
    private List<String> images;

    private double avgRating;
    private int totalReviews;
}