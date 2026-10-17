package com.donations.donations.service.creator;

import com.donations.donations.model.Creator;
import com.donations.donations.model.CreatorStatus;
import com.donations.donations.repository.CreatorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RequestFullNameChangeUseCase {
    private final CreatorRepository creatorRepository;

    @Transactional
    public Creator execute(UUID userId, String requestedFullName) {
        return execute(userId, requestedFullName, null, null);
    }

    @Transactional
    public Creator execute(UUID userId, String requestedFullName, String requestedUsername, String reason) {
        Creator creator = creatorRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Criador não encontrado."));

        if (creator.getStatus() != CreatorStatus.VERIFIED) {
            throw new IllegalStateException("Apenas criadores com conta verificada precisam solicitar alteração de dados de identidade.");
        }

        boolean hasChange = false;

        if (requestedFullName != null && !requestedFullName.isBlank()) {
            creator.setRequestedFullName(requestedFullName.trim());
            hasChange = true;
        }

        if (requestedUsername != null && !requestedUsername.isBlank()) {
            String cleanUsername = requestedUsername.trim().toLowerCase().replaceAll("[^a-z0-9._]", "");
            if (cleanUsername.length() < 3) {
                throw new IllegalArgumentException("O nome de utilizador deve ter pelo menos 3 caracteres.");
            }

            Optional<Creator> existing = creatorRepository.findByUsername(cleanUsername);
            if (existing.isPresent() && !existing.get().getId().equals(creator.getId())) {
                throw new IllegalArgumentException("O nome de utilizador @" + cleanUsername + " já está em uso.");
            }

            creator.setRequestedUsername(cleanUsername);
            hasChange = true;
        }

        if (!hasChange) {
            throw new IllegalArgumentException("Deve fornecer um novo nome oficial ou @username.");
        }

        if (reason != null && !reason.isBlank()) {
            creator.setNameChangeReason(reason.trim());
        }

        creator.setSubmittedAt(LocalDateTime.now());
        return creatorRepository.save(creator);
    }
}
