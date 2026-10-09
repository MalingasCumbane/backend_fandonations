package com.donations.donations.infrastructure.persistence.entity;

import com.donations.donations.domain.model.enums.CampaignStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table
//@org.hibernate.annotations.Where(clause = "is_deleted = false")(name = "campaigns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class CampaignJpaEntity extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID creatorId;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imageUrl;

    @Column(precision = 14, scale = 2)
    private BigDecimal goalAmount;

    @Column(precision = 14, scale = 2)
    @Builder.Default
    private BigDecimal raisedAmount = BigDecimal.ZERO;

    private LocalDate startDate;
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CampaignStatus status;
}
