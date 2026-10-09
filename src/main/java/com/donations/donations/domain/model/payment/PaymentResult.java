package com.donations.donations.domain.model.payment;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentResult {
    private boolean success;
    private String transactionReference;
    private String failureReason;
    private String rawResponse;
}
