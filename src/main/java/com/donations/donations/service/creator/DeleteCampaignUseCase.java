package com.donations.donations.service.creator;

import com.donations.donations.repository.CampaignRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteCampaignUseCase {
    private final CampaignRepository campaignRepository;

    public void execute(UUID campaignId) {
        campaignRepository.deleteById(campaignId);
    }
}
