package com.example.tablecure.auth.dto;

public record ResetPasswordRequest(String email, String otp, String newPassword) {}
