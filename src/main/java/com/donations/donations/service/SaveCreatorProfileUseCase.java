package com.donations.donations.service;

import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.repository.CountryRepository;
import com.donations.donations.repository.CategoryRepository;
import com.donations.donations.model.Creator;
import com.donations.donations.model.Country;
import com.donations.donations.model.Category;
import com.donations.donations.model.CreatorStatus;
import com.donations.donations.dto.creator.CreatorProfileRequestDto;
import com.donations.donations.service.storage.FileStoragePort;
import com.donations.donations.service.storage.StorageFileHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaveCreatorProfileUseCase {

    private final CreatorRepository creatorRepository;
    private final CountryRepository countryRepository;
    private final CategoryRepository categoryRepository;
    private final FileStoragePort fileStoragePort;

    @Transactional
    public Creator execute(UUID userId, CreatorProfileRequestDto input) {
        Optional<Creator> existingOpt = creatorRepository.findByUserId(userId);

        // If updating username, check uniqueness
        if (input.getUsername() != null && !input.getUsername().isBlank()) {
            Optional<Creator> byUsername = creatorRepository.findByUsername(input.getUsername());
            if (byUsername.isPresent() && !byUsername.get().getUserId().equals(userId)) {
                throw new IllegalArgumentException("Username already taken");
            }
        }

        Country country = null;
        if (input.getCountry() != null && !input.getCountry().isBlank()) {
            try {
                country = countryRepository.findById(UUID.fromString(input.getCountry())).orElse(null);
            } catch (IllegalArgumentException e) {
                country = countryRepository.findByName(input.getCountry()).orElse(null);
            }
        }

        Category category = null;
        if (input.getCategory() != null && !input.getCategory().isBlank()) {
            try {
                category = categoryRepository.findById(UUID.fromString(input.getCategory())).orElse(null);
            } catch (IllegalArgumentException e) {
                category = categoryRepository.findByName(input.getCategory()).orElse(null);
            }
        }

        Creator creatorToSave;

        if (existingOpt.isPresent()) {
            Creator existing = existingOpt.get();

            String processedAvatar = processAvatar(existing.getId(), input.getAvatarUrl(), existing.getAvatarUrl());

            if (existing.getStatus() == CreatorStatus.VERIFIED) {
                // If verified, only allow updating socials, avatar and marketing preferences.
                existing.setFacebookUrl(input.getFacebookUrl() != null ? input.getFacebookUrl() : existing.getFacebookUrl());
                existing.setTwitterUrl(input.getTwitterUrl() != null ? input.getTwitterUrl() : existing.getTwitterUrl());
                existing.setInstagramUrl(input.getInstagramUrl() != null ? input.getInstagramUrl() : existing.getInstagramUrl());
                existing.setYoutubeUrl(input.getYoutubeUrl() != null ? input.getYoutubeUrl() : existing.getYoutubeUrl());
                existing.setTiktokUrl(input.getTiktokUrl() != null ? input.getTiktokUrl() : existing.getTiktokUrl());
                existing.setAcceptsMarketing(input.getAcceptsMarketing() != null ? input.getAcceptsMarketing() : existing.isAcceptsMarketing());
                existing.setAvatarUrl(processedAvatar);
                return creatorRepository.save(existing);
            }

            // Update fields
            existing.setFullName(input.getFullName() != null ? input.getFullName() : existing.getFullName());
            existing.setUsername(input.getUsername() != null ? input.getUsername() : existing.getUsername());
            existing.setEmail(input.getEmail() != null ? input.getEmail() : existing.getEmail());

            existing.setCountry(country != null ? country : existing.getCountry());
            existing.setCity(input.getCity() != null ? input.getCity() : existing.getCity());
            existing.setCategory(category != null ? category : existing.getCategory());
            existing.setBio(input.getBio() != null ? input.getBio() : existing.getBio());
            existing.setAvatarUrl(processedAvatar);
            existing.setFacebookUrl(input.getFacebookUrl() != null ? input.getFacebookUrl() : existing.getFacebookUrl());
            existing.setTwitterUrl(input.getTwitterUrl() != null ? input.getTwitterUrl() : existing.getTwitterUrl());
            existing.setInstagramUrl(input.getInstagramUrl() != null ? input.getInstagramUrl() : existing.getInstagramUrl());
            existing.setYoutubeUrl(input.getYoutubeUrl() != null ? input.getYoutubeUrl() : existing.getYoutubeUrl());
            existing.setTiktokUrl(input.getTiktokUrl() != null ? input.getTiktokUrl() : existing.getTiktokUrl());
            existing.setAcceptsMarketing(input.getAcceptsMarketing() != null ? input.getAcceptsMarketing() : existing.isAcceptsMarketing());

            creatorToSave = existing;
        } else {
            UUID newCreatorId = UUID.randomUUID();
            String processedAvatar = processAvatar(newCreatorId, input.getAvatarUrl(), null);

            creatorToSave = Creator.builder()
                    .id(newCreatorId)
                    .userId(userId)
                    .fullName(input.getFullName())
                    .username(input.getUsername())
                    .email(input.getEmail())
                    .country(country)
                    .city(input.getCity())
                    .category(category)
                    .bio(input.getBio())
                    .avatarUrl(processedAvatar)
                    .facebookUrl(input.getFacebookUrl())
                    .twitterUrl(input.getTwitterUrl())
                    .instagramUrl(input.getInstagramUrl())
                    .youtubeUrl(input.getYoutubeUrl())
                    .tiktokUrl(input.getTiktokUrl())
                    .acceptsMarketing(input.getAcceptsMarketing() != null ? input.getAcceptsMarketing() : false)
                    .status(CreatorStatus.DRAFT)
                    .build();
        }

        return creatorRepository.save(creatorToSave);
    }

    private String processAvatar(UUID creatorId, String avatarInput, String currentAvatar) {
        if (avatarInput == null || avatarInput.isBlank()) {
            return currentAvatar;
        }
        if (StorageFileHelper.isDataUrlOrBase64(avatarInput)) {
            try {
                byte[] bytes = StorageFileHelper.extractBytes(avatarInput);
                String mime = StorageFileHelper.extractMimeType(avatarInput, "image/jpeg");
                String ext = StorageFileHelper.getExtensionFromMime(mime);
                String key = "avatars/" + creatorId + "/avatar_" + System.currentTimeMillis() + "." + ext;
                fileStoragePort.upload(key, bytes, mime);
                return fileStoragePort.resolveUrl(key);
            } catch (Exception e) {
                return currentAvatar;
            }
        }
        return avatarInput;
    }
}
