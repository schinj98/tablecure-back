package com.example.tablecure.sale.repository;

import com.example.tablecure.entity.GlobalSale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GlobalSaleRepository extends JpaRepository<GlobalSale, Long> {
    Optional<GlobalSale> findFirstByActiveTrue();
}
