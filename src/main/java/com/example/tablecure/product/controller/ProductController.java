package com.example.tablecure.product.controller;

import com.example.tablecure.product.dto.ProductDetailResponse;
import com.example.tablecure.product.dto.ProductResponse;
import com.example.tablecure.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ✅ GET all products
    @GetMapping
    public List<ProductResponse> getProducts() {
        return productService.getAllProducts();
    }
    // ✅ GET product by id
    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @GetMapping("/{id}/details")
    public ProductDetailResponse getDetails(@PathVariable Long id) {
        return productService.getProductDetails(id);
    }
}