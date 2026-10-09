package com.donations.donations.infrastructure.persistence.adapter;

import com.donations.donations.application.port.out.PaymentPort;
import com.donations.donations.domain.model.Payment;
import com.donations.donations.infrastructure.persistence.entity.PaymentJpaEntity;
import com.donations.donations.infrastructure.persistence.mapper.PaymentMapper;
import com.donations.donations.infrastructure.persistence.repository.PaymentJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PaymentPersistenceAdapter implements PaymentPort {

    private final PaymentJpaRepository paymentRepository;

    @Override
    public Payment save(Payment payment) {
        PaymentJpaEntity entity = PaymentMapper.toEntity(payment);
        PaymentJpaEntity savedEntity = paymentRepository.save(entity);
        return PaymentMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Payment> findById(UUID id) {
        return paymentRepository.findById(id).map(PaymentMapper::toDomain);
    }
}
