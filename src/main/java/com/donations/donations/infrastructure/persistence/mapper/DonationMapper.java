package com.donations.donations.infrastructure.persistence.mapper;

import com.donations.donations.domain.model.Donation;
import com.donations.donations.infrastructure.persistence.entity.DonationJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class DonationMapper {

    public static Donation toDomain(DonationJpaEntity entity) {
        if (entity == null) return null;
        return Donation.builder()
            .id(entity.getId())
            .reference(entity.getReference())
            .creatorId(entity.getCreatorId())
            .campaignId(entity.getCampaignId())
            .supporterName(entity.getSupporterName())
            .supporterEmail(entity.getSupporterEmail())
            .supporterPhone(entity.getSupporterPhone())
            .message(entity.getMessage())
            .anonymous(entity.isAnonymous())
            .amount(entity.getAmount())
            .feeAmount(entity.getFeeAmount())
            .netAmount(entity.getNetAmount())
            .method(entity.getMethod())
            .status(entity.getStatus())
            .failureReason(entity.getFailureReason())
            .createdAt(entity.getCreatedAt())
            .build();
    }

    public static DonationJpaEntity toEntity(Donation domain) {
        if (domain == null) return null;
        DonationJpaEntity entity = DonationJpaEntity.builder()
            .id(domain.getId())
            .reference(domain.getReference())
            .creatorId(domain.getCreatorId())
            .campaignId(domain.getCampaignId())
            .supporterName(domain.getSupporterName())
            .supporterEmail(domain.getSupporterEmail())
            .supporterPhone(domain.getSupporterPhone())
            .message(domain.getMessage())
            .anonymous(domain.isAnonymous())
            .amount(domain.getAmount())
            .feeAmount(domain.getFeeAmount())
            .netAmount(domain.getNetAmount())
            .method(domain.getMethod())
            .status(domain.getStatus())
            .failureReason(domain.getFailureReason())
            .build();
            
        // Initialize Base fields if new
        if (domain.getId() == null) {
            entity.setIsActive(true);
            entity.setIsDeleted(false);
            entity.setCreatedBy("system");
            entity.setCreatedIp("127.0.0.1");
            if (domain.getReference() == null) {
                entity.setReference(java.util.UUID.randomUUID().toString().substring(0, 8));
            }
        }
        
        return entity;
    }
}
