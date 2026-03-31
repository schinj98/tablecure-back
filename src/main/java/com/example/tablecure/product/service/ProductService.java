package com.example.tablecure.product.service;

import com.example.tablecure.entity.*;
import com.example.tablecure.product.dto.ProductDetailResponse;
import com.example.tablecure.product.dto.ProductResponse;
import com.example.tablecure.product.repository.ProductRepository;
import com.example.tablecure.review.dto.ReviewResponse;
import com.example.tablecure.sale.service.GlobalSaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.tablecure.entity.ProductFeature;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final GlobalSaleService globalSaleService;

    @Cacheable(value = "product-details", key = "#id")
    @Transactional(readOnly = true)
    public ProductDetailResponse getProductDetails(Long id) {

        Product p = productRepository.findById(id).orElseThrow();

        List<String> features = p.getFeatures()
                .stream().map(ProductFeature::getFeature).toList();

        Map<String, String> specs = p.getSpecifications()
                .stream()
                .collect(Collectors.toMap(
                        ProductSpecification::getSpecKey,
                        ProductSpecification::getSpecValue
                ));

        List<String> images = p.getImages()
                .stream().map(ProductImage::getUrl).toList();

        double avgRating = p.getReviews()
                .stream()
                .mapToInt(Review::getRating)
                .average().orElse(0);

        List<ReviewResponse> reviews = p.getReviews()
                .stream()
                .map(r -> ReviewResponse.builder()
                        .id(r.getId())
                        .userName(r.getUser() != null ? r.getUser().getName() : "Anonymous")
                        .rating(r.getRating())
                        .comment(r.getComment())
                        .build()
                )
                .toList();

        Optional<GlobalSale> activeSale = globalSaleService.getActiveSale();
        BigDecimal salePrice = activeSale
                .map(sale -> p.getPrice().multiply(
                        BigDecimal.valueOf(1.0 - sale.getDiscountPercent() / 100.0))
                        .setScale(2, RoundingMode.HALF_UP))
                .orElse(null);
        Integer salePercent = activeSale.map(GlobalSale::getDiscountPercent).orElse(null);

        return ProductDetailResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .mrp(p.getMrp())
                .salePrice(salePrice)
                .salePercent(salePercent)
                .imageUrl(p.getImageUrl())
                .videoUrl(p.getVideoUrl())
                .features(features)
                .specifications(specs)
                .images(images)
                .avgRating(avgRating)
                .reviews(reviews)
                .totalReviews(p.getReviews().size())
                .build();
    }

    public List<ProductResponse> getAllProducts() {
        Optional<GlobalSale> activeSale = globalSaleService.getActiveSale();

        return productRepository.findAll()
                .stream()
                .map(p -> toProductResponse(p, activeSale))
                .toList();
    }

    public ProductResponse getProductById(Long id) {
        Product p = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        return toProductResponse(p, globalSaleService.getActiveSale());
    }

    private ProductResponse toProductResponse(Product p, Optional<GlobalSale> activeSale) {
        BigDecimal salePrice = activeSale
                .map(sale -> p.getPrice().multiply(
                        BigDecimal.valueOf(1.0 - sale.getDiscountPercent() / 100.0))
                        .setScale(2, RoundingMode.HALF_UP))
                .orElse(null);
        Integer salePercent = activeSale.map(GlobalSale::getDiscountPercent).orElse(null);

        return ProductResponse.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .mrp(p.getMrp())
                .salePrice(salePrice)
                .salePercent(salePercent)
                .stock(p.getStock())
                .imageUrl(p.getImageUrl())
                .build();
    }

    public Product addProduct(Product product) {
        return productRepository.save(product);
    }

    @CacheEvict(value = "product-details", key = "#id")
    public Product updateProduct(Long id, Product updatedProduct) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setName(updatedProduct.getName());
        product.setDescription(updatedProduct.getDescription());
        product.setPrice(updatedProduct.getPrice());
        product.setStock(updatedProduct.getStock());

        return productRepository.save(product);
    }

    @CacheEvict(value = "product-details", key = "#id")
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}