package com.donations.donations.service.publics;

import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.model.Creator;
import com.donations.donations.model.CreatorStatus;
import com.donations.donations.dto.publics.PublicCreator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetPublicCreatorsUseCase {
    private final CreatorRepository creatorRepository;

    public List<PublicCreator> execute(String query, String category) {
        List<Creator> creators = creatorRepository.findAll();
        return creators.stream()
                .filter(c -> c.getStatus() == CreatorStatus.VERIFIED)
                .filter(c -> category == null || (c.getCategory() != null && category.equalsIgnoreCase(c.getCategory().getName())))
                .filter(c -> query == null || (c.getFullName() != null && c.getFullName().toLowerCase().contains(query.toLowerCase())) || (c.getUsername() != null && c.getUsername().toLowerCase().contains(query.toLowerCase())))
                .map(c -> PublicCreator.builder()
                        .id(c.getId())
                        .userId(c.getUserId())
                        .username(c.getUsername())
                        .fullName(c.getFullName())
                        .country(c.getCountry() != null ? c.getCountry().getName() : null)
                        .city(c.getCity())
                        .category(c.getCategory() != null ? c.getCategory().getName() : null)
                        .bio(c.getBio())
                        .avatarUrl(c.getAvatarUrl())
                        .status(c.getStatus())
                        .socials(PublicCreator.Socials.builder()
                                .instagram(c.getInstagramUrl())
                                .tiktok(c.getTiktokUrl())
                                .youtube(c.getYoutubeUrl())
                                .facebook(c.getFacebookUrl())
                                .twitter(c.getTwitterUrl())
                                .build())
                        .createdAt(c.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }
}
