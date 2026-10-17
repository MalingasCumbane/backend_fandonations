package com.donations.donations.service.admin;

import com.donations.donations.model.Creator;
import com.donations.donations.model.Notification;
import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RejectFullNameUseCase {
    private final CreatorRepository creatorRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public Creator execute(UUID id, String rejectionReason) {
        Creator creator = creatorRepository.findAll().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Criador não encontrado"));

        creator.setRequestedFullName(null);
        creator.setRequestedUsername(null);
        creator.setNameChangeReason(null);
        creator = creatorRepository.save(creator);

        // Notifica o criador
        if (creator.getUserId() != null) {
            String note = rejectionReason != null && !rejectionReason.isBlank() ? " Motivo: " + rejectionReason : "";
            notificationRepository.save(Notification.builder()
                    .userId(creator.getUserId())
                    .title("Pedido de alteração de identidade recusado")
                    .body("O seu pedido de alteração de nome/identidade não foi aceite pela moderação." + note)
                    .isRead(false)
                    .build());
        }

        return creator;
    }
}
