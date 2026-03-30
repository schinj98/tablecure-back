package com.example.tablecure.product.controller;

import com.example.tablecure.product.dto.ProductDetailResponse;
import com.example.tablecure.product.dto.ProductResponse;
import com.example.tablecure.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ✅ GET all products
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getProducts() {
        return ResponseEntity.ok()
                .header("Cloudflare-CDN-Cache-Control", "public, max-age=600")
                .cacheControl(CacheControl.noStore())
                .body(productService.getAllProducts());
    }

    // ✅ GET product by id
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        return ResponseEntity.ok()
                .header("Cloudflare-CDN-Cache-Control", "public, max-age=600")
                .cacheControl(CacheControl.noStore())
                .body(productService.getProductById(id));
    }

    @GetMapping("/{id}/details")
    public ResponseEntity<ProductDetailResponse> getDetails(@PathVariable Long id) {
        return ResponseEntity.ok()
                .header("Cloudflare-CDN-Cache-Control", "public, max-age=600")
                .cacheControl(CacheControl.noStore())
                .body(productService.getProductDetails(id));
    }
}