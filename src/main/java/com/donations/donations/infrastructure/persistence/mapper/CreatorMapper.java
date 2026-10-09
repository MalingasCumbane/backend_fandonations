package com.donations.donations.infrastructure.persistence.mapper;

import com.donations.donations.domain.model.Creator;
import com.donations.donations.infrastructure.persistence.entity.CreatorJpaEntity;
import org.springframework.stereotype.Component;
import com.donations.donations.infrastructure.persistence.repository.CountryJpaRepository;
import com.donations.donations.infrastructure.persistence.repository.CategoryJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;


@Component
public class CreatorMapper {

    @Autowired
    private CountryJpaRepository countryRepository;

    @Autowired
    private CategoryJpaRepository categoryRepository;


    public Creator toDomain(CreatorJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return Creator.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .fullName(entity.getFullName())
                .username(entity.getUsername())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .country(entity.getCountry() != null ? entity.getCountry().getName() : null)
                .city(entity.getCity())
                .category(entity.getCategory() != null ? entity.getCategory().getName() : null)
                .bio(entity.getBio())
                .avatarUrl(entity.getAvatarUrl())
                .status(entity.getStatus())
                .facebookUrl(entity.getFacebookUrl())
                .twitterUrl(entity.getTwitterUrl())
                .instagramUrl(entity.getInstagramUrl())
                .youtubeUrl(entity.getYoutubeUrl())
                .tiktokUrl(entity.getTiktokUrl())
                .adminNote(entity.getAdminNote())
                .rejectionReason(entity.getRejectionReason())
                .requestedFullName(entity.getRequestedFullName())
                .submittedAt(entity.getSubmittedAt())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public CreatorJpaEntity toEntity(Creator domain) {
        if (domain == null) {
            return null;
        }

        CreatorJpaEntity entity = CreatorJpaEntity.builder()
                .id(domain.getId())
                .userId(domain.getUserId())
                .fullName(domain.getFullName())
                .username(domain.getUsername())
                .email(domain.getEmail())
                .phone(domain.getPhone())
                .city(domain.getCity())
                .bio(domain.getBio())
                .avatarUrl(domain.getAvatarUrl())
                .status(domain.getStatus())
                .facebookUrl(domain.getFacebookUrl())
                .twitterUrl(domain.getTwitterUrl())
                .instagramUrl(domain.getInstagramUrl())
                .youtubeUrl(domain.getYoutubeUrl())
                .tiktokUrl(domain.getTiktokUrl())
                .adminNote(domain.getAdminNote())
                .rejectionReason(domain.getRejectionReason())
                .requestedFullName(domain.getRequestedFullName())
                .submittedAt(domain.getSubmittedAt())
                .build();

        // Initialize Base fields if new
        if (domain.getId() == null) {
            entity.setIsActive(true);
            entity.setIsDeleted(false);
            entity.setCreatedBy("system");
            entity.setCreatedIp("127.0.0.1");
            entity.setReference(java.util.UUID.randomUUID().toString().substring(0, 8));
        }


        if (domain.getCountry() != null && !domain.getCountry().isEmpty()) {
            entity.setCountry(countryRepository.findAll().stream()
                    .filter(c -> c.getName().equalsIgnoreCase(domain.getCountry()) || c.getCode().equalsIgnoreCase(domain.getCountry()))
                    .findFirst().orElse(null));
        } else {
            entity.setCountry(null);
        }

        if (domain.getCategory() != null && !domain.getCategory().isEmpty()) {
            entity.setCategory(categoryRepository.findAll().stream()
                    .filter(c -> c.getName().equalsIgnoreCase(domain.getCategory()))
                    .findFirst().orElse(null));
        } else {
            entity.setCategory(null);
        }

        return entity;
    }
}
