package com.example.tablecure.admin.controller;

import com.example.tablecure.entity.User;
import com.example.tablecure.auth.repository.UserRepository;
import com.example.tablecure.order.repository.OrderRepository;

import lombok.*;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository  userRepository;
    private final OrderRepository orderRepository;

    // ── GET ALL USERS ───────────────────────────────────────────
    @GetMapping
    public List<UserSummary> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserSummary(
                        u.getId(),
                        u.getName(),
                        u.getEmail(),
                        u.getRole(),
                        orderRepository.findByUser(u).size()
                ))
                .collect(Collectors.toList());
    }

    // ── GET SINGLE USER ─────────────────────────────────────────
    @GetMapping("/{id}")
    public UserSummary getUser(@PathVariable Long id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new UserSummary(
                u.getId(), u.getName(), u.getEmail(), u.getRole(),
                orderRepository.findByUser(u).size()
        );
    }

    // ── DTO ─────────────────────────────────────────────────────
    @Getter @AllArgsConstructor
    public static class UserSummary {
        private Long   id;
        private String name;
        private String email;
        private String role;
        private int    totalOrders;
    }
}