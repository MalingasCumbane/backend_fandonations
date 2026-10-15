package com.donations.donations.controller;

import com.donations.donations.repository.CampaignRepository;
import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.repository.DonationRepository;
import com.donations.donations.repository.NotificationRepository;
import com.donations.donations.repository.EmailRepository;
import com.donations.donations.model.Campaign;
import com.donations.donations.model.Creator;
import com.donations.donations.model.Donation;
import com.donations.donations.model.Notification;
import com.donations.donations.model.enums.DonationStatus;
import com.donations.donations.service.realtime.RealtimeEventPublisher;
import com.donations.donations.service.creator.GetCreatorStatsUseCase;
import com.donations.donations.service.wallet.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class WebhookController {

    private final DonationRepository donationRepository;
    private final CampaignRepository campaignRepository;
    private final NotificationRepository notificationRepository;
    private final CreatorRepository creatorRepository;
    private final WalletService walletService;
    private final RealtimeEventPublisher realtimeEventPublisher;
    private final GetCreatorStatsUseCase getCreatorStatsUseCase;
    private final EmailRepository emailRepository;

    @PostMapping("/payments/mpesa")
    public ResponseEntity<Void> mpesaCallback(@RequestBody Map<String, Object> payload) {
        log.info("Recebido webhook callback do Vodacom M-Pesa: {}", payload);

        String ref = null;
        if (payload.containsKey("output_ThirdPartyReference") && payload.get("output_ThirdPartyReference") != null) {
            ref = String.valueOf(payload.get("output_ThirdPartyReference"));
        } else if (payload.containsKey("output_TransactionReference") && payload.get("output_TransactionReference") != null) {
            ref = String.valueOf(payload.get("output_TransactionReference"));
        }

        String code = payload.get("output_ResponseCode") != null ? String.valueOf(payload.get("output_ResponseCode")) : null;
        String desc = payload.get("output_ResponseDesc") != null ? String.valueOf(payload.get("output_ResponseDesc")) : null;

        if (ref == null || code == null) {
            log.warn("Webhook M-Pesa recebido sem campos obrigatórios: {}", payload);
            return ResponseEntity.badRequest().build();
        }

        String finalRef = ref;
        Donation donation = donationRepository.findByReference(finalRef).orElse(null);
        if (donation == null) {
            log.warn("Doação não encontrada para a referência recebida no webhook: {}", finalRef);
            return ResponseEntity.ok().build();
        }

        if (donation.getStatus() == DonationStatus.SUCCESS) {
            log.info("Doação {} já se encontra como SUCCESS. Ignorando processamento duplicado.", finalRef);
            return ResponseEntity.ok().build();
        }

        if ("INS-0".equalsIgnoreCase(code)) {
            donation.setStatus(DonationStatus.SUCCESS);
            donationRepository.save(donation);

            // Atualizar campanha se associada
            if (donation.getCampaignId() != null) {
                Campaign campaign = campaignRepository.findById(donation.getCampaignId()).orElse(null);
                if (campaign != null) {
                    if (campaign.getRaisedAmount() == null) campaign.setRaisedAmount(BigDecimal.ZERO);
                    campaign.setRaisedAmount(campaign.getRaisedAmount().add(donation.getAmount()));
                    if (campaign.getRaisedAmount().compareTo(campaign.getGoalAmount()) >= 0) {
                        campaign.setStatus(com.donations.donations.model.enums.CampaignStatus.COMPLETED);
                    }
                    campaignRepository.save(campaign);
                }
            }

            // Notificar criador e atualizar carteira
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
                            donation.getCreatedAt() != null ? donation.getCreatedAt() : LocalDateTime.now()
                    );
                } catch (Exception e) {
                    log.error("Erro ao registar movimento na carteira no webhook: {}", e.getMessage());
                }

                notificationRepository.save(Notification.builder()
                        .userId(creator.getUserId())
                        .title("Novo Apoio!")
                        .body("Recebeu uma doação de " + donation.getAmount() + " MT.")
                        .isRead(false)
                        .build());

                if (donation.getSupporterEmail() != null && !donation.getSupporterEmail().isEmpty()) {
                    emailRepository.sendDonationReceiptEmail(
                            donation.getSupporterEmail(),
                            creator.getFullName(),
                            donation.getAmount().toString(),
                            donation.getReference(),
                            donation.getMethod() != null ? donation.getMethod().name() : "M-Pesa"
                    );
                }

                // Publicar eventos WebSocket
                try {
                    Map<String, Object> publicPayload = new HashMap<>();
                    publicPayload.put("id", donation.getId() != null ? donation.getId().toString() : "");
                    publicPayload.put("reference", donation.getReference());
                    publicPayload.put("amount", donation.getAmount());
                    publicPayload.put("method", donation.getMethod() != null ? donation.getMethod().name() : "MPESA");
                    publicPayload.put("status", "SUCCESS");
                    publicPayload.put("supporterName", donation.isAnonymous() ? "Anónimo" : (donation.getSupporterName() != null ? donation.getSupporterName() : "Anónimo"));
                    publicPayload.put("message", donation.getMessage() != null ? donation.getMessage() : "");
                    publicPayload.put("campaignId", donation.getCampaignId() != null ? donation.getCampaignId().toString() : "");
                    publicPayload.put("createdAt", donation.getCreatedAt() != null ? donation.getCreatedAt().toString() : LocalDateTime.now().toString());
                    realtimeEventPublisher.notifyPublicCreator(creator.getUsername(), publicPayload);
                } catch (Exception wsEx) {
                    log.error("Erro WebSocket público no webhook: {}", wsEx.getMessage());
                }

                try {
                    Map<String, Object> creatorPayload = new HashMap<>();
                    creatorPayload.put("id", donation.getId() != null ? donation.getId().toString() : "");
                    creatorPayload.put("reference", donation.getReference());
                    creatorPayload.put("amount", donation.getAmount());
                    creatorPayload.put("method", donation.getMethod() != null ? donation.getMethod().name() : "MPESA");
                    creatorPayload.put("status", "SUCCESS");
                    creatorPayload.put("supporterName", donation.isAnonymous() ? "Anónimo" : (donation.getSupporterName() != null ? donation.getSupporterName() : "Anónimo"));
                    creatorPayload.put("createdAt", donation.getCreatedAt() != null ? donation.getCreatedAt().toString() : LocalDateTime.now().toString());
                    realtimeEventPublisher.notifyCreatorDonation(creator.getId(), creatorPayload);
                } catch (Exception wsEx) {
                    log.error("Erro WebSocket criador no webhook: {}", wsEx.getMessage());
                }

                try {
                    realtimeEventPublisher.notifyCreatorStats(creator.getId(), getCreatorStatsUseCase.execute(creator.getId()));
                } catch (Exception wsEx) {
                    log.error("Erro WebSocket stats no webhook: {}", wsEx.getMessage());
                }

                try {
                    Map<String, Object> adminPayload = new HashMap<>();
                    adminPayload.put("id", donation.getId() != null ? donation.getId().toString() : "");
                    adminPayload.put("reference", donation.getReference());
                    adminPayload.put("amount", donation.getAmount());
                    adminPayload.put("creatorId", creator.getId().toString());
                    adminPayload.put("creatorUsername", creator.getUsername());
                    adminPayload.put("creatorFullName", creator.getFullName());
                    adminPayload.put("method", donation.getMethod() != null ? donation.getMethod().name() : "MPESA");
                    adminPayload.put("type", "CREDIT");
                    adminPayload.put("category", "DONATION_RECEIVED");
                    adminPayload.put("createdAt", donation.getCreatedAt() != null ? donation.getCreatedAt().toString() : LocalDateTime.now().toString());
                    realtimeEventPublisher.notifyAdminTransactions(adminPayload);
                } catch (Exception wsEx) {
                    log.error("Erro WebSocket admin no webhook: {}", wsEx.getMessage());
                }
            }
        } else {
            donation.setStatus(DonationStatus.FAILED);
            donation.setFailureReason(desc != null ? desc : "Pagamento recusado pela rede M-Pesa");
            donationRepository.save(donation);
        }

        return ResponseEntity.ok().build();
    }
}