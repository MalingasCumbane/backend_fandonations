package com.donations.donations.infrastructure.external.payment;

import com.donations.donations.application.port.out.PaymentGatewayPort;
import com.donations.donations.domain.model.enums.PaymentMethod;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PaymentGatewayFactory {

    private final Map<String, PaymentGatewayPort> gateways;

    public PaymentGatewayPort getGateway(PaymentMethod method) {
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
