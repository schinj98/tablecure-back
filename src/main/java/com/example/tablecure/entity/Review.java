package com.example.tablecure.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int rating;
    private String comment;

    @ManyToOne
    @JsonBackReference("product-reviews")   // reviews are @JsonIgnore on Product side,
    private Product product;                // but still add this to be safe

    @ManyToOne
    private User user;
}