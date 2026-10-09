package com.donations.donations.infrastructure.persistence.mapper;

import com.donations.donations.domain.model.VerificationRequest;
import com.donations.donations.infrastructure.persistence.entity.VerificationRequestJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class VerificationRequestMapper {

    public static VerificationRequest toDomain(VerificationRequestJpaEntity entity) {
        if (entity == null) return null;
        return VerificationRequest.builder()
                .id(entity.getId())
                .creatorId(entity.getCreatorId())
                .documentType(entity.getDocumentType())
                .documentNumber(entity.getDocumentNumber())
                .fullName(entity.getFullName())
                .documentFile(entity.getDocumentFile())
                .documentFileType(entity.getDocumentFileType())
                .selfieImage(entity.getSelfieImage())
                .note(entity.getNote())
                .status(entity.getStatus())
                .submittedAt(entity.getSubmittedAt())
                .reviewedAt(entity.getReviewedAt())
                .adminNote(entity.getAdminNote())
                .build();
    }

    public static VerificationRequestJpaEntity toEntity(VerificationRequest domain) {
        if (domain == null) return null;
        return VerificationRequestJpaEntity.builder()
                .id(domain.getId())
                .creatorId(domain.getCreatorId())
                .documentType(domain.getDocumentType())
                .documentNumber(domain.getDocumentNumber())
                .fullName(domain.getFullName())
                .documentFile(domain.getDocumentFile())
                .documentFileType(domain.getDocumentFileType())
                .selfieImage(domain.getSelfieImage())
                .note(domain.getNote())
                .status(domain.getStatus())
                .submittedAt(domain.getSubmittedAt())
                .reviewedAt(domain.getReviewedAt())
                .adminNote(domain.getAdminNote())
                .build();
    }
}
