package com.donations.donations.controller;

import com.donations.donations.repository.CampaignRepository;
import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.repository.DonationRepository;
import com.donations.donations.repository.NotificationRepository;
import com.donations.donations.model.Campaign;
import com.donations.donations.model.Creator;
import com.donations.donations.model.Donation;
import com.donations.donations.model.Notification;
import com.donations.donations.model.enums.DonationStatus;
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

    private final DonationRepository donationRepository;
    private final CampaignRepository campaignRepository;
    private final NotificationRepository notificationRepository;
    private final CreatorRepository creatorRepository;

    @PostMapping("/payments/mpesa")
    public ResponseEntity<Void> mpesaCallback(@RequestBody Map<String, Object> payload) {
        if (!payload.containsKey("output_TransactionReference") || !payload.containsKey("output_ResponseCode")) {
            return ResponseEntity.badRequest().build();
        }
        
        String ref = (String) payload.get("output_TransactionReference");
        String code = (String) payload.get("output_ResponseCode");
        String desc = (String) payload.get("output_ResponseDesc");
        
        Donation donation = donationRepository.findAll().stream().filter(d -> ref.equals(d.getReference())).findFirst().orElse(null);
        if (donation == null) return ResponseEntity.ok().build(); // Ignore unknown
        
        if (donation.getStatus() == DonationStatus.SUCCESS) return ResponseEntity.ok().build(); // Already processed

        if ("INS-0".equals(code)) {
            donation.setStatus(DonationStatus.SUCCESS);
            
            // Add to campaign
            Campaign campaign = campaignRepository.findById(donation.getCampaignId()).orElse(null);
            if (campaign != null) {
                if (campaign.getRaisedAmount() == null) campaign.setRaisedAmount(BigDecimal.ZERO);
                campaign.setRaisedAmount(campaign.getRaisedAmount().add(donation.getAmount()));
                campaignRepository.save(campaign);
            }
            
            // Notify creator
            Creator creator = creatorRepository.findById(donation.getCreatorId()).orElse(null);
            if (creator != null) {
                notificationRepository.save(Notification.builder()
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
        
        donationRepository.save(donation);
        
        return ResponseEntity.ok().build();
    }
}