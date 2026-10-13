package com.donations.donations.dto.creator;

import com.donations.donations.model.enums.TransactionCategory;
import com.donations.donations.model.enums.TransactionType;
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
public class WalletTransactionResponseDto {
    private UUID id;
    private UUID creatorId;
    private String creatorUsername;
    private String creatorFullName;
    private TransactionType type;
    private TransactionCategory category;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private String referenceType;
    private String referenceId;
    private String description;
    private LocalDateTime createdAt;
}
