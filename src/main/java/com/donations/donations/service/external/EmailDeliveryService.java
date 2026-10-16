package com.donations.donations.service.external;

import com.donations.donations.dto.EmailEvent;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailDeliveryService {

    private final JavaMailSender mailSender;

    public void sendEmailSync(EmailEvent event) {
        doSend(event);
    }

    @Async
    public void sendEmailAsync(EmailEvent event) {
        doSend(event);
    }

    private void doSend(EmailEvent event) {
        if (event == null || event.getToEmail() == null || event.getToEmail().isBlank()) {
            log.warn("[Email] Tentativa de envio de email com destinatário vazio/nulo.");
            return;
        }

        log.info("[Email] A enviar email para '{}' | Assunto: '{}'", event.getToEmail(), event.getSubject());

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("malingascumbane1@gmail.com", "Txopa");
            helper.setTo(event.getToEmail());
            helper.setSubject(event.getSubject());
            helper.setText(event.getBody(), true);

            if (event.getAttachmentBytes() != null && event.getAttachmentBytes().length > 0) {
                String filename = event.getAttachmentFilename() != null ? event.getAttachmentFilename() : "recibo.pdf";
                helper.addAttachment(filename, new ByteArrayResource(event.getAttachmentBytes()));
            }

            mailSender.send(message);
            log.info("[Email] Email enviado com sucesso via SMTP para '{}' | Assunto: '{}'", event.getToEmail(), event.getSubject());
        } catch (Exception e) {
            log.error("[Email ERROR] Falha ao enviar email via SMTP para '{}' | Assunto: '{}' | Erro: {}",
                    event.getToEmail(), event.getSubject(), e.getMessage(), e);
        }
    }
}
