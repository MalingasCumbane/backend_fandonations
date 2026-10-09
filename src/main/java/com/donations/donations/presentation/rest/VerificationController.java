package com.donations.donations.presentation.rest;

import com.donations.donations.application.port.out.CreatorPort;
import com.donations.donations.application.usecase.creator.GetCreatorVerificationUseCase;
import com.donations.donations.application.usecase.creator.SubmitVerificationUseCase;
import com.donations.donations.domain.model.VerificationRequest;
import com.donations.donations.infrastructure.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/verifications")
@RequiredArgsConstructor
public class VerificationController {

    private final SubmitVerificationUseCase submitVerificationUseCase;
    private final GetCreatorVerificationUseCase getCreatorVerificationUseCase;
    private final CreatorPort creatorPort;

    private UUID getCreatorId(UUID userId) {
        return creatorPort.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Creator not found"))
                .getId();
    }

    @GetMapping("/me")
    public ResponseEntity<VerificationRequest> getMine(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        return getCreatorVerificationUseCase.execute(getCreatorId(userDetails.getId()))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @PostMapping("/me")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<VerificationRequest> submit(@RequestBody VerificationRequest request, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(submitVerificationUseCase.execute(getCreatorId(userDetails.getId()), request));
    }
}