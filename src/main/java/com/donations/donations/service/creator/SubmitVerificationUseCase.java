package com.donations.donations.service.creator;

import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.repository.VerificationRequestRepository;
import com.donations.donations.model.Creator;
import com.donations.donations.model.VerificationRequest;
import com.donations.donations.model.enums.VerificationRequestStatus;
import com.donations.donations.service.storage.FileStoragePort;
import com.donations.donations.service.storage.StorageFileHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubmitVerificationUseCase {
    private final VerificationRequestRepository verificationRepository;
    private final CreatorRepository creatorRepository;
    private final FileStoragePort fileStoragePort;

    public VerificationRequest execute(UUID creatorId, VerificationRequest request) {
        Creator creator = creatorRepository.findById(creatorId)
                .orElseThrow(() -> new IllegalArgumentException("Creator not found"));

        // creator status remains as is until approved
        creator.setRequestedFullName(request.getFullName());
        creator.setSubmittedAt(LocalDateTime.now());
        creatorRepository.save(creator);

        // Upload document file to storage if base64 / data URL
        if (StorageFileHelper.isDataUrlOrBase64(request.getDocumentFile())) {
            try {
                byte[] bytes = StorageFileHelper.extractBytes(request.getDocumentFile());
                String mime = StorageFileHelper.extractMimeType(request.getDocumentFile(), request.getDocumentFileType());
                String ext = StorageFileHelper.getExtensionFromMime(mime);
                String docKey = "verifications/" + creatorId + "/doc_" + UUID.randomUUID() + "." + ext;
                fileStoragePort.upload(docKey, bytes, mime);
                String docUrl = fileStoragePort.resolveUrl(docKey);
                request.setDocumentFile(docUrl);
                request.setDocumentFileType(mime);
            } catch (Exception e) {
                log.error("Erro ao enviar documento para armazenamento", e);
            }
        }

        // Upload selfie image to storage if base64 / data URL
        if (StorageFileHelper.isDataUrlOrBase64(request.getSelfieImage())) {
            try {
                byte[] bytes = StorageFileHelper.extractBytes(request.getSelfieImage());
                String mime = StorageFileHelper.extractMimeType(request.getSelfieImage(), "image/jpeg");
                String ext = StorageFileHelper.getExtensionFromMime(mime);
                String selfieKey = "verifications/" + creatorId + "/selfie_" + UUID.randomUUID() + "." + ext;
                fileStoragePort.upload(selfieKey, bytes, mime);
                String selfieUrl = fileStoragePort.resolveUrl(selfieKey);
                request.setSelfieImage(selfieUrl);
            } catch (Exception e) {
                log.error("Erro ao enviar selfie para armazenamento", e);
            }
        }

        request.setCreatorId(creatorId);
        request.setStatus(VerificationRequestStatus.PENDING_REQUEST);
        request.setSubmittedAt(LocalDateTime.now());
        return verificationRepository.save(request);
    }
}