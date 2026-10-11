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

    public Payout execute(UUID id, PayoutStatus status, String adminNote, String transactionRef) {
        Payout payout = payoutRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payout not found"));

        payout.setStatus(status);
        if (status == PayoutStatus.PAID) {
            payout.setProcessedAt(LocalDateTime.now());
        }
        if (adminNote != null) payout.setAdminNote(adminNote);
        if (transactionRef != null) payout.setTransactionReference(transactionRef);
        Payout finalPayout = payoutRepository.save(payout);

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
            realtimeEventPublisher.notifyCreatorDonation(finalPayout.getCreatorId(), java.util.Map.of("type", "PAYOUT_STATUS_CHANGED", "status", status.name(), "id", finalPayout.getId().toString()));
        } catch (Exception ignored) {}

        // Envio de email com resultado da operação para o Criador
        try {
            if (creator != null) {
                String creatorEmail = (creator.getEmail() != null && !creator.getEmail().isBlank())
                        ? creator.getEmail()
                        : (creator.getUserId() != null 
                                ? userRepository.findById(creator.getUserId()).map(com.donations.donations.model.User::getEmail).orElse(null) 
                                : null);

                if (creatorEmail != null && !creatorEmail.isBlank()) {
                    String dateFormatted = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
                    String amountStr = String.format("%,.2f", finalPayout.getAmount());
                    String methodStr = finalPayout.getMethod() != null ? finalPayout.getMethod().name() : "M-Pesa/e-Mola";
                    String phoneStr = finalPayout.getPhone() != null ? finalPayout.getPhone() : (creator.getPhone() != null ? creator.getPhone() : "N/D");

                    if (status == PayoutStatus.PAID) {
                        System.out.println("[Email] Enfileirando email de levantamento pago (PAID) para criador: " + creatorEmail);
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
                    } else if (status == PayoutStatus.CANCELLED) {
                        System.out.println("[Email] Enfileirando email de levantamento cancelado (CANCELLED) para criador: " + creatorEmail);
                        emailRepository.sendPayoutRejectedEmail(
                                creatorEmail,
                                creator.getFullName() != null ? creator.getFullName() : creator.getUsername(),
                                amountStr,
                                methodStr,
                                finalPayout.getAdminNote(),
                                dateFormatted
                        );
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
