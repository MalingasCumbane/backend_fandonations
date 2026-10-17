package com.donations.donations.service.creator;

import com.donations.donations.repository.CampaignRepository;
import com.donations.donations.model.Campaign;
import com.donations.donations.model.enums.CampaignStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateCampaignUseCase {
    private final CampaignRepository campaignRepository;
    private final com.donations.donations.service.realtime.RealtimeEventPublisher realtimeEventPublisher;

    public Campaign execute(Campaign campaign) {
        CampaignStatus status = campaign.getStatus() != null ? campaign.getStatus() : CampaignStatus.ACTIVE;
        campaign.setStatus(status);

        // Regra de Negócio: O criador só pode ter 1 campanha ativa de cada vez.
        if (status == CampaignStatus.ACTIVE) {
            List<Campaign> activeCampaigns = campaignRepository.findByCreatorIdAndStatus(campaign.getCreatorId(), CampaignStatus.ACTIVE);
            if (!activeCampaigns.isEmpty()) {
                String existingTitle = activeCampaigns.get(0).getTitle();
                throw new IllegalStateException(String.format(
                        "Já possui uma campanha ativa (\"%s\"). Para criar uma nova campanha, a anterior deve atingir a meta proposta ou ser encerrada.",
                        existingTitle != null && !existingTitle.isBlank() ? existingTitle : "Campanha ativa"
                ));
            }
        }

        if (campaign.getStartDate() == null) {
            campaign.setStartDate(LocalDate.now());
        }
        if (campaign.getRaisedAmount() == null) {
            campaign.setRaisedAmount(java.math.BigDecimal.ZERO);
        }

        Campaign saved = campaignRepository.save(campaign);
        try {
            realtimeEventPublisher.notifyCreatorCampaign(saved.getCreatorId(), saved);
        } catch (Exception ignored) {
        }
        return saved;
    }
}
