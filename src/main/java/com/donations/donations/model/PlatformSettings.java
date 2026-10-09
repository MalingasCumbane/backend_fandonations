package com.donations.donations.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "platform_settings")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class PlatformSettings extends TimeStamp {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(precision = 14, scale = 2)
    private BigDecimal platformFeePercentage;

    @Column(precision = 14, scale = 2)
    private BigDecimal minPayoutAmount;

    private boolean payoutsEnabled;

    private boolean registrationsOpen;
}
