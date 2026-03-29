package com.example.tablecure.otp.repository;

import com.example.tablecure.otp.entity.EmailOtp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EmailOtpRepository extends JpaRepository<EmailOtp, Long> {

    Optional<EmailOtp> findByEmailAndOtpAndUsedFalse(String email, String otp);

    void deleteByEmail(String email);
}
