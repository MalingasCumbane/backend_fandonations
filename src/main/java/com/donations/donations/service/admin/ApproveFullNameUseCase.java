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
public class ApproveFullNameUseCase {
    private final CreatorRepository creatorRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public Creator execute(UUID id) {
        Creator creator = creatorRepository.findAll().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Criador não encontrado"));

        StringBuilder changes = new StringBuilder();

        if (creator.getRequestedFullName() != null && !creator.getRequestedFullName().isBlank()) {
            creator.setFullName(creator.getRequestedFullName());
            changes.append("Nome oficial alterado para '").append(creator.getRequestedFullName()).append("'. ");
            creator.setRequestedFullName(null);
        }

        if (creator.getRequestedUsername() != null && !creator.getRequestedUsername().isBlank()) {
            creator.setUsername(creator.getRequestedUsername());
            changes.append("Username alterado para '@").append(creator.getRequestedUsername()).append("'. ");
            creator.setRequestedUsername(null);
        }

        creator.setNameChangeReason(null);
        creator = creatorRepository.save(creator);

        // Notifica o criador
        if (creator.getUserId() != null) {
            notificationRepository.save(Notification.builder()
                    .userId(creator.getUserId())
                    .title("Alteração de dados aprovada")
                    .body("O seu pedido de alteração de identidade foi aprovado com sucesso! " + changes.toString())
                    .isRead(false)
                    .build());
        }

        return creator;
    }
}
