package com.donations.donations.service.external.payment;

import com.donations.donations.repository.PaymentGatewayRepository;
import com.donations.donations.model.payment.PaymentRequest;
import com.donations.donations.model.payment.PaymentResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service("mpesaGateway")
public class MpesaPaymentGateway implements PaymentGatewayRepository {

    @Value("${payments.mpesa.api-key:}")
    private String apiKey;

    @Value("${payments.mpesa.public-key:}")
    private String publicKey;

    @Override
    public PaymentResult charge(PaymentRequest request) {
        // Simulação não precisa de verificar chaves (apiKey/publicKey)
        // if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(publicKey)) { ... }

        log.info("Attempting M-Pesa charge for amount {} to phone {}", request.getAmount(), request.getPhone());
        // Simulating a successful transaction as requested by the user
        return PaymentResult.builder()
                .success(true)
                .transactionReference("MPESA-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .rawResponse("{\"status\":\"SUCCESS\"}")
                .build();
    }
}
