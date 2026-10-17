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
    private final com.donations.donations.service.creator.UploadCreatorAvatarUseCase uploadCreatorAvatarUseCase;
    private final com.donations.donations.service.creator.RemoveCreatorAvatarUseCase removeCreatorAvatarUseCase;
    private final com.donations.donations.service.creator.RequestFullNameChangeUseCase requestFullNameChangeUseCase;

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

    @PutMapping("/me/settings")
    public ResponseEntity<?> updateSettings(@AuthenticationPrincipal UserDetailsImpl userDetails, @RequestBody com.donations.donations.dto.creator.CreatorProfileRequestDto input) {
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

    @PostMapping({"/me/full-name-change", "/me/request-identity-change"})
    public ResponseEntity<?> requestIdentityChange(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody java.util.Map<String, String> body
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        String fullName = body.get("fullName") != null ? body.get("fullName") : body.get("requestedFullName");
        String username = body.get("username") != null ? body.get("username") : body.get("requestedUsername");
        String reason = body.get("reason") != null ? body.get("reason") : body.get("nameChangeReason");

        try {
            Creator updated = requestFullNameChangeUseCase.execute(userDetails.getId(), fullName, username, reason);
            return ResponseEntity.ok(updated);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(java.util.Map.of("message", e.getMessage()));
        }
    }

    @PostMapping(value = "/me/avatar", consumes = {"multipart/form-data"})
    public ResponseEntity<?> uploadAvatarMultipart(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            String avatarUrl = uploadCreatorAvatarUseCase.execute(
                    userDetails.getId(),
                    file.getBytes(),
                    file.getContentType()
            );
            return ResponseEntity.ok(java.util.Map.of("avatarUrl", avatarUrl));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(java.util.Map.of("message", "Erro ao carregar a foto de perfil"));
        }
    }

    @PostMapping(value = "/me/avatar", consumes = {"application/json"})
    public ResponseEntity<?> uploadAvatarJson(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestBody java.util.Map<String, String> body) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        String dataUrl = body.get("avatar") != null ? body.get("avatar") : body.get("dataUrl");
        if (dataUrl == null || dataUrl.isBlank()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "Imagem não fornecida"));
        }

        try {
            byte[] bytes = com.donations.donations.service.storage.StorageFileHelper.extractBytes(dataUrl);
            String mime = com.donations.donations.service.storage.StorageFileHelper.extractMimeType(dataUrl, "image/jpeg");
            String avatarUrl = uploadCreatorAvatarUseCase.execute(userDetails.getId(), bytes, mime);
            return ResponseEntity.ok(java.util.Map.of("avatarUrl", avatarUrl));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(java.util.Map.of("message", "Erro ao carregar a foto de perfil"));
        }
    }

    @DeleteMapping("/me/avatar")
    public ResponseEntity<?> removeAvatar(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            removeCreatorAvatarUseCase.execute(userDetails.getId());
            return ResponseEntity.ok(java.util.Map.of("message", "Foto de perfil removida com sucesso"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(java.util.Map.of("message", e.getMessage()));
        }
    }
}
