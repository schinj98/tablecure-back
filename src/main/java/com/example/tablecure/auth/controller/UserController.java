package com.example.tablecure.auth.controller;

import com.example.tablecure.entity.User;
import com.example.tablecure.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    // ✅ GET USER
    @GetMapping("/me")
    public User getUser(Principal principal) {
        return userRepository.findByEmail(principal.getName())
                .orElseThrow();
    }

    // ✅ UPDATE USER
    @PutMapping
    public User updateUser(@RequestBody User updatedUser,
                           Principal principal) {

        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow();

        user.setName(updatedUser.getName());
        user.setEmail(updatedUser.getEmail());

        return userRepository.save(user);
    }
}