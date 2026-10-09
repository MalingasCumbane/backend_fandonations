package com.donations.donations.infrastructure.persistence.mapper;

import com.donations.donations.domain.model.Payout;
import com.donations.donations.infrastructure.persistence.entity.PayoutJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PayoutMapper {

    public static Payout toDomain(PayoutJpaEntity entity) {
        if (entity == null) return null;
        return Payout.builder()
                .id(entity.getId())
                .creatorId(entity.getCreatorId())
                .amount(entity.getAmount())
                .method(entity.getMethod())
                .phone(entity.getPhone())
                .status(entity.getStatus())
                .requestedAt(entity.getRequestedAt())
                .processedAt(entity.getProcessedAt())
                .adminNote(entity.getAdminNote())
                .transactionReference(entity.getTransactionReference())
                .build();
    }

    public static PayoutJpaEntity toEntity(Payout domain) {
        if (domain == null) return null;
        return PayoutJpaEntity.builder()
                .id(domain.getId())
                .creatorId(domain.getCreatorId())
                .amount(domain.getAmount())
                .method(domain.getMethod())
                .phone(domain.getPhone())
                .status(domain.getStatus())
                .requestedAt(domain.getRequestedAt())
                .processedAt(domain.getProcessedAt())
                .adminNote(domain.getAdminNote())
                .transactionReference(domain.getTransactionReference())
                .build();
    }
}
