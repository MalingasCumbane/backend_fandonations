package com.donations.donations.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;
import com.donations.donations.domain.model.enums.ReportStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Report {
    private UUID id;
    private UUID creatorId;
    private String reason;
    private String details;
    private String reporterEmail;
    private ReportStatus status;
    private LocalDateTime createdAt;
}
