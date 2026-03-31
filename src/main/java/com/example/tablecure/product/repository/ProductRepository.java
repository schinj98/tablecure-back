package com.example.tablecure.product.repository;

import com.example.tablecure.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("SELECT p.id FROM Product p")
    List<Long> findAllIds();
}