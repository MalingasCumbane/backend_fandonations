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
        if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(publicKey)) {
            return PaymentResult.builder()
                    .success(false)
                    .failureReason("Método de pagamento indisponível")
                    .rawResponse("Missing credentials")
                    .build();
        }

        log.info("Attempting M-Pesa charge for amount {} to phone {}", request.getAmount(), request.getPhone());
        // Since we don't have the actual HTTP implementation here, we must fail.
        return PaymentResult.builder()
                .success(false)
                .failureReason("Integração M-Pesa não concluída")
                .rawResponse("{\"status\":\"FAILED\"}")
                .build();
    }
}
