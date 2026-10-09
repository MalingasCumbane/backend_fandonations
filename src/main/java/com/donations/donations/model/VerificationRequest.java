package com.donations.donations.model;

import com.donations.donations.model.enums.VerificationRequestStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "verification_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class VerificationRequest extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID creatorId;

    @Column(nullable = false)
    private String documentType;

    @Column(nullable = false)
    private String documentNumber;

    @Column(nullable = false)
    private String fullName;

    @Column(columnDefinition = "LONGTEXT")
    private String documentFile;

    @Column(columnDefinition = "LONGTEXT")
    private String selfieImage;

    @Column(columnDefinition = "TEXT")
    private String note;
    private String documentFileType;
    private java.time.LocalDateTime submittedAt;
    private java.time.LocalDateTime reviewedAt;
    private String adminNote;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VerificationRequestStatus status;
}