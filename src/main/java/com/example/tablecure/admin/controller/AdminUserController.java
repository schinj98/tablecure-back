package com.example.tablecure.admin.controller;

import com.example.tablecure.email.EmailService;
import com.example.tablecure.entity.User;
import com.example.tablecure.auth.repository.UserRepository;
import com.example.tablecure.order.repository.OrderRepository;

import lombok.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository  userRepository;
    private final OrderRepository orderRepository;
    private final EmailService    emailService;

    // ── GET ALL USERS ───────────────────────────────────────────
    @GetMapping
    public List<UserSummary> getAllUsers() {
        return userRepository.findAll().stream()
                .map(u -> new UserSummary(
                        u.getId(),
                        u.getName(),
                        u.getEmail(),
                        u.getRole(),
                        orderRepository.countByUser(u)
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
                orderRepository.countByUser(u)
        );
    }

    // ── SEND EMAIL TO USER ──────────────────────────────────────
    @PostMapping("/{id}/email")
    public ResponseEntity<?> emailUser(
            @PathVariable Long id,
            @RequestBody EmailRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        emailService.sendCustomEmail(
                user.getEmail(),
                user.getName(),
                request.subject(),
                request.message()
        );

        return ResponseEntity.ok(Map.of("message", "Email sent to " + user.getEmail()));
    }

    // ── DTOs ─────────────────────────────────────────────────────
    public record EmailRequest(String subject, String message) {}

    @Getter @AllArgsConstructor
    public static class UserSummary {
        private Long   id;
        private String name;
        private String email;
        private String role;
        private int    totalOrders;
    }
}