package com.donations.donations.domain.model;

import com.donations.donations.domain.model.enums.VerificationRequestStatus;
import lombok.Builder;
import lombok.Data;

import java.util.UUID;
import java.time.LocalDateTime;

@Data
@Builder
public class VerificationRequest {
    private UUID id;
    private UUID creatorId;
    private String documentType;
    private String documentNumber;
    private String fullName;
    private String documentFile;
    private String selfieImage;
    private String note;
    private String documentFileType;
    private LocalDateTime submittedAt;
    private LocalDateTime reviewedAt;
    private String adminNote;
    private VerificationRequestStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}