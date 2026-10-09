package com.donations.donations.repository;

public interface EmailRepository {
    void sendVerificationEmail(String toEmail, String token);
    void sendWelcomeEmail(String toEmail);
    void sendPhoneOtpEmail(String toEmail, String otp, String phone);
    void sendAccountStatusEmail(String toEmail, String status, String note);
    void sendDonationReceiptEmail(String toEmail, String creatorName, String amount, String reference);
}
