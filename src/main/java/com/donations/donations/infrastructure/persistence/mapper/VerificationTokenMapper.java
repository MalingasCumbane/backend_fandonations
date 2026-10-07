package com.donations.donations.infrastructure.persistence.mapper;

import com.donations.donations.domain.model.VerificationToken;
import com.donations.donations.infrastructure.persistence.entity.VerificationTokenJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class VerificationTokenMapper {
    public VerificationToken toDomain(VerificationTokenJpaEntity entity) {
        if (entity == null) return null;
        return VerificationToken.builder()
                .id(entity.getId())
                .token(entity.getToken())
                .userId(entity.getUserId())
                .expiryDate(entity.getExpiryDate())
                .build();
    }

    public VerificationTokenJpaEntity toEntity(VerificationToken domain) {
        if (domain == null) return null;
        return VerificationTokenJpaEntity.builder()
                .id(domain.getId())
                .token(domain.getToken())
                .userId(domain.getUserId())
                .expiryDate(domain.getExpiryDate())
                .build();
    }
}
