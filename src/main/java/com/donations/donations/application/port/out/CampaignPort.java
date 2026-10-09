package com.donations.donations.application.port.out;

import com.donations.donations.domain.model.Campaign;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CampaignPort {
    Campaign save(Campaign campaign);
    Optional<Campaign> findById(UUID id);
    List<Campaign> findByCreatorId(UUID creatorId);
    void deleteById(UUID id);
}
