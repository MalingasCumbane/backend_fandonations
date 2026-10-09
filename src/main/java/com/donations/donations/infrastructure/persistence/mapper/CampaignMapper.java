package com.donations.donations.infrastructure.persistence.mapper;

import com.donations.donations.domain.model.Campaign;
import com.donations.donations.infrastructure.persistence.entity.CampaignJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CampaignMapper {

    public static Campaign toDomain(CampaignJpaEntity entity) {
        if (entity == null) return null;
        return Campaign.builder()
                .id(entity.getId())
                .creatorId(entity.getCreatorId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .goalAmount(entity.getGoalAmount())
                .raisedAmount(entity.getRaisedAmount())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .status(entity.getStatus())
                .build();
    }

    public static CampaignJpaEntity toEntity(Campaign domain) {
        if (domain == null) return null;
        return CampaignJpaEntity.builder()
                .id(domain.getId())
                .creatorId(domain.getCreatorId())
                .title(domain.getTitle())
                .description(domain.getDescription())
                .imageUrl(domain.getImageUrl())
                .goalAmount(domain.getGoalAmount())
                .raisedAmount(domain.getRaisedAmount())
                .startDate(domain.getStartDate())
                .endDate(domain.getEndDate())
                .status(domain.getStatus())
                .build();
    }
}
