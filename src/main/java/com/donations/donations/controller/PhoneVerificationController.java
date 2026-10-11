package com.donations.donations.controller;

import com.donations.donations.security.UserDetailsImpl;
import com.donations.donations.service.SendPhoneOtpUseCase;
import com.donations.donations.service.VerifyPhoneOtpUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/creators/me/phone")
@RequiredArgsConstructor
public class PhoneVerificationController {

    private final SendPhoneOtpUseCase sendPhoneOtpUseCase;
    private final VerifyPhoneOtpUseCase verifyPhoneOtpUseCase;

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> body, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        sendPhoneOtpUseCase.execute(userDetails.getId(), body.get("phoneNumber"));
        return ResponseEntity.ok(Map.of("message", "OTP sent"));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> body, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        verifyPhoneOtpUseCase.execute(userDetails.getId(), body.get("phoneNumber"), body.get("otp"));
        return ResponseEntity.ok(Map.of("message", "Phone verified"));
    }
}
