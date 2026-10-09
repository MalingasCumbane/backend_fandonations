package com.donations.donations.service.creator;

import com.donations.donations.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteCampaignUseCase {
    private final CampaignRepository campaignRepository;

    public void execute(UUID campaignId, UUID creatorId) {
        campaignRepository.findById(campaignId).ifPresent(campaign -> {
            if (campaign.getCreatorId().equals(creatorId)) {
                campaignRepository.deleteById(campaignId);
            } else {
                throw new org.springframework.security.access.AccessDeniedException("Não tem permissão para apagar esta campanha.");
            }
        });
    }
}
