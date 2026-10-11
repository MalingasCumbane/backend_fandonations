with open('src/main/java/com/donations/donations/repository/EmailRepository.java', 'r') as f:
    content = f.read()

content = content.replace(
    "void sendDonationReceiptEmail(String toEmail, String creatorName, String amount, String reference);",
    "void sendDonationReceiptEmail(String toEmail, String creatorName, String amount, String reference, String method);"
)

with open('src/main/java/com/donations/donations/repository/EmailRepository.java', 'w') as f:
    f.write(content)

with open('src/main/java/com/donations/donations/service/external/RabbitMQEmailPublisherAdapter.java', 'r') as f:
    adapter = f.read()

# Replace sendDonationReceiptEmail in RabbitMQEmailPublisherAdapter
old_impl = """    @Override
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
                
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_EXCHANGE, RabbitMQConfig.EMAIL_ROUTING_KEY, event);
        } catch (Exception e) {
            System.err.println("Failed to send email to RabbitMQ: " + e.getMessage());
        }
    }"""

new_impl = """    @Override
    public void sendDonationReceiptEmail(String toEmail, String creatorName, String amount, String reference, String method) {
        byte[] pdfReceipt = null;
        try {
            pdfReceipt = com.donations.donations.util.PdfReceiptGenerator.generateReceipt(
                toEmail, creatorName, amount, reference, method
            );
        } catch (Exception e) {
            System.err.println("Erro ao gerar PDF de recibo: " + e.getMessage());
        }

        String htmlBody = \"\"\"
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
                .row { display: flex; justify-content: space-between; font-size: 13px; padding: 8px 0; border-bottom: 1px solid #f1f5f9; }
                .row:last-child { border-bottom: none; }
                .row-label { color: #64748b; }
                .row-val { font-weight: 600; color: #0f172a; }
                .total-row { padding-top: 12px; margin-top: 6px; border-top: 2px dashed #cbd5e1; font-size: 15px; }
                .total-val { color: #059669; font-size: 18px; font-weight: 700; }
                .pdf-notice { background: #eff6ff; border: 1px solid #bfdbfe; border-radius: 6px; padding: 12px 16px; font-size: 13px; color: #1e40af; margin-bottom: 24px; display: flex; align-items: center; }
                .footer { padding: 20px 32px; background: #f8fafc; border-top: 1px solid #e2e8f0; font-size: 11px; color: #94a3b8; text-align: center; }
            </style>
        </head>
        <body>
            <div class="card">
                <div class="header">
                    <h1>APOIA</h1>
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
                    APOIA Mocambique - Suporte: suporte@apoia.co.mz<br/>
                    Maputo, Mocambique
                </div>
            </div>
        </body>
        </html>
        \"\"\".formatted(creatorName, reference, creatorName, method, amount, reference);

        EmailEvent event = EmailEvent.builder()
                .toEmail(toEmail)
                .subject("Comprovativo de Doacao - APOIA (Ref: " + reference + ")")
                .body(htmlBody)
                .attachmentBytes(pdfReceipt)
                .attachmentFilename("Recibo-" + reference + ".pdf")
                .build();

        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.EMAIL_EXCHANGE, RabbitMQConfig.EMAIL_ROUTING_KEY, event);
        } catch (Exception e) {
            System.err.println("Failed to send email to RabbitMQ: " + e.getMessage());
        }
    }"""

adapter = adapter.replace(old_impl, new_impl)
with open('src/main/java/com/donations/donations/service/external/RabbitMQEmailPublisherAdapter.java', 'w') as f:
    f.write(adapter)
print("Updated EmailRepository and RabbitMQEmailPublisherAdapter")
