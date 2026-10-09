package com.donations.donations.service.admin;

import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.repository.NotificationRepository;
import com.donations.donations.repository.VerificationRequestRepository;
import com.donations.donations.model.VerificationRequest;
import com.donations.donations.model.enums.VerificationRequestStatus;
import java.time.LocalDateTime;
import com.donations.donations.model.Creator;
import com.donations.donations.model.CreatorStatus;
import com.donations.donations.model.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ReviewCreatorUseCase {
    private final CreatorRepository creatorRepository;
    private final NotificationRepository notificationRepository;
    private final VerificationRequestRepository verificationRequestRepository;

    public Creator execute(UUID id, CreatorStatus status, String adminNote) {
        Creator creator = creatorRepository.findAll().stream().filter(c -> c.getId().equals(id)).findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Creator not found"));
        
        creator.setStatus(status);
        if (adminNote != null) creator.setAdminNote(adminNote);
        if (status == CreatorStatus.REJECTED && adminNote != null) {
            creator.setRejectionReason(adminNote);
        }
        creator = creatorRepository.save(creator);

        // Update VerificationRequest if pending
        verificationRequestRepository.findFirstByCreatorIdOrderByCreatedAtDesc(creator.getId()).ifPresent(req -> {
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
                verificationRequestRepository.save(req);
            }
        });

        // Notify
        notificationRepository.save(Notification.builder()
                .id(UUID.randomUUID())
                .userId(creator.getUserId())
                .title("Atualização de Conta")
                .body("O estado da sua conta foi actualizado para " + status.name())
                .isRead(false)
                .build());

        return creator;
    }
}
