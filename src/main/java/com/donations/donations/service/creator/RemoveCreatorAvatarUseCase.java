package com.donations.donations.service.creator;

import com.donations.donations.model.Creator;
import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.service.storage.FileStoragePort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RemoveCreatorAvatarUseCase {

    private final CreatorRepository creatorRepository;
    private final FileStoragePort fileStoragePort;

    @Transactional
    public void execute(UUID userId) {
        Creator creator = creatorRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil de criador não encontrado"));

        String oldUrl = creator.getAvatarUrl();
        if (oldUrl != null && !oldUrl.isBlank()) {
            try {
                int idx = oldUrl.indexOf("avatars/");
                if (idx != -1) {
                    String key = oldUrl.substring(idx);
                    fileStoragePort.delete(key);
                }
            } catch (Exception e) {
                log.warn("Erro ao eliminar ficheiro antigo de avatar: {}", oldUrl, e);
            }
        }

        creator.setAvatarUrl(null);
        creatorRepository.save(creator);
        log.info("Foto de perfil removida para o criador {}", creator.getUsername());
    }
}
