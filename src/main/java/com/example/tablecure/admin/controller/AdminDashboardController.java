package com.example.tablecure.admin.controller;

import com.example.tablecure.admin.dto.AdminDashboardResponse;
import com.example.tablecure.admin.service.AdminDashboardService;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService service;

    @GetMapping
    public AdminDashboardResponse getStats() {
        return service.getStats();
    }
}