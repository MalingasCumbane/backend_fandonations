package com.donations.donations.repository;

import com.donations.donations.model.Donation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DonationRepository extends JpaRepository<Donation, UUID> {
    List<Donation> findByCreatorId(UUID creatorId);
    Page<Donation> findByCreatorIdOrderByCreatedAtDesc(UUID creatorId, Pageable pageable);
    Page<Donation> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Optional<Donation> findByReference(String reference);
}
