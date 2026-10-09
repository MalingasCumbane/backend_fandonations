package com.donations.donations.service.creator;

import com.donations.donations.repository.CampaignRepository;
import com.donations.donations.model.Campaign;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UpdateCampaignUseCase {
    private final CampaignRepository campaignRepository;

    public Campaign execute(UUID campaignId, Campaign updated) {
        return campaignRepository.findById(campaignId)
            .map(existing -> {
                if (updated.getTitle() != null) existing.setTitle(updated.getTitle());
                if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
                if (updated.getGoalAmount() != null) existing.setGoalAmount(updated.getGoalAmount());
                if (updated.getStartDate() != null) existing.setStartDate(updated.getStartDate());
                if (updated.getEndDate() != null) existing.setEndDate(updated.getEndDate());
                if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
                return campaignRepository.save(existing);
            })
            .orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
    }
}
