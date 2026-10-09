package com.donations.donations.infrastructure.persistence.mapper;

import com.donations.donations.domain.model.PlatformSettings;
import com.donations.donations.infrastructure.persistence.entity.PlatformSettingsJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PlatformSettingsMapper {

    public static PlatformSettings toDomain(PlatformSettingsJpaEntity entity) {
        if (entity == null) return null;
        return PlatformSettings.builder()
                .id(entity.getId())
                .platformFeePercentage(entity.getPlatformFeePercentage())
                .minPayoutAmount(entity.getMinPayoutAmount())
                .payoutsEnabled(entity.isPayoutsEnabled())
                .registrationsOpen(entity.isRegistrationsOpen())
                .build();
    }

    public static PlatformSettingsJpaEntity toEntity(PlatformSettings domain) {
        if (domain == null) return null;
        return PlatformSettingsJpaEntity.builder()
                .id(domain.getId())
                .platformFeePercentage(domain.getPlatformFeePercentage())
                .minPayoutAmount(domain.getMinPayoutAmount())
                .payoutsEnabled(domain.isPayoutsEnabled())
                .registrationsOpen(domain.isRegistrationsOpen())
                .build();
    }
}
