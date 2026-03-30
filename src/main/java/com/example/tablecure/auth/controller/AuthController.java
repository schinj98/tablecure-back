package com.example.tablecure.auth.controller;

import com.example.tablecure.auth.dto.AuthResponse;
import com.example.tablecure.auth.dto.ForgotPasswordRequest;
import com.example.tablecure.auth.dto.LoginRequest;
import com.example.tablecure.auth.dto.ResetPasswordRequest;
import com.example.tablecure.auth.repository.UserRepository;
import com.example.tablecure.entity.User;
import com.example.tablecure.auth.service.AuthService;
import com.example.tablecure.auth.util.JwtUtil;
import com.example.tablecure.otp.dto.ResendOtpRequest;
import com.example.tablecure.otp.dto.VerifyOtpRequest;
import com.example.tablecure.otp.service.OtpService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;
    private final OtpService otpService;

    // REGISTER — saves user and sends OTP email; does NOT return JWT
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            authService.register(user);
            return ResponseEntity.ok(Map.of(
                    "message", "Registration successful! Please check your email for the verification OTP."
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", e.getMessage()));
        }
    }

    // VERIFY EMAIL — submit OTP to get JWT
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody VerifyOtpRequest request) {
        try {
            User user = authService.verifyEmail(request.email(), request.otp());
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (RuntimeException e) {
            String msg = switch (e.getMessage()) {
                case "INVALID_OTP" -> "Invalid or already used OTP.";
                case "OTP_EXPIRED"  -> "OTP has expired. Please request a new one.";
                default             -> e.getMessage();
            };
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", msg));
        }
    }

    // RESEND OTP
    @PostMapping("/resend-otp")
    public ResponseEntity<?> resendOtp(@RequestBody ResendOtpRequest request) {
        try {
            User user = userRepository.findByEmail(request.email())
                    .orElseThrow(() -> new RuntimeException("No account found with this email."));
            if (Boolean.TRUE.equals(user.getEmailVerified())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is already verified."));
            }
            otpService.generateAndSend(user);
            return ResponseEntity.ok(Map.of("message", "A new OTP has been sent to your email."));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me")
    public User getCurrentUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName()).orElseThrow();
    }

    // FORGOT PASSWORD — sends OTP to email
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.email());
        // Always return success — don't reveal whether the email is registered
        return ResponseEntity.ok(Map.of("message", "If an account with that email exists, a reset OTP has been sent."));
    }

    // RESET PASSWORD — verify OTP + set new password
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            authService.resetPassword(request.email(), request.otp(), request.newPassword());
            return ResponseEntity.ok(Map.of("message", "Password reset successful. Please log in with your new password."));
        } catch (RuntimeException e) {
            String msg = switch (e.getMessage()) {
                case "INVALID_OTP" -> "Invalid or already used OTP.";
                case "OTP_EXPIRED"  -> "OTP has expired. Please request a new one.";
                default             -> e.getMessage();
            };
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", msg));
        }
    }

    // LOGIN
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            User user = authService.login(request.getEmail(), request.getPassword());
            String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (RuntimeException e) {
            if ("EMAIL_NOT_VERIFIED".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("error", "Please verify your email before logging in."));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password"));
        }
    }
}
