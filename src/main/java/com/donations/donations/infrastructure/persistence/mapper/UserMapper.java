package com.donations.donations.infrastructure.persistence.mapper;

import com.donations.donations.domain.model.User;
import com.donations.donations.infrastructure.persistence.entity.UserJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public User toDomain(UserJpaEntity entity) {
        if (entity == null) return null;
        return User.builder()
                .id(entity.getId())
                .email(entity.getEmail())
                .role(entity.getRole())
                .passwordHash(entity.getPasswordHash())
                .active(entity.getIsActive() != null && entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public UserJpaEntity toEntity(User domain) {
        if (domain == null) return null;
        return UserJpaEntity.builder()
                .id(domain.getId())
                .email(domain.getEmail())
                .role(domain.getRole())
                .passwordHash(domain.getPasswordHash())
                .isActive(domain.isActive())
                .createdAt(domain.getCreatedAt())
                .build();
    }
}
