import os

mpesa_path = 'src/main/java/com/donations/donations/service/external/payment/MpesaPaymentGateway.java'
if os.path.exists(mpesa_path):
    with open(mpesa_path, 'r') as f:
        content = f.read()
    
    old_validation = """        if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(publicKey)) {
            return PaymentResult.builder()
                    .success(false)
                    .failureReason("Método de pagamento indisponível")
                    .rawResponse("Missing credentials")
                    .build();
        }"""
    
    new_validation = """        // Simulação não precisa de verificar chaves (apiKey/publicKey)
        // if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(publicKey)) { ... }"""
        
    content = content.replace(old_validation, new_validation)
    with open(mpesa_path, 'w') as f:
        f.write(content)

emola_path = 'src/main/java/com/donations/donations/service/external/payment/EmolaPaymentGateway.java'
if os.path.exists(emola_path):
    with open(emola_path, 'r') as f:
        content = f.read()
    
    old_validation = """        if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(merchantId)) {
            return PaymentResult.builder()
                    .success(false)
                    .failureReason("Método de pagamento indisponível")
                    .rawResponse("Missing credentials")
                    .build();
        }"""
    
    new_validation = """        // Simulação não precisa de verificar chaves (apiKey/merchantId)
        // if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(merchantId)) { ... }"""
        
    content = content.replace(old_validation, new_validation)
    with open(emola_path, 'w') as f:
        f.write(content)
