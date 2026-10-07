package com.donations.donations.application.usecase;

import com.donations.donations.application.port.out.CreatorPort;
import com.donations.donations.domain.model.Creator;
import com.donations.donations.domain.model.CreatorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaveCreatorProfileUseCase {

    private final CreatorPort creatorPort;

    @Transactional
    public Creator execute(UUID userId, Creator input) {
        Optional<Creator> existingOpt = creatorPort.findByUserId(userId);
        
        // If updating username, check uniqueness
        if (input.getUsername() != null && !input.getUsername().isBlank()) {
            Optional<Creator> byUsername = creatorPort.findByUsername(input.getUsername());
            if (byUsername.isPresent() && !byUsername.get().getUserId().equals(userId)) {
                throw new IllegalArgumentException("Username already taken");
            }
        }
        
        Creator creatorToSave;

        if (existingOpt.isPresent()) {
            Creator existing = existingOpt.get();
            // Update fields
            existing.setFullName(input.getFullName() != null ? input.getFullName() : existing.getFullName());
            existing.setUsername(input.getUsername() != null ? input.getUsername() : existing.getUsername());
            existing.setEmail(input.getEmail() != null ? input.getEmail() : existing.getEmail());
            // Phone is only updated via OTP verification
            existing.setCountry(input.getCountry() != null ? input.getCountry() : existing.getCountry());
            existing.setCity(input.getCity() != null ? input.getCity() : existing.getCity());
            existing.setCategory(input.getCategory() != null ? input.getCategory() : existing.getCategory());
            existing.setBio(input.getBio() != null ? input.getBio() : existing.getBio());
            existing.setAvatarUrl(input.getAvatarUrl() != null ? input.getAvatarUrl() : existing.getAvatarUrl());
            existing.setFacebookUrl(input.getFacebookUrl() != null ? input.getFacebookUrl() : existing.getFacebookUrl());
            existing.setTwitterUrl(input.getTwitterUrl() != null ? input.getTwitterUrl() : existing.getTwitterUrl());
            existing.setInstagramUrl(input.getInstagramUrl() != null ? input.getInstagramUrl() : existing.getInstagramUrl());
            existing.setYoutubeUrl(input.getYoutubeUrl() != null ? input.getYoutubeUrl() : existing.getYoutubeUrl());
            existing.setTiktokUrl(input.getTiktokUrl() != null ? input.getTiktokUrl() : existing.getTiktokUrl());
            
            creatorToSave = existing;
        } else {
            input.setUserId(userId);
            input.setStatus(CreatorStatus.DRAFT);
            creatorToSave = input;
        }

        return creatorPort.save(creatorToSave);
    }
}
