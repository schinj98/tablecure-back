package com.example.tablecure.admin.controller;

import com.example.tablecure.entity.Product;
import com.example.tablecure.entity.ProductFeature;
import com.example.tablecure.entity.ProductImage;
import com.example.tablecure.entity.ProductSpecification;
import com.example.tablecure.product.dto.ProductDetailResponse;
import com.example.tablecure.product.repository.ProductRepository;
import lombok.*;
import com.example.tablecure.product.service.ProductService;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductRepository productRepository;
    private final ProductService productService;

    // ── GET ALL PRODUCTS ────────────────────────────────────────
    @GetMapping
    public List<Product> getAll() {
        return productRepository.findAll();
    }

    // ── GET SINGLE PRODUCT ──────────────────────────────────────
    @GetMapping("/{id}")
    public Product getOne(@PathVariable Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    // ── GET PRODUCT DETAILS (features, specs, images) ──────────
    @GetMapping("/{id}/details")
    public ProductDetailResponse getDetails(@PathVariable Long id) {
        return productService.getProductDetails(id);
    }

    // ── REPLACE ALL FEATURES (bulk) ─────────────────────────────
    // Body: { "features": ["Fast", "Lightweight"] }
    @CacheEvict(value = "product-details", key = "#id")
    @PostMapping("/{id}/features")
    public Product replaceFeatures(@PathVariable Long id,
                                   @RequestBody FeaturesRequest req) {
        Product p = productRepository.findById(id).orElseThrow();

        // Clear existing and replace with new list
        p.getFeatures().clear();

        if (req.getFeatures() != null) {
            for (String feat : req.getFeatures()) {
                ProductFeature f = new ProductFeature();
                f.setFeature(feat);
                f.setProduct(p);
                p.getFeatures().add(f);
            }
        }

        return productRepository.save(p);
    }

    // ── REPLACE ALL SPECIFICATIONS (bulk) ──────────────────────
    // Body: { "specifications": { "Weight": "1kg", "Color": "Red" } }
    @CacheEvict(value = "product-details", key = "#id")
    @PostMapping("/{id}/specifications")
    public Product replaceSpecifications(@PathVariable Long id,
                                         @RequestBody SpecsRequest req) {
        Product p = productRepository.findById(id).orElseThrow();

        p.getSpecifications().clear();

        if (req.getSpecifications() != null) {
            for (Map.Entry<String, String> entry : req.getSpecifications().entrySet()) {
                ProductSpecification s = new ProductSpecification();
                s.setSpecKey(entry.getKey());
                s.setSpecValue(entry.getValue());
                s.setProduct(p);
                p.getSpecifications().add(s);
            }
        }

        return productRepository.save(p);
    }

    // ── REPLACE ALL IMAGES (bulk) ───────────────────────────────
    // Body: { "images": ["img1.jpg", "img2.jpg"] }
    @CacheEvict(value = "product-details", key = "#id")
    @PostMapping("/{id}/images")
    public Product replaceImages(@PathVariable Long id,
                                 @RequestBody ImagesRequest req) {
        Product p = productRepository.findById(id).orElseThrow();

        p.getImages().clear();

        if (req.getImages() != null) {
            for (String url : req.getImages()) {
                ProductImage img = new ProductImage();
                img.setUrl(url);
                img.setProduct(p);
                p.getImages().add(img);
            }
        }

        return productRepository.save(p);
    }

    // ── CREATE PRODUCT ──────────────────────────────────────────
    @PostMapping
    public Product create(@RequestBody ProductRequest req) {
        Product p = Product.builder()
                .name(req.getName())
                .description(req.getDescription())
                .price(req.getPrice())
                .stock(req.getStock())
                .sku(req.getSku())
                .imageUrl(req.getImageUrl())
                .videoUrl(req.getVideoUrl())
                .build();
        p.setFeatures(new ArrayList<>());
        p.setSpecifications(new ArrayList<>());
        p.setImages(new ArrayList<>());
        return productRepository.save(p);
    }

    // ── UPDATE FULL PRODUCT ─────────────────────────────────────
    @CacheEvict(value = "product-details", key = "#id")
    @PutMapping("/{id}")
    public Product update(@PathVariable Long id,
                          @RequestBody ProductRequest req) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        if (req.getName()        != null) p.setName(req.getName());
        if (req.getDescription() != null) p.setDescription(req.getDescription());
        if (req.getPrice()       != null) p.setPrice(req.getPrice());
        if (req.getStock()       != null) p.setStock(req.getStock());
        if (req.getSku()         != null) p.setSku(req.getSku());
        if (req.getImageUrl()    != null) p.setImageUrl(req.getImageUrl());
        if (req.getVideoUrl()    != null) p.setVideoUrl(req.getVideoUrl());

        return productRepository.save(p);
    }

    // ── UPDATE PRICE ONLY ───────────────────────────────────────
    @CacheEvict(value = "product-details", key = "#id")
    @PatchMapping("/{id}/price")
    public Product updatePrice(@PathVariable Long id,
                               @RequestParam BigDecimal price) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        p.setPrice(price);
        return productRepository.save(p);
    }

    // ── UPDATE SKU ONLY ─────────────────────────────────────────
    @CacheEvict(value = "product-details", key = "#id")
    @PatchMapping("/{id}/sku")
    public Product updateSku(@PathVariable Long id,
                             @RequestParam String sku) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        p.setSku(sku);
        return productRepository.save(p);
    }

    // ── UPDATE STOCK ONLY ───────────────────────────────────────
    @CacheEvict(value = "product-details", key = "#id")
    @PatchMapping("/{id}/stock")
    public Product updateStock(@PathVariable Long id,
                               @RequestParam Integer stock) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        p.setStock(stock);
        return productRepository.save(p);
    }

    // ── UPDATE IMAGE URL ────────────────────────────────────────
    @CacheEvict(value = "product-details", key = "#id")
    @PatchMapping("/{id}/image")
    public Product updateImage(@PathVariable Long id,
                               @RequestParam String imageUrl) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        p.setImageUrl(imageUrl);
        return productRepository.save(p);
    }

    // ── UPDATE VIDEO URL ────────────────────────────────────────
    @CacheEvict(value = "product-details", key = "#id")
    @PatchMapping("/{id}/video")
    public Product updateVideo(@PathVariable Long id,
                               @RequestParam String videoUrl) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        p.setVideoUrl(videoUrl);
        return productRepository.save(p);
    }

    // ── DELETE PRODUCT ──────────────────────────────────────────
    @CacheEvict(value = "product-details", key = "#id")
    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        productRepository.deleteById(id);
        return "Product deleted";
    }

    // ── Request DTOs ─────────────────────────────────────────────
    @Getter @Setter @NoArgsConstructor
    public static class ProductRequest {
        private String name;
        private String description;
        private BigDecimal price;
        private Integer stock;
        private String sku;
        private String imageUrl;
        private String videoUrl;
    }

    @Getter @Setter @NoArgsConstructor
    public static class FeaturesRequest {
        private List<String> features;
    }

    @Getter @Setter @NoArgsConstructor
    public static class SpecsRequest {
        private Map<String, String> specifications;
    }

    @Getter @Setter @NoArgsConstructor
    public static class ImagesRequest {
        private List<String> images;
    }
}