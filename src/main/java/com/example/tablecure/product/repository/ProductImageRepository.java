package com.example.tablecure.product.repository;

import com.example.tablecure.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    @Modifying
    @Query("DELETE FROM ProductImage i WHERE i.id IN :ids AND i.product.id = :productId")
    int deleteByIdsAndProductId(@Param("ids") List<Long> ids, @Param("productId") Long productId);
}
