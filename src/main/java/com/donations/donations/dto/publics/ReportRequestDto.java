package com.donations.donations.dto.publics;

import lombok.Data;

import java.util.UUID;

@Data
public class ReportRequestDto {
    private UUID creatorId;
    private String reason;
    private String details;
    private String reporterEmail;
}
