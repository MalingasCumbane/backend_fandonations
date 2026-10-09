package com.donations.donations.service.external.payment;

import com.donations.donations.repository.PaymentGatewayRepository;
import com.donations.donations.model.enums.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentGatewayFactory {

    private final Map<String, PaymentGatewayRepository> gateways;

    public PaymentGatewayRepository getGateway(PaymentMethod method) {
        switch (method) {
            case MPESA:
                return gateways.get("mpesaGateway");
            case EMOLA:
                return gateways.get("emolaGateway");
            default:
                throw new IllegalArgumentException("Unknown payment method: " + method);
        }
    }
}
