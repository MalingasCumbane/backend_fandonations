package com.donations.donations.infrastructure.persistence.adapter;

import com.donations.donations.application.port.out.VerificationRequestPort;
import com.donations.donations.domain.model.VerificationRequest;
import com.donations.donations.infrastructure.persistence.entity.VerificationRequestJpaEntity;
import com.donations.donations.infrastructure.persistence.mapper.VerificationRequestMapper;
import com.donations.donations.infrastructure.persistence.repository.VerificationRequestJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class VerificationRequestPersistenceAdapter implements VerificationRequestPort {

    private final VerificationRequestJpaRepository verificationrequestRepository;

    @Override
    public VerificationRequest save(VerificationRequest verificationrequest) {
        VerificationRequestJpaEntity entity = VerificationRequestMapper.toEntity(verificationrequest);
        VerificationRequestJpaEntity savedEntity = verificationrequestRepository.save(entity);
        return VerificationRequestMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<VerificationRequest> findById(UUID id) {
        return verificationrequestRepository.findById(id).map(VerificationRequestMapper::toDomain);
    }

    @Override
    public Optional<VerificationRequest> findByCreatorId(UUID creatorId) {
        // Needs custom method in JPA repo, assuming findByCreatorId exists, or we get all and filter
        return verificationrequestRepository.findAll().stream().filter(e -> e.getCreatorId().equals(creatorId)).map(VerificationRequestMapper::toDomain).findFirst();
    }
}