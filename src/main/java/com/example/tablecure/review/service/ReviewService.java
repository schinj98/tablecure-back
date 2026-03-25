package com.example.tablecure.review.service;

import com.example.tablecure.auth.repository.UserRepository;
import com.example.tablecure.entity.Product;
import com.example.tablecure.entity.Review;
import com.example.tablecure.entity.User;
import com.example.tablecure.order.repository.OrderRepository;
import com.example.tablecure.product.repository.ProductRepository;
import com.example.tablecure.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public void addReview(String email, Long productId, int rating, String comment) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        // ✅ CHECK PURCHASE
        boolean purchased = orderRepository
                .existsByUserAndOrderItems_Product_Id(user, productId);

        if (!purchased) {
            throw new RuntimeException("You must purchase this product to review");
        }

        // ✅ PREVENT DUPLICATE
        if (reviewRepository.existsByUserAndProduct(user, product)) {
            throw new RuntimeException("You already reviewed this product");
        }

        Review review = new Review();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(rating);
        review.setComment(comment);

        reviewRepository.save(review);
    }
}