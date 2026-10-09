package com.donations.donations.application.usecase.creator;

import com.donations.donations.application.port.out.CampaignPort;
import com.donations.donations.domain.model.Campaign;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCreatorCampaignsUseCase {
    private final CampaignPort campaignPort;

    public List<Campaign> execute(UUID creatorId) {
        return campaignPort.findByCreatorId(creatorId);
    }
}
