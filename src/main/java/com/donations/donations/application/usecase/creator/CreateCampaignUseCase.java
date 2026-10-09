package com.donations.donations.application.usecase.creator;

import com.donations.donations.application.port.out.CampaignPort;
import com.donations.donations.domain.model.Campaign;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateCampaignUseCase {
    private final CampaignPort campaignPort;

    public Campaign execute(Campaign campaign) {
        campaign.setStartDate(java.time.LocalDate.now());
        return campaignPort.save(campaign);
    }
}
