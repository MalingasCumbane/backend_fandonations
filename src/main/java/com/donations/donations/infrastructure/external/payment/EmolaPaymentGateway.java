package com.donations.donations.infrastructure.external.payment;

import com.donations.donations.application.port.out.PaymentGatewayPort;
import com.donations.donations.domain.model.payment.PaymentRequest;
import com.donations.donations.domain.model.payment.PaymentResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service("emolaGateway")
public class EmolaPaymentGateway implements PaymentGatewayPort {

    @Value("${payments.emola.api-url:}")
    private String apiUrl;

    @Value("${payments.emola.api-key:}")
    private String apiKey;

    @Override
    public PaymentResult charge(PaymentRequest request) {
        if (!StringUtils.hasText(apiUrl) || !StringUtils.hasText(apiKey)) {
            return PaymentResult.builder()
                    .success(false)
                    .failureReason("Método de pagamento indisponível")
                    .rawResponse("Missing credentials")
                    .build();
        }

        log.info("Attempting e-Mola charge for amount {} to phone {}", request.getAmount(), request.getPhone());
        return PaymentResult.builder()
                .success(false)
                .failureReason("Integração e-Mola não concluída")
                .rawResponse("{\"status\":\"FAILED\"}")
                .build();
    }
}
