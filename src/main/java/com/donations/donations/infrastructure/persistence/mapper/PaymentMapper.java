package com.donations.donations.infrastructure.persistence.mapper;

import com.donations.donations.domain.model.Payment;
import com.donations.donations.infrastructure.persistence.entity.PaymentJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public static Payment toDomain(PaymentJpaEntity entity) {
        if (entity == null) return null;
        return Payment.builder()
                .id(entity.getId())
                .donationId(entity.getDonationId())
                .providerReference(entity.getProviderReference())
                .thirdPartyReference(entity.getThirdPartyReference())
                .rawResponse(entity.getRawResponse())
                .build();
    }

    public static PaymentJpaEntity toEntity(Payment domain) {
        if (domain == null) return null;
        return PaymentJpaEntity.builder()
                .id(domain.getId())
                .donationId(domain.getDonationId())
                .providerReference(domain.getProviderReference())
                .thirdPartyReference(domain.getThirdPartyReference())
                .rawResponse(domain.getRawResponse())
                .build();
    }
}
