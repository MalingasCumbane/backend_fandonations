package com.donations.donations.repository;

import com.donations.donations.model.Campaign;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CampaignRepository extends JpaRepository<Campaign, UUID> {
    List<Campaign> findByCreatorId(UUID creatorId);
    Page<Campaign> findByCreatorIdOrderByCreatedAtDesc(UUID creatorId, Pageable pageable);
}
