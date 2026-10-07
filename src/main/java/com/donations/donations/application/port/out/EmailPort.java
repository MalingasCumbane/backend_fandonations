package com.donations.donations.application.port.out;

public interface EmailPort {
    void sendVerificationEmail(String toEmail, String token);
    void sendWelcomeEmail(String toEmail);
    void sendPhoneOtpEmail(String toEmail, String otp, String phone);
}
