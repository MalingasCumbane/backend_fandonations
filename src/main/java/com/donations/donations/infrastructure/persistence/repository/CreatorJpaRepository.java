package com.donations.donations.infrastructure.persistence.repository;

import com.donations.donations.infrastructure.persistence.entity.CreatorJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CreatorJpaRepository extends JpaRepository<CreatorJpaEntity, UUID> {
    Optional<CreatorJpaEntity> findByUserId(UUID userId);
    Optional<CreatorJpaEntity> findByUsername(String username);
}
