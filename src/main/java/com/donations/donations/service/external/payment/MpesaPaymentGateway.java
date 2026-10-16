package com.donations.donations.service.external.payment;

import com.donations.donations.repository.PaymentGatewayRepository;
import com.donations.donations.model.payment.PaymentRequest;
import com.donations.donations.model.payment.PaymentResult;
import com.fc.sdk.APIContext;
import com.fc.sdk.APIMethodType;
import com.fc.sdk.APIRequest;
import com.fc.sdk.APIResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * Gateway oficial de pagamentos Vodacom Moçambique M-Pesa.
 * Utiliza o SDK oficial do portal (developer.vm.co.mz) empacotado em com.fc.sdk.*
 */
@Slf4j
@Service("mpesaGateway")
public class MpesaPaymentGateway implements PaymentGatewayRepository {

    @Value("${payments.mpesa.enabled}")
    private boolean enabled;

    @Value("${payments.mpesa.ssl}")
    private boolean ssl;

    @Value("${payments.mpesa.host}")
    private String host;

    @Value("${payments.mpesa.port}")
    private int port;

    @Value("${payments.mpesa.api-key}")
    private String apiKey;

    @Value("${payments.mpesa.public-key}")
    private String publicKey;

    @Value("${payments.mpesa.service-provider-code}")
    private String serviceProviderCode;

    @Value("${payments.mpesa.origin}")
    private String origin;

    /**
     * Processa um pagamento C2B Single-Stage usando o SDK oficial do M-Pesa.
     */
    @Override
    public PaymentResult charge(PaymentRequest request) {
        if (!enabled) {
            log.warn("Tentativa de cobrança via M-Pesa, mas o gateway está desativado (payments.mpesa.enabled=false).");
            return PaymentResult.builder().success(false).failureReason("O serviço de pagamentos M-Pesa está temporariamente desativado.").build();
        }

        if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(publicKey)) {
            log.error("Credenciais M-Pesa não configuradas no servidor.");
            return PaymentResult.builder().success(false).failureReason("Chaves da API do M-Pesa da Vodacom não configuradas no servidor.").build();
        }

        // Validação e normalização do telemóvel para Vodacom: 84 ou 85 -> 25884xxxxxxx / 25885xxxxxxx
        String normalizedPhone = normalizeMsisdn(request.getPhone());
        if (!isValidVodacomPhone(normalizedPhone)) {
            return PaymentResult.builder().success(false).failureReason("Número de telemóvel inválido para Vodacom M-Pesa. Deve pertencer à rede Vodacom (84 ou 85).").build();
        }

        String formattedAmount = request.getAmount().stripTrailingZeros().toPlainString();
        if (!formattedAmount.contains(".")) {
            // No portal Vodacom aceita formato inteiro ou decimal (ex.: "10" ou "10.00")
            formattedAmount = request.getAmount().setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
        }

        String transactionReference = sanitizeReference(request.getReference());
        String thirdPartyReference = request.getReference() != null ? request.getReference() : transactionReference;

        try {
            APIContext context = new APIContext();
            context.setApiKey(apiKey.trim());
            context.setPublicKey(publicKey.trim());
            context.setSsl(determineSsl(host, ssl));
            context.setMethodType(APIMethodType.POST);
            context.setAddress(cleanAddress(host));
            context.setPort(port);
            context.setPath("/ipg/v1x/c2bPayment/singleStage/");

            // Header Origin
            context.addHeader("Origin", StringUtils.hasText(origin) ? origin.trim() : "*");

            // Parâmetros obrigatórios da API C2B
            context.addParameter("input_TransactionReference", transactionReference);
            context.addParameter("input_CustomerMSISDN", normalizedPhone);
            context.addParameter("input_Amount", formattedAmount);
            context.addParameter("input_ThirdPartyReference", thirdPartyReference);
            context.addParameter("input_ServiceProviderCode", serviceProviderCode.trim());

            log.info("Executando APIRequest M-Pesa C2B via portal-sdk para {}:{} (Montante: {} MZN, Telefone: {}, Ref: {})", context.getAddress(), context.getPort(), formattedAmount, normalizedPhone, thirdPartyReference);

            APIRequest apiRequest = new APIRequest(context);
            APIResponse apiResponse = apiRequest.execute();

            if (apiResponse == null) {
                log.error("Resposta nula recebida do SDK M-Pesa da Vodacom.");
                return PaymentResult.builder().success(false).failureReason("Sem resposta da rede Vodacom M-Pesa.").build();
            }

            int statusCode = apiResponse.getStatusCode();
            String reason = apiResponse.getReason();
            String result = apiResponse.getResult();
            log.info("Resposta recebida do SDK M-Pesa: HTTP {} - {}, Resultado: {}", statusCode, reason, result);

            String responseCode = apiResponse.getParameter("output_ResponseCode");
            String responseDesc = apiResponse.getParameter("output_ResponseDesc");
            String transactionId = apiResponse.getParameter("output_TransactionID");
            String conversationId = apiResponse.getParameter("output_ConversationID");

            if ("INS-0".equalsIgnoreCase(responseCode)) {
                String finalRef = StringUtils.hasText(transactionId) ? transactionId : conversationId;
                return PaymentResult.builder().success(true).transactionReference(finalRef != null ? finalRef : thirdPartyReference).rawResponse(result).build();
            } else {
                String translatedReason = translateResponseCode(responseCode, responseDesc);
                log.warn("M-Pesa recusou transação C2B. Código: {}, Desc: {}, Motivo: {}", responseCode, responseDesc, translatedReason);
                return PaymentResult.builder().success(false).failureReason(translatedReason).rawResponse(result).build();
            }

        } catch (Exception e) {
            log.error("Exceção ao executar chamada M-Pesa via portal-sdk: {}", e.getMessage(), e);
            return PaymentResult.builder().success(false).failureReason("Erro ao comunicar com o servidor Vodacom M-Pesa: " + e.getMessage()).build();
        }
    }

    /**
     * Consulta o estado de uma transação na API do M-Pesa via portal-sdk.
     */
    @Override
    public PaymentResult queryTransactionStatus(String queryReference, String thirdPartyReference) {
        if (!enabled || !StringUtils.hasText(apiKey) || !StringUtils.hasText(publicKey)) {
            return PaymentResult.builder().success(false).failureReason("Gateway M-Pesa não configurado para consulta.").build();
        }

        try {
            APIContext context = new APIContext();
            context.setApiKey(apiKey.trim());
            context.setPublicKey(publicKey.trim());
            context.setSsl(determineSsl(host, ssl));
            context.setMethodType(APIMethodType.GET);
            context.setAddress(cleanAddress(host));
            context.setPort(port);
            context.setPath("/ipg/v1x/queryTransactionStatus/");
            context.addHeader("Origin", StringUtils.hasText(origin) ? origin.trim() : "*");

            context.addParameter("input_QueryReference", queryReference);
            context.addParameter("input_ThirdPartyReference", thirdPartyReference);
            context.addParameter("input_ServiceProviderCode", serviceProviderCode.trim());

            APIRequest request = new APIRequest(context);
            APIResponse response = request.execute();

            if (response == null) {
                return PaymentResult.builder().success(false).failureReason("Sem resposta da consulta M-Pesa.").build();
            }

            String code = response.getParameter("output_ResponseCode");
            String desc = response.getParameter("output_ResponseDesc");
            String txId = response.getParameter("output_TransactionID");

            boolean isSuccess = "INS-0".equalsIgnoreCase(code);
            return PaymentResult.builder().success(isSuccess).transactionReference(txId).failureReason(isSuccess ? null : translateResponseCode(code, desc)).rawResponse(response.getResult()).build();

        } catch (Exception e) {
            log.error("Erro ao consultar status no M-Pesa via SDK: {}", e.getMessage(), e);
            return PaymentResult.builder().success(false).failureReason("Erro ao consultar transação M-Pesa: " + e.getMessage()).build();
        }
    }

    /**
     * Envia um pagamento B2C (Business to Customer) para transferir fundos para o telemóvel do criador via portal-sdk.
     */
    @Override
    public PaymentResult b2cPayment(String phone, BigDecimal amount, String reference) {
        if (!enabled || !StringUtils.hasText(apiKey) || !StringUtils.hasText(publicKey)) {
            return PaymentResult.builder().success(false).failureReason("Gateway M-Pesa não configurado para desembolsos B2C.").build();
        }

        String normalizedPhone = normalizeMsisdn(phone);
        String formattedAmount = amount.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
        String txRef = sanitizeReference(reference);

        try {
            APIContext context = new APIContext();
            context.setApiKey(apiKey.trim());
            context.setPublicKey(publicKey.trim());
            context.setSsl(determineSsl(host, ssl));
            context.setMethodType(APIMethodType.POST);
            context.setAddress(cleanAddress(host));
            context.setPort(port);
            context.setPath("/ipg/v1x/b2cPayment/");
            context.addHeader("Origin", StringUtils.hasText(origin) ? origin.trim() : "*");

            context.addParameter("input_TransactionReference", txRef);
            context.addParameter("input_CustomerMSISDN", normalizedPhone);
            context.addParameter("input_Amount", formattedAmount);
            context.addParameter("input_ThirdPartyReference", reference != null ? reference : txRef);
            context.addParameter("input_ServiceProviderCode", serviceProviderCode.trim());

            APIRequest request = new APIRequest(context);
            APIResponse response = request.execute();

            if (response == null) {
                return PaymentResult.builder().success(false).failureReason("Sem resposta da transferência B2C.").build();
            }

            String code = response.getParameter("output_ResponseCode");
            String desc = response.getParameter("output_ResponseDesc");
            String txId = response.getParameter("output_TransactionID");

            boolean isSuccess = "INS-0".equalsIgnoreCase(code);
            return PaymentResult.builder().success(isSuccess).transactionReference(txId).failureReason(isSuccess ? null : translateResponseCode(code, desc)).rawResponse(response.getResult()).build();

        } catch (Exception e) {
            log.error("Erro ao emitir pagamento B2C M-Pesa via SDK: {}", e.getMessage(), e);
            return PaymentResult.builder().success(false).failureReason("Erro ao processar transferência B2C via M-Pesa: " + e.getMessage()).build();
        }
    }

    private String cleanAddress(String rawHost) {
        if (rawHost == null) return "api.sandbox.vm.co.mz";
        String clean = rawHost.trim();
        clean = clean.replaceFirst("^https?://", "");
        clean = clean.replaceFirst(":\\d+.*$", "");
        clean = clean.replaceFirst("/.*$", "");
        return clean.isEmpty() ? "api.sandbox.vm.co.mz" : clean;
    }

    private boolean determineSsl(String rawHost, boolean explicitSsl) {
        if (rawHost != null && rawHost.toLowerCase().startsWith("https://")) {
            return true;
        }
        return explicitSsl;
    }

    private String normalizeMsisdn(String phone) {
        if (phone == null) return "";
        String digits = phone.replaceAll("\\D", "");
        if (digits.startsWith("258") && digits.length() == 12) {
            return digits;
        }
        if (digits.length() == 9 && (digits.startsWith("84") || digits.startsWith("85"))) {
            return "258" + digits;
        }
        return digits;
    }

    private boolean isValidVodacomPhone(String phone) {
        if (phone == null) return false;
        return phone.matches("^258(84|85)\\d{7}$");
    }

    private String sanitizeReference(String ref) {
        if (ref == null || ref.isBlank()) {
            return "T" + System.currentTimeMillis();
        }
        String clean = ref.replaceAll("[^a-zA-Z0-9]", "");
        if (clean.length() > 20) {
            clean = clean.substring(0, 20);
        }
        return clean.isEmpty() ? "T" + System.currentTimeMillis() : clean;
    }

    public static String translateResponseCode(String code, String defaultDesc) {
        if (code == null || code.isBlank()) {
            return (defaultDesc != null && !defaultDesc.isBlank()) ? defaultDesc : "Falha na comunicação com o M-Pesa.";
        }
        return switch (code.toUpperCase().trim()) {
            case "INS-0" -> "Transação processada com sucesso.";
            case "INS-1" -> "Erro interno no sistema M-Pesa da Vodacom. Tente novamente mais tarde.";
            case "INS-2" -> "Serviço M-Pesa temporariamente indisponível.";
            case "INS-4" -> "A transação expirou. O tempo para introduzir o PIN no telemóvel terminou.";
            case "INS-5" -> "Transação cancelada pelo utilizador no telemóvel.";
            case "INS-6" -> "Transação falhou durante o processamento no M-Pesa.";
            case "INS-9" -> "Tempo limite excedido na rede M-Pesa da Vodacom.";
            case "INS-10" -> "Referência de transação duplicada.";
            case "INS-13" -> "O valor do apoio é inferior ao mínimo aceite pelo M-Pesa.";
            case "INS-14" -> "O valor do apoio ultrapassa o limite máximo permitido pelo M-Pesa.";
            case "INS-15" -> "A conta M-Pesa do cliente encontra-se inativa ou bloqueada.";
            case "INS-2001" -> "Referência de transação inválida no M-Pesa.";
            case "INS-2002" -> "Valor de transação inválido.";
            case "INS-2006" -> "Saldo insuficiente na sua conta M-Pesa para concluir a transação.";
            case "INS-2051" -> "Número de telemóvel inválido na rede M-Pesa (deve ser um número Vodacom 84 ou 85).";
            case "INS-2057" -> "Código de serviço de comerciante (shortcode) inválido.";
            default ->
                    (defaultDesc != null && !defaultDesc.isBlank()) ? defaultDesc : "Erro no M-Pesa (Código: " + code + ").";
        };
    }
}
