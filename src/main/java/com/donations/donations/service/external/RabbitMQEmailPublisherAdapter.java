package com.donations.donations.service.external;

import com.donations.donations.repository.EmailRepository;
import com.donations.donations.config.RabbitMQConfig;
import com.donations.donations.dto.EmailEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RabbitMQEmailPublisherAdapter implements EmailRepository {

    private final RabbitTemplate rabbitTemplate;
    private final EmailDeliveryService emailDeliveryService;

    @Value("${app.frontend.url:https://txopa.app}")
    private String frontendUrl;

    private void dispatch(EmailEvent event) {
        if (event == null || event.getToEmail() == null || event.getToEmail().isBlank()) {
            return;
        }

        boolean queued = false;
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_EXCHANGE, RabbitMQConfig.EMAIL_ROUTING_KEY, event);
            queued = true;
        } catch (Exception e) {
            System.err.println("[RabbitMQ] Indisponível (" + e.getMessage() + "). A despachar email assincronamente via SMTP para " + event.getToEmail());
        }

        if (!queued) {
            emailDeliveryService.sendEmailAsync(event);
        }
    }

    @Override
    public void sendVerificationEmail(String toEmail, String token) {
        String verificationUrl = frontendUrl + "/verificar-token?token=" + token;

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
                            <h1>TXOP<span>A</span></h1>
                        </div>
                        <div class="card">
                            <div class="icon">✉️</div>
                            <h2 class="title">Verifique o seu email</h2>
                            <p class="text">Bem-vindo(a) ao Txopa! Para garantir a segurança da sua conta e começar a receber donativos, precisamos que confirme o seu endereço de email.</p>
                            <a href="%s" class="btn">Confirmar o meu email</a>
                
                            <div class="divider"></div>
                
                            <p class="subtext">Se o botão não funcionar, copie e cole o seguinte link no seu navegador:<br>
                            <a href="%s" class="raw-link">%s</a></p>
                        </div>
                        <div class="footer">
                            &copy; 2026 Txopa Inc. &bull; Maputo, Moçambique<br>
                            Não criou nenhuma conta? Pode apagar este email.
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(verificationUrl, verificationUrl, verificationUrl);

        EmailEvent event = EmailEvent.builder()
                .toEmail(toEmail)
                .subject("Ative a sua conta no Txopa")
                .body(htmlBody)
                .build();

        dispatch(event);
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
                            <h1>TXOP<span>A</span></h1>
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
                
                            <a href="https://txopa.app/dashboard" class="btn">Aceder ao Dashboard</a>
                        </div>
                        <div class="footer">
                            &copy; 2026 Txopa Inc. &bull; Maputo, Moçambique
                        </div>
                    </div>
                </body>
                </html>
                """;

        EmailEvent event = EmailEvent.builder()
                .toEmail(toEmail)
                .subject("Bem-vindo(a) ao Txopa! \uD83C\uDF89")
                .body(htmlBody)
                .build();

        dispatch(event);
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
                            <h1>TXOP<span>A</span></h1>
                        </div>
                        <div class="card">
                            <h2 class="title">Verificação de Número</h2>
                            <p class="text">Solicitou a verificação do número de telemóvel. Use o código abaixo para confirmar:</p>
                            <div class="otp-box">%s</div>
                            <p class="text" style="font-size: 13px; margin-bottom: 0;">Este código expira em 10 minutos. Não partilhe este código com ninguém.</p>
                        </div>
                        <div class="footer">
                            &copy; 2026 Txopa Inc. &bull; Maputo, Moçambique
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(otp);
//                """.formatted(phone, otp);

        EmailEvent event = EmailEvent.builder()
                .toEmail(toEmail)
                .subject("Código de Verificação: " + otp)
                .body(htmlBody)
                .build();

        dispatch(event);
    }

    @Override
    public void sendAccountStatusEmail(String toEmail, String status, String note) {
        String statusText = status.equals("VERIFIED") ? "Aprovada!" : "Rejeitada";
        String emoji = status.equals("VERIFIED") ? "✅" : "❌";
        String message = status.equals("VERIFIED")
                ? "A sua conta foi verificada com sucesso! O seu perfil agora está ativo e bloqueado para manter a integridade dos seus dados. Já pode partilhar o seu link e receber apoios!"
                : "Infelizmente, não foi possível aprovar a sua conta no momento. Por favor, reveja os seus documentos e tente novamente.";
        String noteHtml = (note != null && !note.isEmpty()) ? "<p style='margin-top: 20px; font-weight: bold;'>Nota da equipa: " + note + "</p>" : "";

        String htmlBody = """
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
                """.formatted(statusText, emoji, message, noteHtml);

        EmailEvent event = EmailEvent.builder()
                .toEmail(toEmail)
                .subject("Atualização do Estado da Conta - Txopa")
                .body(htmlBody)
                .build();

        dispatch(event);
    }

    @Override
    public void sendDonationReceiptEmail(String toEmail, String creatorName, String amount, String reference, String method) {
        byte[] pdfReceipt = null;
        try {
            pdfReceipt = com.donations.donations.util.PdfReceiptGenerator.generateReceipt(
                toEmail, creatorName, amount, reference, method
            );
        } catch (Exception e) {
            System.err.println("Erro ao gerar PDF de recibo: " + e.getMessage());
        }

        String htmlBody = """
        <!DOCTYPE html>
        <html lang="pt">
        <head>
            <meta charset="utf-8">
            <style>
                body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; background-color: #f8fafc; margin: 0; padding: 30px; color: #0f172a; }
                .card { max-width: 580px; margin: 0 auto; background: #ffffff; border-radius: 12px; border: 1px solid #e2e8f0; overflow: hidden; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05); }
                .header { background: #0f172a; padding: 28px 32px; color: #ffffff; }
                .header h1 { margin: 0; font-size: 22px; font-weight: 700; letter-spacing: 0.5px; }
                .header p { margin: 4px 0 0; font-size: 12px; color: #94a3b8; }
                .content { padding: 32px; }
                .badge { display: inline-block; background: #ecfdf5; color: #065f46; font-size: 12px; font-weight: 600; padding: 4px 10px; border-radius: 9999px; margin-bottom: 16px; border: 1px solid #a7f3d0; }
                .title { font-size: 20px; font-weight: 700; margin: 0 0 8px; color: #0f172a; }
                .desc { font-size: 14px; line-height: 1.6; color: #475569; margin: 0 0 24px; }
                .receipt-box { background: #f8fafc; border: 1px solid #e2e8f0; border-radius: 8px; padding: 20px; margin-bottom: 24px; }
                .pdf-notice { background: #eff6ff; border: 1px solid #bfdbfe; border-radius: 6px; padding: 12px 16px; font-size: 13px; color: #1e40af; margin-bottom: 24px; }
                .footer { padding: 20px 32px; background: #f8fafc; border-top: 1px solid #e2e8f0; font-size: 11px; color: #94a3b8; text-align: center; }
            </style>
        </head>
        <body>
            <div class="card">
                <div class="header">
                    <h1>Txopa</h1>
                    <p>Plataforma Oficial de Apoio a Criadores de Mocambique</p>
                </div>
                <div class="content">
                    <span class="badge">PAGAMENTO CONFIRMADO</span>
                    <h2 class="title">Obrigado pelo seu apoio!</h2>
                    <p class="desc">A sua contribuicao para <strong>%s</strong> foi confirmada e creditada com sucesso.</p>

                    <div class="receipt-box">
                        <table style="width: 100%%; border-collapse: collapse;">
                            <tr style="border-bottom: 1px solid #f1f5f9;">
                                <td style="padding: 6px 0; color: #64748b; font-size: 13px;">Referencia:</td>
                                <td style="padding: 6px 0; font-weight: 600; color: #0f172a; text-align: right; font-size: 13px;">%s</td>
                            </tr>
                            <tr style="border-bottom: 1px solid #f1f5f9;">
                                <td style="padding: 6px 0; color: #64748b; font-size: 13px;">Beneficiario:</td>
                                <td style="padding: 6px 0; font-weight: 600; color: #0f172a; text-align: right; font-size: 13px;">%s</td>
                            </tr>
                            <tr style="border-bottom: 1px solid #f1f5f9;">
                                <td style="padding: 6px 0; color: #64748b; font-size: 13px;">Metodo:</td>
                                <td style="padding: 6px 0; font-weight: 600; color: #0f172a; text-align: right; font-size: 13px;">%s</td>
                            </tr>
                            <tr style="border-top: 2px dashed #cbd5e1;">
                                <td style="padding: 10px 0 0; font-weight: 600; color: #0f172a; font-size: 14px;">Valor Total:</td>
                                <td style="padding: 10px 0 0; font-weight: 700; color: #059669; text-align: right; font-size: 18px;">%s MT</td>
                            </tr>
                        </table>
                    </div>

                    <div class="pdf-notice">
                        O recibo oficial detalhado encontra-se anexado a este email em formato PDF (Recibo-%s.pdf).
                    </div>
                </div>
                <div class="footer">
                    Txopa Mocambique - Suporte: suporte@txopa.app<br/>
                    Maputo, Mocambique
                </div>
            </div>
        </body>
        </html>
        """.formatted(creatorName, reference, creatorName, method, amount, reference);

        EmailEvent event = EmailEvent.builder()
                .toEmail(toEmail)
                .subject("Comprovativo de Doacao - Txopa (Ref: " + reference + ")")
                .body(htmlBody)
                .attachmentBytes(pdfReceipt)
                .attachmentFilename("Recibo-" + reference + ".pdf")
                .build();

        dispatch(event);
    }

    @Override
    public void sendPayoutRequestedAdminEmail(String adminEmail, String creatorName, String creatorUsername, String creatorEmail, String amount, String fee, String totalDeducted, String method, String phone, String date) {
        String htmlBody = """
                <!DOCTYPE html>
                <html lang="pt">
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');
                        body { font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #fafafa; margin: 0; padding: 40px 20px; color: #111827; }
                        .wrapper { max-width: 560px; margin: 0 auto; }
                        .logo { text-align: center; margin-bottom: 24px; }
                        .logo h1 { color: #111827; margin: 0; font-size: 26px; font-weight: 800; }
                        .logo span { color: #6366f1; }
                        .card { background-color: #ffffff; border-radius: 16px; padding: 36px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05); border: 1px solid #f3f4f6; }
                        .header-badge { display: inline-block; background-color: #eef2ff; color: #4f46e5; font-size: 12px; font-weight: 600; padding: 4px 12px; border-radius: 9999px; margin-bottom: 16px; }
                        .title { font-size: 20px; font-weight: 700; margin: 0 0 8px; color: #111827; }
                        .desc { font-size: 14px; color: #4b5563; line-height: 1.5; margin-bottom: 24px; }
                        .table-box { background-color: #f9fafb; border: 1px solid #e5e7eb; border-radius: 12px; padding: 18px 20px; margin-bottom: 24px; }
                        table { width: 100%%; border-collapse: collapse; }
                        td { padding: 8px 0; font-size: 14px; }
                        .label { color: #6b7280; }
                        .val { text-align: right; font-weight: 600; color: #111827; }
                        .highlight { font-size: 18px; color: #059669; font-weight: 700; }
                        .footer { text-align: center; margin-top: 28px; font-size: 12px; color: #9ca3af; }
                    </style>
                </head>
                <body>
                    <div class="wrapper">
                        <div class="logo">
                            <h1>TXOP<span>A</span></h1>
                        </div>
                        <div class="card">
                            <span class="header-badge">NOVO PEDIDO DE LEVANTAMENTO</span>
                            <h2 class="title">Solicitação de Levantamento Recebida</h2>
                            <p class="desc">O criador <strong>%s</strong> (@%s) solicitou uma retirada de fundos da plataforma.</p>
                            
                            <div class="table-box">
                                <table>
                                    <tr>
                                        <td class="label">Criador</td>
                                        <td class="val">%s (@%s)</td>
                                    </tr>
                                    <tr>
                                        <td class="label">Email do Criador</td>
                                        <td class="val">%s</td>
                                    </tr>
                                    <tr>
                                        <td class="label">Método de Envio</td>
                                        <td class="val">%s</td>
                                    </tr>
                                    <tr>
                                        <td class="label">Número de Telemóvel</td>
                                        <td class="val">%s</td>
                                    </tr>
                                    <tr>
                                        <td class="label">Data do Pedido</td>
                                        <td class="val">%s</td>
                                    </tr>
                                    <tr style="border-top: 1px solid #e5e7eb;">
                                        <td class="label" style="padding-top: 12px;">Valor Líquido a Transferir</td>
                                        <td class="val highlight" style="padding-top: 12px;">%s MT</td>
                                    </tr>
                                    <tr>
                                        <td class="label">Taxa Plataforma (10%%)</td>
                                        <td class="val" style="color: #6b7280;">+%s MT</td>
                                    </tr>
                                    <tr style="border-top: 1px dashed #e5e7eb;">
                                        <td class="label" style="font-weight: 600;">Total Abatido no Saldo</td>
                                        <td class="val" style="color: #dc2626;">-%s MT</td>
                                    </tr>
                                </table>
                            </div>

                            <p style="font-size: 13px; color: #6b7280; margin: 0;">
                                Por favor, aceda ao painel administrativo para validar a solicitação e efetuar a transferência manual/automática.
                            </p>
                        </div>
                        <div class="footer">
                            Painel Administrativo Txopa - Gestão Financeira<br/>
                            Maputo, Moçambique
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(creatorName, creatorUsername, creatorName, creatorUsername, creatorEmail, method, phone, date, amount, fee, totalDeducted);

        EmailEvent event = EmailEvent.builder()
                .toEmail(adminEmail)
                .subject("Novo Pedido de Levantamento: " + creatorName + " (" + amount + " MT)")
                .body(htmlBody)
                .build();

        dispatch(event);
    }

    @Override
    public void sendPayoutRequestedCreatorEmail(String creatorEmail, String creatorName, String amount, String method, String phone, String fee, String totalDeducted, String date) {
        String htmlBody = """
                <!DOCTYPE html>
                <html lang="pt">
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');
                        body { font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #fafafa; margin: 0; padding: 40px 20px; color: #111827; }
                        .wrapper { max-width: 540px; margin: 0 auto; }
                        .logo { text-align: center; margin-bottom: 24px; }
                        .logo h1 { color: #111827; margin: 0; font-size: 26px; font-weight: 800; }
                        .logo span { color: #6366f1; }
                        .card { background-color: #ffffff; border-radius: 16px; padding: 36px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05); border: 1px solid #f3f4f6; }
                        .icon-circle { width: 56px; height: 56px; background-color: #ecfdf5; color: #059669; border-radius: 50%%; display: flex; align-items: center; justify-content: center; font-size: 24px; margin: 0 auto 16px; }
                        .title { font-size: 22px; font-weight: 700; text-align: center; margin: 0 0 8px; color: #111827; }
                        .desc { font-size: 14px; text-align: center; color: #4b5563; line-height: 1.6; margin-bottom: 24px; }
                        .table-box { background-color: #f9fafb; border: 1px solid #e5e7eb; border-radius: 12px; padding: 18px 20px; margin-bottom: 20px; }
                        table { width: 100%%; border-collapse: collapse; }
                        td { padding: 8px 0; font-size: 14px; }
                        .label { color: #6b7280; }
                        .val { text-align: right; font-weight: 600; color: #111827; }
                        .highlight { font-size: 18px; color: #059669; font-weight: 700; }
                        .notice-box { background-color: #eff6ff; border-left: 4px solid #3b82f6; padding: 14px 16px; border-radius: 8px; margin-bottom: 20px; }
                        .notice-title { font-size: 13px; font-weight: 700; color: #1e40af; margin-bottom: 4px; }
                        .notice-text { font-size: 13px; color: #1e3a8a; line-height: 1.5; margin: 0; }
                        .footer { text-align: center; margin-top: 28px; font-size: 12px; color: #9ca3af; }
                    </style>
                </head>
                <body>
                    <div class="wrapper">
                        <div class="logo">
                            <h1>TXOP<span>A</span></h1>
                        </div>
                        <div class="card">
                            <div class="icon-circle">✓</div>
                            <h2 class="title">Pedido de Levantamento Efetuado</h2>
                            <p class="desc">Olá, <strong>%s</strong>! O seu pedido de levantamento foi registado no sistema com sucesso.</p>

                            <div class="table-box">
                                <table>
                                    <tr>
                                        <td class="label">Valor a Receber na Conta</td>
                                        <td class="val highlight">%s MT</td>
                                    </tr>
                                    <tr>
                                        <td class="label">Método Selecionado</td>
                                        <td class="val">%s</td>
                                    </tr>
                                    <tr>
                                        <td class="label">Número de Destino</td>
                                        <td class="val">%s</td>
                                    </tr>
                                    <tr>
                                        <td class="label">Taxa de Serviço da Plataforma</td>
                                        <td class="val" style="color: #6b7280;">%s MT</td>
                                    </tr>
                                    <tr style="border-top: 1px dashed #e5e7eb;">
                                        <td class="label" style="font-weight: 600; padding-top: 8px;">Total Abatido no Saldo</td>
                                        <td class="val" style="color: #dc2626; padding-top: 8px;">%s MT</td>
                                    </tr>
                                    <tr>
                                        <td class="label">Data da Solicitação</td>
                                        <td class="val">%s</td>
                                    </tr>
                                </table>
                            </div>

                            <div class="notice-box">
                                <div class="notice-title">⏱ Prazo de Processamento</div>
                                <p class="notice-text">
                                    O pedido pode levar <strong>até 3 dias úteis</strong> para ser processado e creditado na sua conta móvel. Receberá uma nova notificação assim que a transferência for concluída.
                                </p>
                            </div>

                            <p style="font-size: 12px; color: #9ca3af; text-align: center; margin: 0;">
                                Se tiver alguma dúvida, responda a este email ou contacte o nosso suporte em suporte@txopa.app.
                            </p>
                        </div>
                        <div class="footer">
                            Txopa Moçambique - Apoie quem você admira<br/>
                            Maputo, Moçambique
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(creatorName, amount, method, phone, fee, totalDeducted, date);

        EmailEvent event = EmailEvent.builder()
                .toEmail(creatorEmail)
                .subject("Confirmação de Pedido de Levantamento - Txopa (" + amount + " MT)")
                .body(htmlBody)
                .build();

        dispatch(event);
    }

    @Override
    public void sendPayoutApprovedEmail(String creatorEmail, String creatorName, String amount, String method, String phone, String transactionRef, String note, String date) {
        String htmlBody = """
                <!DOCTYPE html>
                <html lang="pt">
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');
                        body { font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #fafafa; margin: 0; padding: 40px 20px; color: #111827; }
                        .wrapper { max-width: 540px; margin: 0 auto; }
                        .logo { text-align: center; margin-bottom: 24px; }
                        .logo h1 { color: #111827; margin: 0; font-size: 26px; font-weight: 800; }
                        .logo span { color: #6366f1; }
                        .card { background-color: #ffffff; border-radius: 16px; padding: 36px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05); border: 1px solid #f3f4f6; }
                        .icon-circle { width: 56px; height: 56px; background-color: #ecfdf5; color: #059669; border-radius: 50%%; display: flex; align-items: center; justify-content: center; font-size: 26px; margin: 0 auto 16px; }
                        .title { font-size: 22px; font-weight: 700; text-align: center; margin: 0 0 8px; color: #111827; }
                        .desc { font-size: 14px; text-align: center; color: #4b5563; line-height: 1.6; margin-bottom: 24px; }
                        .table-box { background-color: #f9fafb; border: 1px solid #e5e7eb; border-radius: 12px; padding: 18px 20px; margin-bottom: 20px; }
                        table { width: 100%%; border-collapse: collapse; }
                        td { padding: 8px 0; font-size: 14px; }
                        .label { color: #6b7280; }
                        .val { text-align: right; font-weight: 600; color: #111827; }
                        .highlight { font-size: 18px; color: #059669; font-weight: 700; }
                        .note-box { background-color: #f3f4f6; padding: 12px 16px; border-radius: 8px; font-size: 13px; color: #374151; margin-bottom: 20px; }
                        .footer { text-align: center; margin-top: 28px; font-size: 12px; color: #9ca3af; }
                    </style>
                </head>
                <body>
                    <div class="wrapper">
                        <div class="logo">
                            <h1>TXOP<span>A</span></h1>
                        </div>
                        <div class="card">
                            <div class="icon-circle">💰</div>
                            <h2 class="title">Levantamento Concluído com Sucesso!</h2>
                            <p class="desc">Olá, <strong>%s</strong>! O seu pedido de levantamento foi processado e o montante foi transferido para a sua conta móvel.</p>

                            <div class="table-box">
                                <table>
                                    <tr>
                                        <td class="label">Valor Transferido</td>
                                        <td class="val highlight">%s MT</td>
                                    </tr>
                                    <tr>
                                        <td class="label">Canal / Método</td>
                                        <td class="val">%s</td>
                                    </tr>
                                    <tr>
                                        <td class="label">Número Beneficiário</td>
                                        <td class="val">%s</td>
                                    </tr>
                                    <tr>
                                        <td class="label">Referência de Transação</td>
                                        <td class="val" style="font-family: monospace;">%s</td>
                                    </tr>
                                    <tr>
                                        <td class="label">Data de Liquidação</td>
                                        <td class="val">%s</td>
                                    </tr>
                                </table>
                            </div>

                            %s

                            <p style="font-size: 12px; color: #9ca3af; text-align: center; margin: 0;">
                                Obrigado por fazer parte da comunidade Txopa. Continue a inspirar os seus apoiadores!
                            </p>
                        </div>
                        <div class="footer">
                            Txopa Moçambique - Apoie quem você admira<br/>
                            Maputo, Moçambique
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                        creatorName,
                        amount,
                        method,
                        phone,
                        (transactionRef != null && !transactionRef.isBlank()) ? transactionRef : "N/D",
                        date,
                        (note != null && !note.isBlank()) ? "<div class=\"note-box\"><strong>Nota do Administrador:</strong> " + note + "</div>" : ""
                );

        EmailEvent event = EmailEvent.builder()
                .toEmail(creatorEmail)
                .subject("Levantamento Concluído - Txopa (" + amount + " MT)")
                .body(htmlBody)
                .build();

        dispatch(event);
    }

    @Override
    public void sendPayoutRejectedEmail(String creatorEmail, String creatorName, String amount, String method, String reason, String date) {
        String htmlBody = """
                <!DOCTYPE html>
                <html lang="pt">
                <head>
                    <meta charset="utf-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                    <style>
                        @import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap');
                        body { font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background-color: #fafafa; margin: 0; padding: 40px 20px; color: #111827; }
                        .wrapper { max-width: 540px; margin: 0 auto; }
                        .logo { text-align: center; margin-bottom: 24px; }
                        .logo h1 { color: #111827; margin: 0; font-size: 26px; font-weight: 800; }
                        .logo span { color: #6366f1; }
                        .card { background-color: #ffffff; border-radius: 16px; padding: 36px; box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05); border: 1px solid #f3f4f6; }
                        .icon-circle { width: 56px; height: 56px; background-color: #fef2f2; color: #dc2626; border-radius: 50%%; display: flex; align-items: center; justify-content: center; font-size: 24px; margin: 0 auto 16px; }
                        .title { font-size: 22px; font-weight: 700; text-align: center; margin: 0 0 8px; color: #111827; }
                        .desc { font-size: 14px; text-align: center; color: #4b5563; line-height: 1.6; margin-bottom: 24px; }
                        .table-box { background-color: #f9fafb; border: 1px solid #e5e7eb; border-radius: 12px; padding: 18px 20px; margin-bottom: 20px; }
                        table { width: 100%%; border-collapse: collapse; }
                        td { padding: 8px 0; font-size: 14px; }
                        .label { color: #6b7280; }
                        .val { text-align: right; font-weight: 600; color: #111827; }
                        .reason-box { background-color: #fef2f2; border: 1px solid #fecaca; padding: 14px 16px; border-radius: 8px; font-size: 13px; color: #991b1b; margin-bottom: 20px; }
                        .footer { text-align: center; margin-top: 28px; font-size: 12px; color: #9ca3af; }
                    </style>
                </head>
                <body>
                    <div class="wrapper">
                        <div class="logo">
                            <h1>TXOP<span>A</span></h1>
                        </div>
                        <div class="card">
                            <div class="icon-circle">✕</div>
                            <h2 class="title">Atualização sobre o seu Levantamento</h2>
                            <p class="desc">Olá, <strong>%s</strong>! O seu pedido de levantamento não pôde ser executado pela administração.</p>

                            <div class="table-box">
                                <table>
                                    <tr>
                                        <td class="label">Valor Solicitado</td>
                                        <td class="val">%s MT</td>
                                    </tr>
                                    <tr>
                                        <td class="label">Método</td>
                                        <td class="val">%s</td>
                                    </tr>
                                    <tr>
                                        <td class="label">Data</td>
                                        <td class="val">%s</td>
                                    </tr>
                                    <tr>
                                        <td class="label">Estado</td>
                                        <td class="val" style="color: #dc2626;">Recusado / Cancelado</td>
                                    </tr>
                                </table>
                            </div>

                            <div class="reason-box">
                                <strong>Motivo informado pela equipa:</strong><br/>
                                %s
                            </div>

                            <p style="font-size: 13px; color: #6b7280; line-height: 1.5; margin: 0 0 16px;">
                                O saldo correspondente permanece disponível na sua conta para nova solicitação assim que a situação estiver regularizada.
                            </p>
                        </div>
                        <div class="footer">
                            Txopa Moçambique - Suporte: suporte@txopa.app<br/>
                            Maputo, Moçambique
                        </div>
                    </div>
                </body>
                </html>
                """.formatted(
                        creatorName,
                        amount,
                        method,
                        date,
                        (reason != null && !reason.isBlank()) ? reason : "Inconsistência nos dados ou impossibilidade de transferência para a conta informada."
                );

        EmailEvent event = EmailEvent.builder()
                .toEmail(creatorEmail)
                .subject("Atualização do Pedido de Levantamento - Txopa (" + amount + " MT)")
                .body(htmlBody)
                .build();

        dispatch(event);
    }
}
