package com.donations.donations.controller;

import com.donations.donations.service.*;
import com.donations.donations.model.Creator;
import com.donations.donations.security.UserDetailsImpl;
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
    public ResponseEntity<?> saveMyProfile(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody com.donations.donations.dto.creator.CreatorProfileRequestDto input) {

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
