package com.donations.donations.service.creator;

import com.donations.donations.model.Creator;
import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.service.storage.FileStoragePort;
import com.donations.donations.service.storage.StorageFileHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UploadCreatorAvatarUseCase {

    private final CreatorRepository creatorRepository;
    private final FileStoragePort fileStoragePort;

    private static final long MAX_AVATAR_BYTES = 5 * 1024 * 1024; // 5 MB

    @Transactional
    public String execute(UUID userId, byte[] imageBytes, String contentType) {
        if (imageBytes == null || imageBytes.length == 0) {
            throw new IllegalArgumentException("A imagem não pode estar vazia");
        }

        if (imageBytes.length > MAX_AVATAR_BYTES) {
            throw new IllegalArgumentException("A imagem de perfil deve ter no máximo 5 MB");
        }

        String mime = contentType != null ? contentType.toLowerCase() : "image/jpeg";
        if (!mime.startsWith("image/")) {
            throw new IllegalArgumentException("Apenas imagens (JPEG, PNG, WebP) são permitidas");
        }

        Creator creator = creatorRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Perfil de criador não encontrado"));

        String ext = StorageFileHelper.getExtensionFromMime(mime);
        String key = "avatars/" + creator.getId() + "/avatar_" + System.currentTimeMillis() + "." + ext;

        // Upload to storage (Cloudflare R2 or Local fallback)
        fileStoragePort.upload(key, imageBytes, mime);

        // Resolve public URL (CDN, public bucket URL, or app media endpoint)
        String avatarUrl = fileStoragePort.resolveUrl(key);

        creator.setAvatarUrl(avatarUrl);
        creatorRepository.save(creator);

        log.info("Foto de perfil atualizada para o criador {}: {}", creator.getUsername(), avatarUrl);
        return avatarUrl;
    }
}
