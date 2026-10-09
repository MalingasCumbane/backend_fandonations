package com.donations.donations.infrastructure.persistence.entity;

import com.donations.donations.domain.model.enums.DonationStatus;
import com.donations.donations.domain.model.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "donations")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DonationJpaEntity extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID creatorId;

    private UUID campaignId;

    private String supporterName;
    private String supporterEmail;
    private String supporterPhone;

    @Column(columnDefinition = "TEXT")
    private String message;

    private boolean anonymous;

    @Column(precision = 14, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(precision = 14, scale = 2)
    private BigDecimal feeAmount;

    @Column(precision = 14, scale = 2)
    private BigDecimal netAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DonationStatus status;

    @Column(columnDefinition = "TEXT")
    private String failureReason;
}
