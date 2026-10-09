package com.donations.donations.service.external;

import com.donations.donations.repository.EmailRepository;
import com.donations.donations.config.RabbitMQConfig;
import com.donations.donations.dto.EmailEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMQEmailPublisherAdapter implements EmailRepository {

    private final RabbitTemplate rabbitTemplate;

    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        String verificationUrl = "http://localhost:8080/verificar-token?token=" + token;
        
        String htmlBody = """
        <!DOCTYPE html>
        <html lang="pt">
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');
                body { font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #fafafa; margin: 0; padding: 40px 20px; color: #111827; -webkit-font-smoothing: antialiased; }
                .wrapper { max-width: 520px; margin: 0 auto; }
                .logo { text-align: center; margin-bottom: 30px; }
                .logo h1 { color: #111827; margin: 0; font-size: 28px; font-weight: 800; letter-spacing: -0.5px; }
                .logo span { color: #6366f1; }
                .card { background-color: #ffffff; border-radius: 16px; padding: 40px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03); border: 1px solid #f3f4f6; text-align: center; }
                .icon { background-color: #eef2ff; color: #6366f1; width: 64px; height: 64px; border-radius: 100px; display: inline-flex; align-items: center; justify-content: center; font-size: 28px; margin-bottom: 24px; }
                .title { font-size: 22px; font-weight: 700; margin-top: 0; margin-bottom: 12px; color: #111827; letter-spacing: -0.3px; }
                .text { font-size: 15px; line-height: 1.6; color: #4b5563; margin-bottom: 32px; }
                .btn { display: inline-block; background-color: #111827; color: #ffffff !important; font-weight: 600; font-size: 15px; text-decoration: none; padding: 16px 32px; border-radius: 9999px; transition: background-color 0.2s, transform 0.2s; box-shadow: 0 4px 14px 0 rgba(0,0,0,0.1); }
                .btn:hover { background-color: #374151; transform: translateY(-1px); }
                .divider { height: 1px; background-color: #f3f4f6; margin: 32px 0; }
                .subtext { font-size: 13px; color: #9ca3af; line-height: 1.5; }
                .raw-link { color: #6366f1; text-decoration: underline; word-break: break-all; }
                .footer { text-align: center; margin-top: 32px; font-size: 13px; color: #9ca3af; }
            </style>
        </head>
        <body>
            <div class="wrapper">
                <div class="logo">
                    <h1>APOI<span>A</span></h1>
                </div>
                <div class="card">
                    <div class="icon">✉️</div>
                    <h2 class="title">Verifique o seu email</h2>
                    <p class="text">Bem-vindo(a) ao APOIA! Para garantir a segurança da sua conta e começar a receber donativos, precisamos que confirme o seu endereço de email.</p>
                    <a href="%s" class="btn">Confirmar o meu email</a>
                    
                    <div class="divider"></div>
                    
                    <p class="subtext">Se o botão não funcionar, copie e cole o seguinte link no seu navegador:<br>
                    <a href="%s" class="raw-link">%s</a></p>
                </div>
                <div class="footer">
                    &copy; 2026 APOIA Inc. &bull; Maputo, Moçambique<br>
                    Não criou nenhuma conta? Pode apagar este email.
                </div>
            </div>
        </body>
        </html>
        """.formatted(verificationUrl, verificationUrl, verificationUrl);
        
        EmailEvent event = EmailEvent.builder()
                .toEmail(toEmail)
                .subject("Ative a sua conta no APOIA")
                .body(htmlBody)
                .build();
                
        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_EXCHANGE, RabbitMQConfig.EMAIL_ROUTING_KEY, event);
    }

    @Override
    public void sendWelcomeEmail(String toEmail) {
        String htmlBody = """
        <!DOCTYPE html>
        <html lang="pt">
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');
                body { font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #fafafa; margin: 0; padding: 40px 20px; color: #111827; -webkit-font-smoothing: antialiased; }
                .wrapper { max-width: 520px; margin: 0 auto; }
                .logo { text-align: center; margin-bottom: 30px; }
                .logo h1 { color: #111827; margin: 0; font-size: 28px; font-weight: 800; letter-spacing: -0.5px; }
                .logo span { color: #6366f1; }
                .card { background-color: #ffffff; border-radius: 16px; padding: 40px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03); border: 1px solid #f3f4f6; text-align: center; }
                .icon-success { font-size: 56px; margin-bottom: 20px; display: inline-block; animation: pulse 2s infinite; }
                .title { font-size: 24px; font-weight: 700; margin-top: 0; margin-bottom: 12px; color: #111827; letter-spacing: -0.3px; }
                .text { font-size: 15px; line-height: 1.6; color: #4b5563; margin-bottom: 32px; }
                .highlight-box { background-color: #f8fafc; border-radius: 12px; padding: 20px; margin-bottom: 32px; text-align: left; }
                .highlight-box ul { margin: 0; padding-left: 20px; color: #4b5563; font-size: 14px; line-height: 1.8; }
                .highlight-box li strong { color: #111827; }
                .btn { display: inline-block; background-color: #6366f1; color: #ffffff !important; font-weight: 600; font-size: 15px; text-decoration: none; padding: 16px 32px; border-radius: 9999px; transition: background-color 0.2s, transform 0.2s; box-shadow: 0 4px 14px 0 rgba(99, 102, 241, 0.3); }
                .btn:hover { background-color: #4f46e5; transform: translateY(-1px); }
                .footer { text-align: center; margin-top: 32px; font-size: 13px; color: #9ca3af; }
            </style>
        </head>
        <body>
            <div class="wrapper">
                <div class="logo">
                    <h1>APOI<span>A</span></h1>
                </div>
                <div class="card">
                    <span class="icon-success">🚀</span>
                    <h2 class="title">Tudo pronto!</h2>
                    <p class="text">O seu email foi verificado com sucesso. Bem-vindo(a) oficialmente à maior comunidade de criadores!</p>
                    
                    <div class="highlight-box">
                        <p style="margin-top: 0; font-weight: 600; color: #111827; font-size: 15px;">O que fazer a seguir?</p>
                        <ul>
                            <li><strong>Complete o seu perfil:</strong> Adicione a sua foto e biografia.</li>
                            <li><strong>Crie uma campanha:</strong> Diga aos seus fãs o que quer alcançar.</li>
                            <li><strong>Partilhe o seu link:</strong> Publique nas suas redes sociais!</li>
                        </ul>
                    </div>
                    
                    <a href="http://localhost:8080/dashboard" class="btn">Aceder ao Dashboard</a>
                </div>
                <div class="footer">
                    &copy; 2026 APOIA Inc. &bull; Maputo, Moçambique
                </div>
            </div>
        </body>
        </html>
        """;
        
        EmailEvent event = EmailEvent.builder()
                .toEmail(toEmail)
                .subject("Bem-vindo(a) ao APOIA! \uD83C\uDF89")
                .body(htmlBody)
                .build();
                
        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_EXCHANGE, RabbitMQConfig.EMAIL_ROUTING_KEY, event);
    }

    @Override
    public void sendPhoneOtpEmail(String toEmail, String otp, String phone) {
        String htmlBody = """
        <!DOCTYPE html>
        <html lang="pt">
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');
                body { font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #fafafa; margin: 0; padding: 40px 20px; color: #111827; -webkit-font-smoothing: antialiased; }
                .wrapper { max-width: 520px; margin: 0 auto; }
                .logo { text-align: center; margin-bottom: 30px; }
                .logo h1 { color: #111827; margin: 0; font-size: 28px; font-weight: 800; letter-spacing: -0.5px; }
                .logo span { color: #6366f1; }
                .card { background-color: #ffffff; border-radius: 16px; padding: 40px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05), 0 2px 4px -1px rgba(0, 0, 0, 0.03); border: 1px solid #f3f4f6; text-align: center; }
                .title { font-size: 22px; font-weight: 700; margin-top: 0; margin-bottom: 12px; color: #111827; letter-spacing: -0.3px; }
                .text { font-size: 15px; line-height: 1.6; color: #4b5563; margin-bottom: 32px; }
                .otp-box { background-color: #f3f4f6; border-radius: 12px; padding: 24px; margin-bottom: 32px; font-size: 32px; font-weight: 800; letter-spacing: 8px; color: #111827; }
                .footer { text-align: center; margin-top: 32px; font-size: 13px; color: #9ca3af; }
            </style>
        </head>
        <body>
            <div class="wrapper">
                <div class="logo">
                    <h1>APOI<span>A</span></h1>
                </div>
                <div class="card">
                    <h2 class="title">Verificação de Número</h2>
                    <p class="text">Solicitou a verificação do número de telemóvel <strong>%s</strong>. Use o código abaixo para confirmar:</p>
                    <div class="otp-box">%s</div>
                    <p class="text" style="font-size: 13px; margin-bottom: 0;">Este código expira em 10 minutos. Não partilhe este código com ninguém.</p>
                </div>
                <div class="footer">
                    &copy; 2026 APOIA Inc. &bull; Maputo, Moçambique
                </div>
            </div>
        </body>
        </html>
        """.formatted(phone, otp);
        
        EmailEvent event = EmailEvent.builder()
                .toEmail(toEmail)
                .subject("Código de Verificação: " + otp)
                .body(htmlBody)
                .build();
                
        rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_EXCHANGE, RabbitMQConfig.EMAIL_ROUTING_KEY, event);
    }
}
