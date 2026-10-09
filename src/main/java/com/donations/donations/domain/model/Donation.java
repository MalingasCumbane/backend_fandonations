package com.donations.donations.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.donations.donations.domain.model.enums.DonationStatus;
import com.donations.donations.domain.model.enums.PaymentMethod;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Donation {
    private UUID id;
    private String reference;
    private String providerTransactionId;
    private String providerConversationId;
    private UUID creatorId;
    private UUID campaignId;
    private String supporterName;
    private String supporterEmail;
    private String supporterPhone;
    private String message;
    private boolean anonymous;
    private BigDecimal amount;
    private BigDecimal feeAmount;
    private BigDecimal netAmount;
    private PaymentMethod method;
    private DonationStatus status;
    private String failureReason;
    private LocalDateTime createdAt;
}
