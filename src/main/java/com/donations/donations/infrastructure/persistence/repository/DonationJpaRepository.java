package com.donations.donations.infrastructure.persistence.repository;

import com.donations.donations.infrastructure.persistence.entity.DonationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DonationJpaRepository extends JpaRepository<DonationJpaEntity, UUID> {
    List<DonationJpaEntity> findByCreatorId(UUID creatorId);

    Optional<DonationJpaEntity> findByReference(String reference);
}
