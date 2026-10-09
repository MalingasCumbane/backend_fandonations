package com.donations.donations.application.usecase.admin;

import com.donations.donations.application.port.out.CreatorPort;
import com.donations.donations.application.port.out.NotificationPort;
import com.donations.donations.application.port.out.VerificationRequestPort;
import com.donations.donations.domain.model.VerificationRequest;
import com.donations.donations.domain.model.enums.VerificationRequestStatus;
import java.time.LocalDateTime;
import com.donations.donations.domain.model.Creator;
import com.donations.donations.domain.model.CreatorStatus;
import com.donations.donations.domain.model.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ReviewCreatorUseCase {
    private final CreatorPort creatorPort;
    private final NotificationPort notificationPort;
    private final VerificationRequestPort verificationRequestPort;

    public Creator execute(UUID id, CreatorStatus status, String adminNote) {
        Creator creator = creatorPort.findAll().stream().filter(c -> c.getId().equals(id)).findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Creator not found"));
        
        creator.setStatus(status);
        if (adminNote != null) creator.setAdminNote(adminNote);
        if (status == CreatorStatus.REJECTED && adminNote != null) {
            creator.setRejectionReason(adminNote);
        }
        creator = creatorPort.save(creator);

        // Update VerificationRequest if pending
        verificationRequestPort.findByCreatorId(creator.getId()).ifPresent(req -> {
            if (req.getStatus() == VerificationRequestStatus.PENDING) {
                if (status == CreatorStatus.VERIFIED) {
                    req.setStatus(VerificationRequestStatus.APPROVED);
                } else if (status == CreatorStatus.REJECTED) {
                    req.setStatus(VerificationRequestStatus.REJECTED);
                }
                req.setReviewedAt(LocalDateTime.now());
                if (adminNote != null) {
                    req.setAdminNote(adminNote);
                }
                verificationRequestPort.save(req);
            }
        });

        // Notify
        notificationPort.save(Notification.builder()
                .id(UUID.randomUUID())
                .userId(creator.getUserId())
                .title("Atualização de Conta")
                .body("O estado da sua conta foi actualizado para " + status.name())
                .isRead(false)
                .build());

        return creator;
    }
}
