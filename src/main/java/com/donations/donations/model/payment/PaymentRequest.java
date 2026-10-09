package com.donations.donations.model.payment;

import com.donations.donations.model.enums.PaymentMethod;
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
