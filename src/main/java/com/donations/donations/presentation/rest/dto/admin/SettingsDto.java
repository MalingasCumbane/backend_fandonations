package com.donations.donations.presentation.rest.dto.admin;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class SettingsDto {
    private BigDecimal platformFeePercentage;
    private BigDecimal minPayoutAmount;
    private Boolean payoutsEnabled;
    private Boolean registrationsOpen;
}
