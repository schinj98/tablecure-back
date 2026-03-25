package com.example.tablecure.review.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewResponse {

    private Long id;
    private String userName;
    private int rating;
    private String comment;
}