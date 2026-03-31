package com.example.tablecure.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GlobalSale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String label;           // e.g. "Festival Sale"
    private Integer discountPercent; // e.g. 20 for 20% off
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
}
