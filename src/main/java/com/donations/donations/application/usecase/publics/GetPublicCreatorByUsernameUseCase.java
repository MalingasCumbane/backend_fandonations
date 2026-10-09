package com.donations.donations.application.usecase.publics;

import com.donations.donations.application.port.out.CreatorPort;
import com.donations.donations.domain.model.Creator;
import com.donations.donations.domain.model.CreatorStatus;
import com.donations.donations.presentation.rest.dto.publics.PublicCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class GetPublicCreatorByUsernameUseCase {
    private final CreatorPort creatorPort;

    public Map<String, Object> execute(String username) {
        Creator creator = creatorPort.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Criador não encontrado"));

        if (creator.getStatus() != CreatorStatus.VERIFIED) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Criador não encontrado");
        }

        PublicCreator pc = PublicCreator.builder()
                .id(creator.getId())
                .userId(creator.getUserId())
                .username(creator.getUsername())
                .country(creator.getCountry())
                .city(creator.getCity())
                .category(creator.getCategory())
                .bio(creator.getBio())
                .avatarUrl(creator.getAvatarUrl())
                .status(creator.getStatus())
                .createdAt(creator.getCreatedAt())
                .build();

        return Map.of(
                "creator", pc,
                "campaigns", java.util.List.of(),
                "stats", Map.of("supporterCount", 0, "donationCount", 0)
        );
    }
}
