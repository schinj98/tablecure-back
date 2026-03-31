package com.example.tablecure.sale.service;

import com.example.tablecure.entity.GlobalSale;
import com.example.tablecure.sale.repository.GlobalSaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GlobalSaleService {

    private final GlobalSaleRepository saleRepository;

    public Optional<GlobalSale> getActiveSale() {
        return saleRepository.findFirstByActiveTrue();
    }

    public boolean hasActiveSale() {
        return saleRepository.findFirstByActiveTrue().isPresent();
    }
}
