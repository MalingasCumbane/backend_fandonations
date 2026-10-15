package com.donations.donations.service.admin;

import com.donations.donations.model.VerificationRequest;
import com.donations.donations.repository.VerificationRequestRepository;
import com.donations.donations.service.storage.FileStoragePort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetVerificationsUseCase {
    private final VerificationRequestRepository verificationRequestRepository;
    private final FileStoragePort fileStoragePort;

    public List<VerificationRequest> execute() {
        return verificationRequestRepository.findAll().stream()
                .map(this::enrichSignedUrls)
                .toList();
    }

    public com.donations.donations.dto.PageResponse<VerificationRequest> executePaged(int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(Math.max(0, page), Math.max(1, size));
        return com.donations.donations.dto.PageResponse.of(verificationRequestRepository.findAllByOrderByCreatedAtDesc(pageable), this::enrichSignedUrls);
    }

    private VerificationRequest enrichSignedUrls(VerificationRequest req) {
        return VerificationRequest.builder()
                .id(req.getId())
                .creatorId(req.getCreatorId())
                .documentType(req.getDocumentType())
                .documentNumber(req.getDocumentNumber())
                .fullName(req.getFullName())
                .documentFile(resolveFileUrl(req.getDocumentFile()))
                .selfieImage(resolveFileUrl(req.getSelfieImage()))
                .note(req.getNote())
                .documentFileType(req.getDocumentFileType())
                .submittedAt(req.getSubmittedAt())
                .reviewedAt(req.getReviewedAt())
                .adminNote(req.getAdminNote())
                .status(req.getStatus())
                .createdAt(req.getCreatedAt())
                .updatedAt(req.getUpdatedAt())
                .build();
    }

    private String resolveFileUrl(String fileRef) {
        if (fileRef == null || fileRef.isBlank()) return fileRef;
        if (fileRef.startsWith("data:") || fileRef.startsWith("http://") || fileRef.startsWith("https://")) {
            return fileRef;
        }
        return fileStoragePort.generatePresignedUrl(fileRef, Duration.ofHours(2));
    }
}
