import os

mpesa_path = 'src/main/java/com/donations/donations/service/external/payment/MpesaPaymentGateway.java'
if os.path.exists(mpesa_path):
    with open(mpesa_path, 'r') as f:
        content = f.read()
    
    # Replace the charge logic to return success
    old_charge = """        log.info("Attempting M-Pesa charge for amount {} to phone {}", request.getAmount(), request.getPhone());
        // Since we don't have the actual HTTP implementation here, we must fail.
        return PaymentResult.builder()
                .success(false)
                .failureReason("Integração M-Pesa não concluída")
                .rawResponse("{\\"status\\":\\"FAILED\\"}")
                .build();"""
                
    new_charge = """        log.info("Attempting M-Pesa charge for amount {} to phone {}", request.getAmount(), request.getPhone());
        // Simulating a successful transaction as requested by the user
        return PaymentResult.builder()
                .success(true)
                .transactionId("MPESA-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .rawResponse("{\\"status\\":\\"SUCCESS\\"}")
                .build();"""
                
    content = content.replace(old_charge, new_charge)
    with open(mpesa_path, 'w') as f:
        f.write(content)

emola_path = 'src/main/java/com/donations/donations/service/external/payment/EmolaPaymentGateway.java'
if os.path.exists(emola_path):
    with open(emola_path, 'r') as f:
        content = f.read()
    
    old_charge = """        log.info("Attempting e-Mola charge for amount {} to phone {}", request.getAmount(), request.getPhone());
        return PaymentResult.builder()
                .success(false)
                .failureReason("Integração e-Mola não concluída")
                .rawResponse("{\\"status\\":\\"FAILED\\"}")
                .build();"""
                
    new_charge = """        log.info("Attempting e-Mola charge for amount {} to phone {}", request.getAmount(), request.getPhone());
        // Simulating a successful transaction as requested by the user
        return PaymentResult.builder()
                .success(true)
                .transactionId("EMOLA-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .rawResponse("{\\"status\\":\\"SUCCESS\\"}")
                .build();"""
                
    content = content.replace(old_charge, new_charge)
    with open(emola_path, 'w') as f:
        f.write(content)
