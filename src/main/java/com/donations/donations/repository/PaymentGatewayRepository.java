package com.donations.donations.repository;

import com.donations.donations.model.payment.PaymentRequest;
import com.donations.donations.model.payment.PaymentResult;

import java.math.BigDecimal;

public interface PaymentGatewayRepository {
    PaymentResult charge(PaymentRequest request);

    default PaymentResult queryTransactionStatus(String queryReference, String thirdPartyReference) {
        return PaymentResult.builder()
                .success(false)
                .failureReason("Consulta não suportada por este gateway.")
                .build();
    }

    default PaymentResult b2cPayment(String phone, BigDecimal amount, String reference) {
        return PaymentResult.builder()
                .success(false)
                .failureReason("B2C não suportado por este gateway.")
                .build();
    }
}
