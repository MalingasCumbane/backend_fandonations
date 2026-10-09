package com.donations.donations.repository;

import com.donations.donations.model.VerificationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, UUID> {
    Optional<VerificationRequest> findFirstByCreatorIdOrderByCreatedAtDesc(UUID creatorId);
    List<VerificationRequest> findByCreatorId(UUID creatorId);
}