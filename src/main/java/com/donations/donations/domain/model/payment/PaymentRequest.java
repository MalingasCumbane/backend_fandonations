package com.donations.donations.domain.model.payment;

import com.donations.donations.domain.model.enums.PaymentMethod;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class PaymentRequest {
    private UUID donationId;
    private String phone;
    private BigDecimal amount;
    private PaymentMethod method;
    private String reference;
}
