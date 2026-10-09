package com.donations.donations.application.usecase.publics;

import com.donations.donations.application.port.out.CreatorPort;
import com.donations.donations.domain.model.Creator;
import com.donations.donations.domain.model.CreatorStatus;
import com.donations.donations.presentation.rest.dto.publics.PublicCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetPublicCreatorsUseCase {
    private final CreatorPort creatorPort;

    public List<PublicCreator> execute(String query, String category) {
        // Mock implementation, update port later if needed
        List<Creator> creators = creatorPort.findAll();
        return creators.stream()
                .filter(c -> c.getStatus() == CreatorStatus.VERIFIED)
                .filter(c -> category == null || category.equalsIgnoreCase(c.getCategory()))
                .filter(c -> query == null || c.getFullName().toLowerCase().contains(query.toLowerCase()) || c.getUsername().toLowerCase().contains(query.toLowerCase()))
                .map(c -> PublicCreator.builder()
                        .id(c.getId())
                        .userId(c.getUserId())
                        .username(c.getUsername())
                        .country(c.getCountry())
                        .city(c.getCity())
                        .category(c.getCategory())
                        .bio(c.getBio())
                        .avatarUrl(c.getAvatarUrl())
                        .status(c.getStatus())
                        .createdAt(c.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
