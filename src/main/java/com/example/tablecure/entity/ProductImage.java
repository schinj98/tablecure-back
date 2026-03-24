package com.example.tablecure.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class ProductImage {

    @Id
    @GeneratedValue
    private Long id;

    private String url;

    @ManyToOne
    @JoinColumn(name = "product_id")
    @JsonBackReference("product-images")
    private Product product;
}