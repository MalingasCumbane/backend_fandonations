package com.donations.donations.infrastructure.external.payment;

import com.donations.donations.application.port.out.PaymentGatewayPort;
import com.donations.donations.domain.model.enums.PaymentMethod;
import com.donations.donations.domain.model.payment.PaymentRequest;
import com.donations.donations.domain.model.payment.PaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@RequiredArgsConstructor
public class PaymentGatewayDispatcher implements PaymentGatewayPort {

    private final MpesaPaymentGateway mpesaPaymentGateway;
    private final EmolaPaymentGateway emolaPaymentGateway;

    @Override
    public PaymentResult charge(PaymentRequest request) {
        if (request.getMethod() == PaymentMethod.MPESA) {
            return mpesaPaymentGateway.charge(request);
        } else if (request.getMethod() == PaymentMethod.EMOLA) {
            return emolaPaymentGateway.charge(request);
        }
        return PaymentResult.builder()
                .success(false)
                .failureReason("Método de pagamento não suportado.")
                .build();
    }
}
