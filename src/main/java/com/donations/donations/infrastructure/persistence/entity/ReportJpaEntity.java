package com.donations.donations.infrastructure.persistence.entity;

import com.donations.donations.domain.model.enums.ReportStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ReportJpaEntity extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID creatorId;

    private String reason;

    @Column(columnDefinition = "TEXT")
    private String details;

    private String reporterEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;
}
