package com.donations.donations.service.publics;

import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.model.Creator;
import com.donations.donations.model.CreatorStatus;
import com.donations.donations.dto.publics.PublicCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GetPublicCreatorByUsernameUseCase {
    private final CreatorRepository creatorRepository;

    public Map<String, Object> execute(String username) {
        Creator creator = creatorRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Criador não encontrado"));

        if (creator.getStatus() != CreatorStatus.VERIFIED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Criador não encontrado");
        }

        PublicCreator pc = PublicCreator.builder()
                .id(creator.getId())
                .userId(creator.getUserId())
                .username(creator.getUsername())
                .fullName(creator.getFullName())
                .country(creator.getCountry() != null ? creator.getCountry().getName() : null)
                .city(creator.getCity())
                .category(creator.getCategory() != null ? creator.getCategory().getName() : null)
                .bio(creator.getBio())
                .avatarUrl(creator.getAvatarUrl())
                .status(creator.getStatus())
                .socials(PublicCreator.Socials.builder()
                        .instagram(creator.getInstagramUrl())
                        .tiktok(creator.getTiktokUrl())
                        .youtube(creator.getYoutubeUrl())
                        .facebook(creator.getFacebookUrl())
                        .twitter(creator.getTwitterUrl())
                        .build())
                .createdAt(creator.getCreatedAt())
                .build();

        return Map.of(
                "creator", pc,
                "campaigns", java.util.List.of(),
                "stats", Map.of("supporterCount", 0, "donationCount", 0)
        );
    }
}
