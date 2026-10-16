package com.donations.donations.service.creator;

import com.donations.donations.model.VerificationRequest;
import com.donations.donations.repository.VerificationRequestRepository;
import com.donations.donations.service.storage.FileStoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCreatorVerificationUseCase {
    private final VerificationRequestRepository verificationRepository;
    private final FileStoragePort fileStoragePort;

    public Optional<VerificationRequest> execute(UUID creatorId) {
        return verificationRepository.findFirstByCreatorIdOrderByCreatedAtDesc(creatorId).map(this::enrichSignedUrls);
    }

    private VerificationRequest enrichSignedUrls(VerificationRequest req) {
        return VerificationRequest.builder().id(req.getId()).creatorId(req.getCreatorId()).documentType(req.getDocumentType()).documentNumber(req.getDocumentNumber()).fullName(req.getFullName()).documentFile(resolveFileUrl(req.getDocumentFile())).selfieImage(resolveFileUrl(req.getSelfieImage())).note(req.getNote()).documentFileType(req.getDocumentFileType()).submittedAt(req.getSubmittedAt()).reviewedAt(req.getReviewedAt()).adminNote(req.getAdminNote()).status(req.getStatus()).createdAt(req.getCreatedAt()).updatedAt(req.getUpdatedAt()).build();
    }

    private String resolveFileUrl(String fileRef) {
        if (fileRef == null || fileRef.isBlank()) return fileRef;
        if (fileRef.startsWith("data:") || fileRef.startsWith("http://") || fileRef.startsWith("https://")) {
            return fileRef;
        }
        return fileStoragePort.generatePresignedUrl(fileRef, Duration.ofHours(2));
    }
}