package com.example.tablecure.review.repository;

import com.example.tablecure.entity.Product;
import com.example.tablecure.entity.Review;
import com.example.tablecure.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByUserAndProduct(User user, Product product);
}