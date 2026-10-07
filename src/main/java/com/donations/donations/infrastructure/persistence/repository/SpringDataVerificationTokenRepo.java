package com.donations.donations.infrastructure.persistence.repository;

import com.donations.donations.infrastructure.persistence.entity.VerificationTokenJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataVerificationTokenRepo extends JpaRepository<VerificationTokenJpaEntity, UUID> {
    Optional<VerificationTokenJpaEntity> findByToken(String token);
}
