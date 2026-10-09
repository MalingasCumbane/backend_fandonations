package com.donations.donations.infrastructure.persistence.repository;

import com.donations.donations.infrastructure.persistence.entity.VerificationRequestJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationRequestJpaRepository extends JpaRepository<VerificationRequestJpaEntity, UUID> {
    Optional<VerificationRequestJpaEntity> findFirstByCreatorIdOrderByCreatedAtDesc(UUID creatorId);
    List<VerificationRequestJpaEntity> findByCreatorId(UUID creatorId);
}