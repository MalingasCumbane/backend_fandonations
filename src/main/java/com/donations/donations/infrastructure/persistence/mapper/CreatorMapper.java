package com.donations.donations.infrastructure.persistence.mapper;

import com.donations.donations.domain.model.Creator;
import com.donations.donations.infrastructure.persistence.entity.CreatorJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CreatorMapper {

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
                .country(entity.getCountry())
                .city(entity.getCity())
                .category(entity.getCategory())
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
                .country(domain.getCountry())
                .city(domain.getCity())
                .category(domain.getCategory())
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
                .build();
                
        // Initialize Base fields if new
        if (domain.getId() == null) {
            entity.setIsActive(true);
            entity.setIsDeleted(false);
            entity.setCreatedBy("system");
            entity.setCreatedIp("127.0.0.1");
            entity.setReference(java.util.UUID.randomUUID().toString().substring(0, 8));
        }
        
        return entity;
    }
}
