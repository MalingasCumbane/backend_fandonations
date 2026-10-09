package com.donations.donations.repository;

import com.donations.donations.model.PhoneVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PhoneVerificationRepository extends JpaRepository<PhoneVerification, UUID> {
    Optional<PhoneVerification> findFirstByUserIdAndPhoneNumberOrderByCreatedAtDesc(UUID userId, String phoneNumber);
}
