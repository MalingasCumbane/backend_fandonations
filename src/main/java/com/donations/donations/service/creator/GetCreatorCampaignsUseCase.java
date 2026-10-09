package com.donations.donations.service.creator;

import com.donations.donations.repository.CampaignRepository;
import com.donations.donations.model.Campaign;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCreatorCampaignsUseCase {
    private final CampaignRepository campaignRepository;

    public List<Campaign> execute(UUID creatorId) {
        return campaignRepository.findByCreatorId(creatorId);
    }
}
