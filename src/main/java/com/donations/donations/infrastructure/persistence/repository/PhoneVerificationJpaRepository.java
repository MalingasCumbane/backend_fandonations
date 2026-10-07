package com.donations.donations.infrastructure.persistence.repository;

import com.donations.donations.infrastructure.persistence.entity.PhoneVerificationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PhoneVerificationJpaRepository extends JpaRepository<PhoneVerificationJpaEntity, UUID> {
    Optional<PhoneVerificationJpaEntity> findFirstByUserIdAndPhoneNumberOrderByCreatedAtDesc(UUID userId, String phoneNumber);
}
