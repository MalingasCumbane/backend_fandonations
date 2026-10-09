package com.donations.donations.presentation.rest.dto.publics;

import com.donations.donations.domain.model.enums.DonationStatus;
import com.donations.donations.domain.model.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PublicDonationStatus {
    private String reference;
    private DonationStatus status;
    private BigDecimal amount;
    private PaymentMethod method;
    private String failureReason;
    private String creatorUsername;
}
