package com.donations.donations.application.usecase.impl;

import com.donations.donations.application.port.out.CampaignPort;
import com.donations.donations.application.port.out.CreatorPort;
import com.donations.donations.application.port.out.DonationPort;
import com.donations.donations.application.port.out.NotificationPort;
import com.donations.donations.application.port.out.PaymentGatewayPort;
import com.donations.donations.domain.model.Campaign;
import com.donations.donations.domain.model.Creator;
import com.donations.donations.domain.model.Donation;
import com.donations.donations.domain.model.Notification;
import com.donations.donations.domain.model.enums.CampaignStatus;
import com.donations.donations.domain.model.enums.DonationStatus;
import com.donations.donations.domain.model.payment.PaymentRequest;
import com.donations.donations.domain.model.payment.PaymentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncPaymentProcessor {

    private final PaymentGatewayPort paymentGatewayPort;
    private final DonationPort donationPort;
    private final CampaignPort campaignPort;
    private final NotificationPort notificationPort;
    private final CreatorPort creatorPort;

    @Async
    @Transactional
    public void processPayment(Donation donation, String phone) {
        log.info("Processing payment for donation {}", donation.getReference());

        PaymentRequest paymentRequest = PaymentRequest.builder()
                .reference(donation.getReference())
                .amount(donation.getAmount())
                .phone(phone)
                .method(donation.getMethod())
                .build();

        PaymentResult result = paymentGatewayPort.charge(paymentRequest);

        if (result.isSuccess()) {
            donation.setStatus(DonationStatus.SUCCESS);
            donationPort.save(donation);

            if (donation.getCampaignId() != null) {
                Campaign campaign = campaignPort.findById(donation.getCampaignId()).orElse(null);
                if (campaign != null) {
                    campaign.setRaisedAmount(campaign.getRaisedAmount().add(donation.getAmount()));
                    if (campaign.getRaisedAmount().compareTo(campaign.getGoalAmount()) >= 0) {
                        campaign.setStatus(CampaignStatus.COMPLETED);
                    }
                    campaignPort.save(campaign);
                }
            }

            Creator creator = creatorPort.findById(donation.getCreatorId()).orElse(null);
            if (creator != null) {
                Notification notification = Notification.builder()
                        .userId(creator.getUserId())
                        .title("Nova Doação!")
                        .body("Recebeu uma doação de " + donation.getAmount() + " MZN de " + 
                              (donation.isAnonymous() ? "Anónimo" : (donation.getSupporterName() != null ? donation.getSupporterName() : "Anónimo")))
                        .isRead(false)
                        .build();
                notificationPort.save(notification);
            }
        } else {
            donation.setStatus(DonationStatus.FAILED);
            donation.setFailureReason(result.getFailureReason() != null ? result.getFailureReason() : "Método de pagamento indisponível");
            donationPort.save(donation);
        }
    }
}
