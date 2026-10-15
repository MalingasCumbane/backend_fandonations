package com.donations.donations.service.admin;

import com.donations.donations.repository.PayoutRepository;
import com.donations.donations.repository.NotificationRepository;
import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.model.Creator;
import com.donations.donations.model.Payout;
import com.donations.donations.model.Notification;
import com.donations.donations.model.enums.PayoutStatus;
import com.donations.donations.repository.EmailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ReviewPayoutUseCase {
    private final PayoutRepository payoutRepository;
    private final NotificationRepository notificationRepository;
    private final CreatorRepository creatorRepository;
    private final com.donations.donations.repository.UserRepository userRepository;
    private final com.donations.donations.service.realtime.RealtimeEventPublisher realtimeEventPublisher;
    private final EmailRepository emailRepository;
    private final com.donations.donations.service.wallet.WalletService walletService;
    private final com.donations.donations.repository.PlatformSettingsRepository settingsRepository;

    public Payout execute(UUID id, PayoutStatus status, String adminNote, String transactionRef) {
        Payout payout = payoutRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payout not found"));

        payout.setStatus(status);
        if (status == PayoutStatus.PAID) {
            payout.setProcessedAt(LocalDateTime.now());
        } else if (status == PayoutStatus.CANCELLED) {
            payout.setProcessedAt(LocalDateTime.now());
        }
        if (adminNote != null) payout.setAdminNote(adminNote);
        if (transactionRef != null) payout.setTransactionReference(transactionRef);
        Payout finalPayout = payoutRepository.save(payout);

        // Se o levantamento for rejeitado/cancelado, devolver o valor e a taxa retida ao saldo do criador
        if (status == PayoutStatus.CANCELLED) {
            try {
                BigDecimal feePercentage = settingsRepository.getSettings()
                        .map(com.donations.donations.model.PlatformSettings::getPlatformFeePercentage)
                        .orElse(new BigDecimal("10.00"));
                BigDecimal feeAmount = finalPayout.getAmount().multiply(feePercentage).divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);

                walletService.recordTransaction(
                        finalPayout.getCreatorId(),
                        com.donations.donations.model.enums.TransactionType.CREDIT,
                        com.donations.donations.model.enums.TransactionCategory.PAYOUT_REFUND,
                        finalPayout.getAmount(),
                        "PAYOUT",
                        finalPayout.getId().toString(),
                        "Estorno de pedido de levantamento recusado",
                        LocalDateTime.now()
                );

                walletService.recordTransaction(
                        finalPayout.getCreatorId(),
                        com.donations.donations.model.enums.TransactionType.CREDIT,
                        com.donations.donations.model.enums.TransactionCategory.PLATFORM_FEE_REFUND,
                        feeAmount,
                        "PAYOUT",
                        finalPayout.getId().toString(),
                        "Estorno de taxa da plataforma por levantamento recusado",
                        LocalDateTime.now().plusSeconds(1)
                );
            } catch (Exception e) {
                System.err.println("Erro ao registar estorno de carteira: " + e.getMessage());
            }
        }

        Creator creator = creatorRepository.findById(finalPayout.getCreatorId()).orElse(null);

        if (creator != null && creator.getUserId() != null) {
            notificationRepository.save(Notification.builder()
                    .userId(creator.getUserId())
                    .title("Actualização de Levantamento")
                    .body("O estado do seu levantamento foi actualizado para " + status.name())
                    .isRead(false)
                    .build());
        }

        try {
            java.util.Map<String, Object> payoutEvent = new java.util.HashMap<>();
            payoutEvent.put("type", "PAYOUT_STATUS_CHANGED");
            payoutEvent.put("status", status.name());
            payoutEvent.put("id", finalPayout.getId().toString());
            realtimeEventPublisher.notifyCreatorPayout(finalPayout.getCreatorId(), payoutEvent);
            realtimeEventPublisher.notifyCreatorDonation(finalPayout.getCreatorId(), payoutEvent);
            realtimeEventPublisher.notifyAdminPayouts(finalPayout);
        } catch (Exception ignored) {}

        // Envio de email com resultado da operação para o Criador
        try {
            if (creator != null) {
                java.util.Set<String> recipientCreatorEmails = new java.util.LinkedHashSet<>();
                if (creator.getEmail() != null && !creator.getEmail().isBlank()) {
                    recipientCreatorEmails.add(creator.getEmail().trim());
                }
                if (creator.getUserId() != null) {
                    userRepository.findById(creator.getUserId()).ifPresent(u -> {
                        if (u.getEmail() != null && !u.getEmail().isBlank()) {
                            recipientCreatorEmails.add(u.getEmail().trim());
                        }
                    });
                }

                if (!recipientCreatorEmails.isEmpty()) {
                    String dateFormatted = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                    String amountStr = String.format("%,.2f", finalPayout.getAmount());
                    String methodStr = finalPayout.getMethod() != null ? finalPayout.getMethod().name() : "M-Pesa/e-Mola";
                    String phoneStr = finalPayout.getPhone() != null ? finalPayout.getPhone() : (creator.getPhone() != null ? creator.getPhone() : "N/D");

                    for (String creatorEmail : recipientCreatorEmails) {
                        if (status == PayoutStatus.PAID) {
                            System.out.println("[Email] A enviar email de levantamento pago (PAID) para criador: " + creatorEmail);
                            emailRepository.sendPayoutApprovedEmail(
                                    creatorEmail,
                                    creator.getFullName() != null ? creator.getFullName() : creator.getUsername(),
                                    amountStr,
                                    methodStr,
                                    phoneStr,
                                    finalPayout.getTransactionReference(),
                                    finalPayout.getAdminNote(),
                                    dateFormatted
                            );
                        } else if (status == PayoutStatus.CANCELLED || status == PayoutStatus.FAILED) {
                            System.out.println("[Email] A enviar email de levantamento cancelado/rejeitado para criador: " + creatorEmail);
                            emailRepository.sendPayoutRejectedEmail(
                                    creatorEmail,
                                    creator.getFullName() != null ? creator.getFullName() : creator.getUsername(),
                                    amountStr,
                                    methodStr,
                                    finalPayout.getAdminNote(),
                                    dateFormatted
                            );
                        }
                    }
                } else {
                    System.out.println("[Email] Aviso: email do criador não encontrado para notificação de revisão de levantamento (creatorId=" + finalPayout.getCreatorId() + ").");
                }
            }
        } catch (Exception e) {
            System.err.println("Erro ao enviar email de revisão de levantamento: " + e.getMessage());
            e.printStackTrace();
        }

        return finalPayout;
    }
}
