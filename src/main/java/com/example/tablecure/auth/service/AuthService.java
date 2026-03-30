package com.example.tablecure.auth.service;

import com.example.tablecure.entity.User;
import com.example.tablecure.common.exception.UserAlreadyExistsException;
import com.example.tablecure.auth.repository.UserRepository;
import com.example.tablecure.otp.service.OtpService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OtpService otpService;

    public User register(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("User already exists");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        user.setEmailVerified(false);
        User savedUser = userRepository.save(user);
        otpService.generateAndSend(savedUser);
        return savedUser;
    }

    public User verifyEmail(String email, String otp) {
        otpService.verifyOtp(email, otp);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setEmailVerified(true);
        return userRepository.save(user);
    }

    public void forgotPassword(String email) {
        // Silently no-op for unknown emails — don't leak whether the account exists
        userRepository.findByEmail(email)
                .ifPresent(otpService::generateAndSendPasswordReset);
    }

    public void resetPassword(String email, String otp, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("INVALID_OTP"));
        otpService.verifyOtp(email, otp);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public User login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new RuntimeException("EMAIL_NOT_VERIFIED");
        }

        return user;
    }
}
