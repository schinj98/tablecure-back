package com.example.tablecure.product.repository;

import com.example.tablecure.entity.Product;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    @EntityGraph(attributePaths = {
            "features",
            "specifications",
            "images",
            "reviews",
            "reviews.user" // 🔥 THIS FIXES YOUR ISSUE
    })
    Optional<Product> findById(Long id);
}