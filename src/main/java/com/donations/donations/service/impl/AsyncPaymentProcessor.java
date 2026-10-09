package com.donations.donations.service.impl;

import com.donations.donations.repository.CampaignRepository;
import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.repository.DonationRepository;
import com.donations.donations.repository.PaymentGatewayRepository;
import com.donations.donations.repository.NotificationRepository;
import com.donations.donations.model.Campaign;
import com.donations.donations.model.Creator;
import com.donations.donations.model.Donation;
import com.donations.donations.model.Notification;
import com.donations.donations.model.enums.CampaignStatus;
import com.donations.donations.model.enums.DonationStatus;
import com.donations.donations.model.payment.PaymentRequest;
import com.donations.donations.model.payment.PaymentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AsyncPaymentProcessor {

    private final PaymentGatewayRepository paymentGatewayRepository;
    private final DonationRepository donationRepository;
    private final CampaignRepository campaignRepository;
    private final NotificationRepository notificationRepository;
    private final CreatorRepository creatorRepository;

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

        PaymentResult result = paymentGatewayRepository.charge(paymentRequest);

        if (result.isSuccess()) {
            donation.setStatus(DonationStatus.SUCCESS);
            donationRepository.save(donation);

            if (donation.getCampaignId() != null) {
                Campaign campaign = campaignRepository.findById(donation.getCampaignId()).orElse(null);
                if (campaign != null) {
                    campaign.setRaisedAmount(campaign.getRaisedAmount().add(donation.getAmount()));
                    if (campaign.getRaisedAmount().compareTo(campaign.getGoalAmount()) >= 0) {
                        campaign.setStatus(CampaignStatus.COMPLETED);
                    }
                    campaignRepository.save(campaign);
                }
            }

            Creator creator = creatorRepository.findById(donation.getCreatorId()).orElse(null);
            if (creator != null) {
                Notification notification = Notification.builder()
                        .userId(creator.getUserId())
                        .title("Nova Doação!")
                        .body("Recebeu uma doação de " + donation.getAmount() + " MZN de " + 
                              (donation.isAnonymous() ? "Anónimo" : (donation.getSupporterName() != null ? donation.getSupporterName() : "Anónimo")))
                        .isRead(false)
                        .build();
                notificationRepository.save(notification);
            }
        } else {
            donation.setStatus(DonationStatus.FAILED);
            donation.setFailureReason(result.getFailureReason() != null ? result.getFailureReason() : "Método de pagamento indisponível");
            donationRepository.save(donation);
        }
    }
}
