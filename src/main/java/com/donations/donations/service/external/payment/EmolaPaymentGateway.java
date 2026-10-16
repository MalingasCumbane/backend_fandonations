package com.donations.donations.service.external.payment;

import com.donations.donations.repository.PaymentGatewayRepository;
import com.donations.donations.model.payment.PaymentRequest;
import com.donations.donations.model.payment.PaymentResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service("emolaGateway")
public class EmolaPaymentGateway implements PaymentGatewayRepository {

    @Value("${payments.emola.api-url:}")
    private String apiUrl;

    @Value("${payments.emola.api-key:}")
    private String apiKey;

    @Override
    public PaymentResult charge(PaymentRequest request) {
        // Simulação não precisa de verificar chaves
        // if (!StringUtils.hasText(apiUrl) || !StringUtils.hasText(apiKey)) { ... }

        log.info("Attempting e-Mola charge for amount {} to phone {}", request.getAmount(), request.getPhone());
        // Simulating a successful transaction as requested by the user
        return PaymentResult.builder().success(true).transactionReference("EMOLA-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase()).rawResponse("{\"status\":\"SUCCESS\"}").build();
    }
}
