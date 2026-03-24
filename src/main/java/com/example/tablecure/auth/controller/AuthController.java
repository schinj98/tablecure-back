package com.example.tablecure.auth.controller;

import com.example.tablecure.auth.dto.AuthResponse;
import com.example.tablecure.auth.dto.LoginRequest;
import com.example.tablecure.auth.repository.UserRepository;
import com.example.tablecure.entity.User;
import com.example.tablecure.auth.service.AuthService;
import com.example.tablecure.auth.util.JwtUtil;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    private final UserRepository userRepository;
    // ✅ REGISTER
    @PostMapping("/register")
    public AuthResponse register(@RequestBody User user) {

        User savedUser = authService.register(user);

        String token = jwtUtil.generateToken(
                savedUser.getEmail(),
                savedUser.getRole()
        );

        return new AuthResponse(token);
    }
    @GetMapping("/me")
    public User getCurrentUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName()).orElseThrow();
    }

    // ✅ LOGIN
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {

        User user = authService.login(
                request.getEmail(),
                request.getPassword()
        );

        String token = jwtUtil.generateToken(
                user.getEmail(),
                user.getRole()
        );

        return new AuthResponse(token);
    }
}