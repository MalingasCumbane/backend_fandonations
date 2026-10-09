package com.donations.donations.service.external.payment;

import com.donations.donations.repository.PaymentGatewayRepository;
import com.donations.donations.model.enums.PaymentMethod;
import com.donations.donations.model.payment.PaymentRequest;
import com.donations.donations.model.payment.PaymentResult;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
@RequiredArgsConstructor
public class PaymentGatewayDispatcher implements PaymentGatewayRepository {

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
