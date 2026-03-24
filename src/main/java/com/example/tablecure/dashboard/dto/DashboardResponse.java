package com.example.tablecure.dashboard.dto;

import com.example.tablecure.entity.User;
import com.example.tablecure.entity.Address;
import com.example.tablecure.entity.Order;

import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardResponse {

    private User user;
    private List<Address> addresses;
    private List<Order> orders;
}