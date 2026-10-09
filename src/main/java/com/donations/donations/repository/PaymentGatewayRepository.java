package com.donations.donations.repository;

import com.donations.donations.model.payment.PaymentRequest;
import com.donations.donations.model.payment.PaymentResult;

public interface PaymentGatewayRepository {
    PaymentResult charge(PaymentRequest request);
}
