package com.donations.donations.controller;

import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.service.creator.GetCreatorVerificationUseCase;
import com.donations.donations.service.creator.CancelVerificationUseCase;
import com.donations.donations.service.creator.SubmitVerificationUseCase;
import com.donations.donations.model.VerificationRequest;
import com.donations.donations.security.UserDetailsImpl;
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
    private final CancelVerificationUseCase cancelVerificationUseCase;
    private final com.donations.donations.service.realtime.RealtimeEventPublisher realtimeEventPublisher;
    private final CreatorRepository creatorRepository;

    private UUID getCreatorId(UUID userId) {
        return creatorRepository.findByUserId(userId)
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

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        cancelVerificationUseCase.execute(getCreatorId(userDetails.getId()));
    }
}
