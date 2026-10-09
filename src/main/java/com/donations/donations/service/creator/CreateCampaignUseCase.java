package com.donations.donations.service.creator;

import com.donations.donations.repository.CampaignRepository;
import com.donations.donations.model.Campaign;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CreateCampaignUseCase {
    private final CampaignRepository campaignRepository;

    public Campaign execute(Campaign campaign) {
        campaign.setStartDate(java.time.LocalDate.now());
        return campaignRepository.save(campaign);
    }
}
