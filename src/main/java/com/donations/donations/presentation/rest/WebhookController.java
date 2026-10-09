package com.donations.donations.presentation.rest;

import com.donations.donations.application.port.out.CampaignPort;
import com.donations.donations.application.port.out.CreatorPort;
import com.donations.donations.application.port.out.DonationPort;
import com.donations.donations.application.port.out.NotificationPort;
import com.donations.donations.domain.model.Campaign;
import com.donations.donations.domain.model.Creator;
import com.donations.donations.domain.model.Donation;
import com.donations.donations.domain.model.Notification;
import com.donations.donations.domain.model.enums.DonationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final DonationPort donationPort;
    private final CampaignPort campaignPort;
    private final NotificationPort notificationPort;
    private final CreatorPort creatorPort;

    @PostMapping("/payments/mpesa")
    public ResponseEntity<Void> mpesaCallback(@RequestBody Map<String, Object> payload) {
        if (!payload.containsKey("output_TransactionReference") || !payload.containsKey("output_ResponseCode")) {
            return ResponseEntity.badRequest().build();
        }
        
        String ref = (String) payload.get("output_TransactionReference");
        String code = (String) payload.get("output_ResponseCode");
        String desc = (String) payload.get("output_ResponseDesc");
        
        Donation donation = donationPort.findAll().stream().filter(d -> ref.equals(d.getReference())).findFirst().orElse(null);
        if (donation == null) return ResponseEntity.ok().build(); // Ignore unknown
        
        if (donation.getStatus() == DonationStatus.SUCCESS) return ResponseEntity.ok().build(); // Already processed

        if ("INS-0".equals(code)) {
            donation.setStatus(DonationStatus.SUCCESS);
            donation.setProviderTransactionId((String) payload.get("output_ConversationID")); // Use whatever is available
            
            // Add to campaign
            Campaign campaign = campaignPort.findById(donation.getCampaignId()).orElse(null);
            if (campaign != null) {
                if (campaign.getRaisedAmount() == null) campaign.setRaisedAmount(BigDecimal.ZERO);
                campaign.setRaisedAmount(campaign.getRaisedAmount().add(donation.getAmount()));
                campaignPort.save(campaign);
            }
            
            // Notify creator
            Creator creator = creatorPort.findById(donation.getCreatorId()).orElse(null);
            if (creator != null) {
                notificationPort.save(Notification.builder()
                        .id(UUID.randomUUID())
                        .userId(creator.getUserId())
                        .title("Novo Apoio!")
                        .body("Recebeu uma doação de " + donation.getAmount() + " MT.")
                        .isRead(false)
                        .build());
            }
        } else {
            donation.setStatus(DonationStatus.FAILED);
            donation.setFailureReason(desc);
        }
        
        donationPort.save(donation);
        
        return ResponseEntity.ok().build();
    }
}