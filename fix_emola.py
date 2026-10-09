import re

with open('src/main/java/com/donations/donations/service/external/payment/EmolaPaymentGateway.java', 'r') as f:
    content = f.read()

# Comment out the credentials check
old_check = """        if (!StringUtils.hasText(apiUrl) || !StringUtils.hasText(apiKey)) {
            return PaymentResult.builder()
                    .success(false)
                    .failureReason("Método de pagamento indisponível")
                    .rawResponse("Missing credentials")
                    .build();
        }"""
        
new_check = """        // Simulação não precisa de verificar chaves
        // if (!StringUtils.hasText(apiUrl) || !StringUtils.hasText(apiKey)) { ... }"""

content = content.replace(old_check, new_check)

with open('src/main/java/com/donations/donations/service/external/payment/EmolaPaymentGateway.java', 'w') as f:
    f.write(content)
