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
import com.donations.donations.repository.EmailRepository;
import com.donations.donations.service.realtime.RealtimeEventPublisher;
import com.donations.donations.service.creator.GetCreatorStatsUseCase;
import com.donations.donations.model.enums.DonationStatus;
import com.donations.donations.model.payment.PaymentRequest;
import com.donations.donations.model.payment.PaymentResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final EmailRepository emailRepository;
    private final RealtimeEventPublisher realtimeEventPublisher;
    private final GetCreatorStatsUseCase getCreatorStatsUseCase;
    private final com.donations.donations.service.wallet.WalletService walletService;

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
                try {
                    String supporter = donation.isAnonymous() ? "Apoiador Anónimo" : (donation.getSupporterName() != null ? donation.getSupporterName() : "Apoiador");
                    walletService.recordTransaction(
                            creator.getId(),
                            com.donations.donations.model.enums.TransactionType.CREDIT,
                            com.donations.donations.model.enums.TransactionCategory.DONATION_RECEIVED,
                            donation.getAmount(),
                            "DONATION",
                            donation.getReference(),
                            "Apoio recebido de " + supporter + (donation.getMethod() != null ? " (" + donation.getMethod().name() + ")" : ""),
                            donation.getCreatedAt() != null ? donation.getCreatedAt() : java.time.LocalDateTime.now()
                    );
                } catch (Exception e) {
                    log.error("Erro ao registar movimento de carteira para doação {}: {}", donation.getReference(), e.getMessage());
                }

                Notification notification = Notification.builder()
                        .userId(creator.getUserId())
                        .title("Nova Doação!")
                        .body("Recebeu uma doação de " + donation.getAmount() + " MZN de " + 
                              (donation.isAnonymous() ? "Anónimo" : (donation.getSupporterName() != null ? donation.getSupporterName() : "Anónimo")))
                        .isRead(false)
                        .build();
                notificationRepository.save(notification);
                
                if (donation.getSupporterEmail() != null && !donation.getSupporterEmail().isEmpty()) {
                    emailRepository.sendDonationReceiptEmail(
                        donation.getSupporterEmail(), 
                        creator.getFullName(), 
                        donation.getAmount().toString(), 
                        donation.getReference(),
                        donation.getMethod() != null ? donation.getMethod().name() : "M-Pesa / e-Mola"
                    );
                }

                // Disparo de Eventos WebSocket em Tempo Real
                try {
                    // 1. Notificar canal público do criador para atualizar metas e doações ao vivo
                    realtimeEventPublisher.notifyPublicCreator(creator.getUsername(), java.util.Map.of(
                        "type", "NEW_DONATION",
                        "amount", donation.getAmount(),
                        "campaignId", donation.getCampaignId() != null ? donation.getCampaignId().toString() : null
                    ));

                    // 2. Notificar canal privado do criador (Dashboard)
                    realtimeEventPublisher.notifyCreatorDonation(creator.getId(), donation);

                    // 3. Atualizar estatísticas do criador em tempo real
                    realtimeEventPublisher.notifyCreatorStats(creator.getId(), getCreatorStatsUseCase.execute(creator.getId()));

                    // 4. Notificar painel administrativo sobre a transação em tempo real
                    realtimeEventPublisher.notifyAdminTransactions(donation);
                } catch (Exception wsEx) {
                    log.error("Erro ao publicar evento WebSocket de doação: {}", wsEx.getMessage());
                }
            }
        } else {
            donation.setStatus(DonationStatus.FAILED);
            donation.setFailureReason(result.getFailureReason() != null ? result.getFailureReason() : "Método de pagamento indisponível");
            donationRepository.save(donation);
        }
    }
}
