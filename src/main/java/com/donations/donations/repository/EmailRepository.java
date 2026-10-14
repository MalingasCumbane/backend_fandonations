package com.donations.donations.repository;

public interface EmailRepository {
    void sendVerificationEmail(String toEmail, String token);
    void sendWelcomeEmail(String toEmail);
    void sendPhoneOtpEmail(String toEmail, String otp, String phone);
    void sendAccountStatusEmail(String toEmail, String status, String note);
    void sendDonationReceiptEmail(String toEmail, String creatorName, String amount, String reference, String method);
    void sendPayoutRequestedAdminEmail(String adminEmail, String creatorName, String creatorUsername, String creatorEmail, String amount, String fee, String totalDeducted, String method, String phone, String date);
    void sendPayoutRequestedCreatorEmail(String creatorEmail, String creatorName, String amount, String method, String phone, String fee, String totalDeducted, String date);
    void sendPayoutApprovedEmail(String creatorEmail, String creatorName, String amount, String method, String phone, String transactionRef, String note, String date);
    void sendPayoutRejectedEmail(String creatorEmail, String creatorName, String amount, String method, String reason, String date);
    void sendPasswordResetEmail(String toEmail, String token);
    void sendPhoneRemovalOtpEmail(String toEmail, String otp, String phone, String provider);
    void sendPayoutOtpEmail(String toEmail, String otp, String amount, String method, String phone);
}
