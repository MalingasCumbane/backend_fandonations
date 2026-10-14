package com.donations.donations.repository;

import com.donations.donations.model.SecurityOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SecurityOtpRepository extends JpaRepository<SecurityOtp, UUID> {
    Optional<SecurityOtp> findFirstByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(UUID userId, String purpose);
}
