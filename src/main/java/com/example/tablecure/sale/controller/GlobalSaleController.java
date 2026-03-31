package com.example.tablecure.sale.controller;

import com.example.tablecure.entity.GlobalSale;
import com.example.tablecure.sale.repository.GlobalSaleRepository;
import lombok.*;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class GlobalSaleController {

    private final GlobalSaleRepository saleRepository;

    // ── PUBLIC: active sale info for banner ─────────────────────
    @GetMapping("/api/sale/active")
    public GlobalSale getActiveSale() {
        return saleRepository.findFirstByActiveTrue().orElse(null);
    }

    // ── ADMIN: list all sales ────────────────────────────────────
    @GetMapping("/api/admin/sale")
    public List<GlobalSale> listAll() {
        return saleRepository.findAll();
    }

    // ── ADMIN: create a sale ─────────────────────────────────────
    @PostMapping("/api/admin/sale")
    public GlobalSale create(@RequestBody SaleRequest req) {
        GlobalSale sale = GlobalSale.builder()
                .label(req.getLabel())
                .discountPercent(req.getDiscountPercent())
                .startDate(req.getStartDate())
                .endDate(req.getEndDate())
                .active(false)
                .build();
        return saleRepository.save(sale);
    }

    // ── ADMIN: activate a sale (deactivates any currently active) ─
    @CacheEvict(value = "product-details", allEntries = true)
    @PostMapping("/api/admin/sale/{id}/activate")
    public GlobalSale activate(@PathVariable Long id) {
        // Deactivate any currently active sale first
        saleRepository.findFirstByActiveTrue().ifPresent(current -> {
            current.setActive(false);
            saleRepository.save(current);
        });

        GlobalSale sale = saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found"));
        sale.setActive(true);
        return saleRepository.save(sale);
    }

    // ── ADMIN: deactivate a sale ─────────────────────────────────
    @CacheEvict(value = "product-details", allEntries = true)
    @PostMapping("/api/admin/sale/{id}/deactivate")
    public GlobalSale deactivate(@PathVariable Long id) {
        GlobalSale sale = saleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found"));
        sale.setActive(false);
        return saleRepository.save(sale);
    }

    // ── ADMIN: delete a sale ─────────────────────────────────────
    @DeleteMapping("/api/admin/sale/{id}")
    public String delete(@PathVariable Long id) {
        saleRepository.deleteById(id);
        return "Sale deleted";
    }

    // ── Request DTO ──────────────────────────────────────────────
    @Getter @Setter @NoArgsConstructor
    public static class SaleRequest {
        private String label;
        private Integer discountPercent;
        private LocalDate startDate;
        private LocalDate endDate;
    }
}
