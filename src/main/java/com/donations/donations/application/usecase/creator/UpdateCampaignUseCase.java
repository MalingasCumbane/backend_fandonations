package com.donations.donations.application.usecase.creator;

import com.donations.donations.application.port.out.CampaignPort;
import com.donations.donations.domain.model.Campaign;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateCampaignUseCase {
    private final CampaignPort campaignPort;

    public Campaign execute(UUID campaignId, Campaign updated) {
        return campaignPort.findById(campaignId)
            .map(existing -> {
                existing.setTitle(updated.getTitle());
                existing.setDescription(updated.getDescription());
                existing.setGoalAmount(updated.getGoalAmount());
                existing.setEndDate(updated.getEndDate());
                existing.setStatus(updated.getStatus());
                return campaignPort.save(existing);
            })
            .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
    }
}
