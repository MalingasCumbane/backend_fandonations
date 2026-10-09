package com.donations.donations.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlatformSettings {
    private UUID id;
    private BigDecimal platformFeePercentage;
    private BigDecimal minPayoutAmount;
    private boolean payoutsEnabled;
    private boolean registrationsOpen;
}
