package com.donations.donations.infrastructure.persistence.repository;

import com.donations.donations.infrastructure.persistence.entity.CampaignJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CampaignJpaRepository extends JpaRepository<CampaignJpaEntity, UUID> {
    List<CampaignJpaEntity> findByCreatorId(UUID creatorId);
}
