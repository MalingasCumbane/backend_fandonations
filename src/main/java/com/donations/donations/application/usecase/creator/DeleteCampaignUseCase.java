package com.donations.donations.application.usecase.creator;

import com.donations.donations.application.port.out.CampaignPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeleteCampaignUseCase {
    private final CampaignPort campaignPort;

    public void execute(UUID campaignId) {
        campaignPort.deleteById(campaignId);
    }
}
