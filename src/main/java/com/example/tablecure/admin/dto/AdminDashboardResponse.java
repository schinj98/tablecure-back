package com.example.tablecure.admin.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AdminDashboardResponse {

    private long totalOrders;
    private long paidOrders;       // only PAID
    private long pendingOrders;    // CREATED / PENDING
    private double totalRevenue;   // sum of confirmed paid order amounts
    private long totalUsers;
    private long totalProducts;
}