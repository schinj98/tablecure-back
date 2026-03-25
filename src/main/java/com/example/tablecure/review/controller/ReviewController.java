package com.example.tablecure.review.controller;

import com.example.tablecure.review.dto.ReviewRequest;
import com.example.tablecure.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping("/{productId}/reviews")
    public void addReview(@PathVariable Long productId,
                          @RequestBody ReviewRequest req,
                          Principal principal) {

        reviewService.addReview(
                principal.getName(),
                productId,
                req.getRating(),
                req.getComment()
        );
    }
}