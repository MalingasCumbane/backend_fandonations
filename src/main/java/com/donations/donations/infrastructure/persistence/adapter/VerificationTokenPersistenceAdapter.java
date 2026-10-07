package com.donations.donations.infrastructure.persistence.adapter;

import com.donations.donations.domain.model.VerificationToken;
import com.donations.donations.domain.repository.VerificationTokenRepository;
import com.donations.donations.infrastructure.persistence.entity.VerificationTokenJpaEntity;
import com.donations.donations.infrastructure.persistence.mapper.VerificationTokenMapper;
import com.donations.donations.infrastructure.persistence.repository.SpringDataVerificationTokenRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class VerificationTokenPersistenceAdapter implements VerificationTokenRepository {

    private final SpringDataVerificationTokenRepo springDataRepo;
    private final VerificationTokenMapper mapper;

    @Override
    public VerificationToken save(VerificationToken token) {
        VerificationTokenJpaEntity entity = mapper.toEntity(token);
        VerificationTokenJpaEntity savedEntity = springDataRepo.save(entity);
        return mapper.toDomain(savedEntity);
    }

    @Override
    public Optional<VerificationToken> findByToken(String token) {
        return springDataRepo.findByToken(token).map(mapper::toDomain);
    }

    @Override
    public void delete(VerificationToken token) {
        springDataRepo.delete(mapper.toEntity(token));
    }
}
