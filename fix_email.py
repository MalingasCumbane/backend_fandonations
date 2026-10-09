import re

# 1. Update EmailRepository
with open('src/main/java/com/donations/donations/repository/EmailRepository.java', 'r') as f:
    content = f.read()

content = content.replace('void sendPhoneOtpEmail(String toEmail, String otp, String phone);', 'void sendPhoneOtpEmail(String toEmail, String otp, String phone);\n    void sendAccountStatusEmail(String toEmail, String status, String note);')

with open('src/main/java/com/donations/donations/repository/EmailRepository.java', 'w') as f:
    f.write(content)

# 2. Update RabbitMQEmailPublisherAdapter
with open('src/main/java/com/donations/donations/service/external/RabbitMQEmailPublisherAdapter.java', 'r') as f:
    content = f.read()

new_method = """
    @Override
    public void sendAccountStatusEmail(String toEmail, String status, String note) {
        String statusText = status.equals("VERIFIED") ? "Aprovada!" : "Rejeitada";
        String emoji = status.equals("VERIFIED") ? "✅" : "❌";
        String message = status.equals("VERIFIED") 
            ? "A sua conta foi verificada com sucesso! O seu perfil agora está ativo e bloqueado para manter a integridade dos seus dados. Já pode partilhar o seu link e receber apoios!"
            : "Infelizmente, não foi possível aprovar a sua conta no momento. Por favor, reveja os seus documentos e tente novamente.";
        String noteHtml = (note != null && !note.isEmpty()) ? "<p style='margin-top: 20px; font-weight: bold;'>Nota da equipa: " + note + "</p>" : "";

        String htmlBody = \"\"\"
        <!DOCTYPE html>
        <html lang="pt">
        <head>
            <meta charset="utf-8">
        </head>
        <body style="font-family: sans-serif; padding: 40px;">
            <h2>Estado da sua conta: %s %s</h2>
            <p>%s</p>
            %s
        </body>
        </html>
        \"\"\".formatted(statusText, emoji, message, noteHtml);
        
        EmailEvent event = EmailEvent.builder()
                .toEmail(toEmail)
                .subject("Atualização do Estado da Conta - APOIA")
                .body(htmlBody)
                .build();
                
        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_EXCHANGE, RabbitMQConfig.EMAIL_ROUTING_KEY, event);
    }
}
"""
content = content.replace("}\n", new_method, 1)

with open('src/main/java/com/donations/donations/service/external/RabbitMQEmailPublisherAdapter.java', 'w') as f:
    f.write(content)
