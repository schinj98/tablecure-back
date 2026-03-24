package com.example.tablecure.auth.service;

import com.example.tablecure.entity.User;
import com.example.tablecure.common.exception.UserAlreadyExistsException;
import com.example.tablecure.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        return userRepository.save(user);
    }

    public User login(String email, String password) {
        System.out.println("RAW: [" + password + "]");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("MATCH: " + passwordEncoder.matches(password, user.getPassword()));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            System.out.println("DB: [" + user.getPassword() + "]");
            throw new RuntimeException("Invalid password");
        }

        return user;
    }


}