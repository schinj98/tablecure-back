package com.example.tablecure.dashboard.controller;

import com.example.tablecure.dashboard.dto.DashboardResponse;
import com.example.tablecure.dashboard.service.DashboardService;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping
    public DashboardResponse getDashboard(Principal principal) {
        return dashboardService.getDashboard(principal.getName());
    }
}