package com.donations.donations.infrastructure.persistence.adapter;

import com.donations.donations.application.port.out.PayoutPort;
import com.donations.donations.domain.model.Payout;
import com.donations.donations.infrastructure.persistence.entity.PayoutJpaEntity;
import com.donations.donations.infrastructure.persistence.mapper.PayoutMapper;
import com.donations.donations.infrastructure.persistence.repository.PayoutJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PayoutPersistenceAdapter implements PayoutPort {

    private final PayoutJpaRepository payoutRepository;

    @Override
    public Payout save(Payout payout) {
        PayoutJpaEntity entity = PayoutMapper.toEntity(payout);
        PayoutJpaEntity savedEntity = payoutRepository.save(entity);
        return PayoutMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Payout> findById(UUID id) {
        return payoutRepository.findById(id).map(PayoutMapper::toDomain);
    }

    @Override
    public List<Payout> findByCreatorId(UUID creatorId) {
        return payoutRepository.findByCreatorId(creatorId).stream()
                .map(PayoutMapper::toDomain)
                .collect(Collectors.toList());
    }
}
