package com.donations.donations.domain.model;

import com.donations.donations.domain.model.enums.PaymentMethod;
import com.donations.donations.domain.model.enums.PayoutStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payout {
    private UUID id;
    private UUID creatorId;
    private BigDecimal amount;
    private PaymentMethod method;
    private String phone;
    private PayoutStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime processedAt;
    private String adminNote;
    private String transactionReference;
    private LocalDateTime createdAt;
}
