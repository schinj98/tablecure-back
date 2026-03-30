package com.example.tablecure.otp.service;

import com.example.tablecure.email.EmailService;
import com.example.tablecure.entity.User;
import com.example.tablecure.otp.entity.EmailOtp;
import com.example.tablecure.otp.repository.EmailOtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final EmailOtpRepository emailOtpRepository;
    private final EmailService emailService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    private static final SecureRandom RANDOM = new SecureRandom();

    @Transactional
    public void generateAndSend(User user) {
        // Delete any existing OTPs for this email
        emailOtpRepository.deleteByEmail(user.getEmail());

        // Generate 6-digit OTP
        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));

        EmailOtp emailOtp = EmailOtp.builder()
                .email(user.getEmail())
                .otp(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        emailOtpRepository.save(emailOtp);

        emailService.sendVerificationEmail(user.getEmail(), user.getName(), otp, frontendUrl);
    }

    @Transactional
    public void generateAndSendPasswordReset(User user) {
        emailOtpRepository.deleteByEmail(user.getEmail());

        String otp = String.format("%06d", RANDOM.nextInt(1_000_000));

        EmailOtp emailOtp = EmailOtp.builder()
                .email(user.getEmail())
                .otp(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        emailOtpRepository.save(emailOtp);

        emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), otp);
    }

    @Transactional
    public void verifyOtp(String email, String otp) {
        EmailOtp emailOtp = emailOtpRepository.findByEmailAndOtpAndUsedFalse(email, otp)
                .orElseThrow(() -> new RuntimeException("INVALID_OTP"));

        if (emailOtp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP_EXPIRED");
        }

        emailOtp.setUsed(true);
        emailOtpRepository.save(emailOtp);
    }
}
