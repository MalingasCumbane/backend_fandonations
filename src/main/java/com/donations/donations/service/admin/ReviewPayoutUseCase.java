package com.donations.donations.service.admin;

import com.donations.donations.repository.PayoutRepository;
import com.donations.donations.repository.NotificationRepository;
import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.model.Creator;
import com.donations.donations.model.Payout;
import com.donations.donations.model.Notification;
import com.donations.donations.model.enums.PayoutStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ReviewPayoutUseCase {
    private final PayoutRepository payoutRepository;
    private final NotificationRepository notificationRepository;
    private final CreatorRepository creatorRepository;

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

        Creator creator = creatorRepository.findAll().stream().filter(c -> c.getId().equals(finalPayout.getCreatorId())).findFirst().orElseThrow();

        notificationRepository.save(Notification.builder()
                .id(UUID.randomUUID())
                .userId(creator.getUserId())
                .title("Actualização de Levantamento")
                .body("O estado do seu levantamento foi actualizado para " + status.name())
                .isRead(false)
                .build());

        return finalPayout;
    }
}
