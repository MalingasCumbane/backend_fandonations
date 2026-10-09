package com.donations.donations.application.port.out;

import com.donations.donations.domain.model.payment.PaymentRequest;
import com.donations.donations.domain.model.payment.PaymentResult;

public interface PaymentGatewayPort {
    PaymentResult charge(PaymentRequest request);
}
