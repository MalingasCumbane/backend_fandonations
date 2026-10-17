package com.donations.donations.service.creator;

import com.donations.donations.repository.CampaignRepository;
import com.donations.donations.model.Campaign;
import com.donations.donations.model.enums.CampaignStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
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

            // Regra de Negócio: Se estiver a ativar ou reativar, não pode existir outra campanha ativa
            if (updated.getStatus() == CampaignStatus.ACTIVE && existing.getStatus() != CampaignStatus.ACTIVE) {
                List<Campaign> activeCampaigns = campaignRepository.findByCreatorIdAndStatus(creatorId, CampaignStatus.ACTIVE);
                boolean hasOtherActive = activeCampaigns.stream().anyMatch(c -> !c.getId().equals(campaignId));
                if (hasOtherActive) {
                    Campaign other = activeCampaigns.stream().filter(c -> !c.getId().equals(campaignId)).findFirst().get();
                    String otherTitle = other.getTitle();
                    throw new IllegalStateException(String.format(
                            "Já possui outra campanha ativa (\"%s\"). Encerre ou conclua a campanha atual antes de ativar esta.",
                            otherTitle != null && !otherTitle.isBlank() ? otherTitle : "Campanha ativa"
                    ));
                }
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
        }).orElseThrow(() -> new IllegalArgumentException("Campanha não encontrada"));
    }
}
