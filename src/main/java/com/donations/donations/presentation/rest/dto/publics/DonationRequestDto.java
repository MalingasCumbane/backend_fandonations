package com.donations.donations.presentation.rest.dto.publics;

import com.donations.donations.domain.model.enums.PaymentMethod;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class DonationRequestDto {
    private UUID creatorId;
    private UUID campaignId;
    private BigDecimal amount;
    private PaymentMethod method;
    private String phone;
    private String message;
    private boolean anonymous;
    private String supporterName;
    private String supporterEmail;
}
