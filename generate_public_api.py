import os

base_pkg = "src/main/java/com/donations/donations"
usecase_dir = f"{base_pkg}/application/usecase/publics"
os.makedirs(usecase_dir, exist_ok=True)

usecases = {
    "GetPublicCreatorsUseCase.java": """package com.donations.donations.application.usecase.publics;

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
""",
    "GetPublicCreatorByUsernameUseCase.java": """package com.donations.donations.application.usecase.publics;

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
""",
}

for name, content in usecases.items():
    with open(f"{usecase_dir}/{name}", "w") as f:
        f.write(content)

print("Generated usecases")
