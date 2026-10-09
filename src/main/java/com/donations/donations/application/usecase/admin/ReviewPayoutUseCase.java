package com.donations.donations.application.usecase.admin;

import com.donations.donations.application.port.out.PayoutPort;
import com.donations.donations.application.port.out.NotificationPort;
import com.donations.donations.application.port.out.CreatorPort;
import com.donations.donations.domain.model.Creator;
import com.donations.donations.domain.model.Payout;
import com.donations.donations.domain.model.Notification;
import com.donations.donations.domain.model.enums.PayoutStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ReviewPayoutUseCase {
    private final PayoutPort payoutPort;
    private final NotificationPort notificationPort;
    private final CreatorPort creatorPort;

    public Payout execute(UUID id, PayoutStatus status, String adminNote, String transactionRef) {
        Payout payout = payoutPort.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Payout not found"));

        payout.setStatus(status);
        if (status == PayoutStatus.PAID) {
            payout.setProcessedAt(LocalDateTime.now());
        }
        if (adminNote != null) payout.setAdminNote(adminNote);
        if (transactionRef != null) payout.setTransactionReference(transactionRef);
        Payout finalPayout = payoutPort.save(payout);

        Creator creator = creatorPort.findAll().stream().filter(c -> c.getId().equals(finalPayout.getCreatorId())).findFirst().orElseThrow();

        notificationPort.save(Notification.builder()
                .id(UUID.randomUUID())
                .userId(creator.getUserId())
                .title("Actualização de Levantamento")
                .body("O estado do seu levantamento foi actualizado para " + status.name())
                .isRead(false)
                .build());

        return finalPayout;
    }
}
