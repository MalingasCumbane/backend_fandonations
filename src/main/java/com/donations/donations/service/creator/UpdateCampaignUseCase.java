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
    private final com.donations.donations.service.realtime.RealtimeEventPublisher realtimeEventPublisher;

    public Campaign execute(UUID campaignId, Campaign updated, UUID creatorId) {
        return campaignRepository.findById(campaignId).map(existing -> {
            if (!existing.getCreatorId().equals(creatorId)) {
                throw new org.springframework.security.access.AccessDeniedException("Não tem permissão para editar esta campanha.");
            }
            if (updated.getTitle() != null) existing.setTitle(updated.getTitle());
            if (updated.getDescription() != null) existing.setDescription(updated.getDescription());
            if (updated.getGoalAmount() != null) existing.setGoalAmount(updated.getGoalAmount());
            if (updated.getStartDate() != null) existing.setStartDate(updated.getStartDate());
            if (updated.getEndDate() != null) existing.setEndDate(updated.getEndDate());
            if (updated.getStatus() != null) existing.setStatus(updated.getStatus());
            Campaign saved = campaignRepository.save(existing);
            try {
                realtimeEventPublisher.notifyCreatorCampaign(saved.getCreatorId(), saved);
            } catch (Exception ignored) {
            }
            return saved;
        }).orElseThrow(() -> new IllegalArgumentException("Campaign not found"));
    }
}
