with open('src/main/java/com/donations/donations/repository/EmailRepository.java', 'r') as f:
    content = f.read()

if 'sendDonationReceiptEmail' not in content:
    content = content.replace("void sendAccountStatusEmail(String toEmail, String status, String note);", "void sendAccountStatusEmail(String toEmail, String status, String note);\n    void sendDonationReceiptEmail(String toEmail, String creatorName, String amount, String reference);")
    with open('src/main/java/com/donations/donations/repository/EmailRepository.java', 'w') as f:
        f.write(content)

with open('src/main/java/com/donations/donations/service/external/RabbitMQEmailPublisherAdapter.java', 'r') as f:
    content = f.read()

new_method = """
    @Override
    public void sendDonationReceiptEmail(String toEmail, String creatorName, String amount, String reference) {
        String htmlBody = \"\"\"
        <!DOCTYPE html>
        <html lang="pt">
        <head>
            <meta charset="utf-8">
        </head>
        <body style="font-family: sans-serif; padding: 40px; color: #111827;">
            <h2 style="color: #111827;">Pagamento Confirmado! 🎉</h2>
            <p>O seu apoio de <strong>%s MT</strong> para <strong>%s</strong> foi recebido com sucesso.</p>
            <p>A sua referência de transação é: <strong>%s</strong>.</p>
            <p>O criador agradece imenso o seu apoio!</p>
            <br/>
            <p style="font-size: 12px; color: #6b7280;">Equipa APOIA</p>
        </body>
        </html>
        \"\"\".formatted(amount, creatorName, reference);
        
        EmailEvent event = EmailEvent.builder()
                .toEmail(toEmail)
                .subject("Obrigado pelo seu apoio! (Ref: " + reference + ")")
                .body(htmlBody)
                .build();
                
        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_EXCHANGE, RabbitMQConfig.EMAIL_ROUTING_KEY, event);
    }
}
"""

if 'sendDonationReceiptEmail' not in content:
    content = content.replace("    }\n}", "    }\n" + new_method)
    with open('src/main/java/com/donations/donations/service/external/RabbitMQEmailPublisherAdapter.java', 'w') as f:
        f.write(content)
