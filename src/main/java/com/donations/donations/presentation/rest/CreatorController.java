package com.donations.donations.presentation.rest;

import com.donations.donations.application.usecase.*;
import com.donations.donations.domain.model.Creator;
import com.donations.donations.infrastructure.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/creators")
@RequiredArgsConstructor
public class CreatorController {

    private final GetCreatorProfileUseCase getCreatorProfileUseCase;
    private final SaveCreatorProfileUseCase saveCreatorProfileUseCase;
    private final CheckUsernameUseCase checkUsernameUseCase;
    private final SendPhoneOtpUseCase sendPhoneOtpUseCase;
    private final VerifyPhoneOtpUseCase verifyPhoneOtpUseCase;

    @PostMapping("/me/phone/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody java.util.Map<String, String> body, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        sendPhoneOtpUseCase.execute(userDetails.getId(), body.get("phoneNumber"));
        return ResponseEntity.ok(java.util.Map.of("message", "OTP sent"));
    }

    @PostMapping("/me/phone/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody java.util.Map<String, String> body, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        verifyPhoneOtpUseCase.execute(userDetails.getId(), body.get("phoneNumber"), body.get("otp"));
        return ResponseEntity.ok(java.util.Map.of("message", "Phone verified"));
    }

    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }
        boolean available = checkUsernameUseCase.execute(username, userDetails.getId());
        return ResponseEntity.ok(java.util.Map.of("available", available));
    }

    @GetMapping("/me")
    public ResponseEntity<Creator> getMyProfile(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        return getCreatorProfileUseCase.execute(userDetails.getId()).map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/me")
    public ResponseEntity<?> saveMyProfile(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody Creator input) {

        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            Creator saved = saveCreatorProfileUseCase.execute(userDetails.getId(), input);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }
}
