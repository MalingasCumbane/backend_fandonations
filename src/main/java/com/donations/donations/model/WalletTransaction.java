package com.donations.donations.model;

import com.donations.donations.model.enums.TransactionCategory;
import com.donations.donations.model.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "wallet_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class WalletTransaction extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID creatorId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionCategory category;

    @Column(precision = 14, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(precision = 14, scale = 2, nullable = false)
    private BigDecimal balanceAfter;

    private String referenceType;

    private String referenceId;

    @Column(columnDefinition = "TEXT")
    private String description;
}
