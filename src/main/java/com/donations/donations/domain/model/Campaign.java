package com.donations.donations.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import com.donations.donations.domain.model.enums.CampaignStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Campaign {
    private UUID id;
    private UUID creatorId;
    private String title;
    private String description;
    private String imageUrl;
    private BigDecimal goalAmount;
    private BigDecimal raisedAmount;
    private LocalDate startDate;
    private LocalDate endDate;
    private CampaignStatus status;
    private LocalDateTime createdAt;
}
