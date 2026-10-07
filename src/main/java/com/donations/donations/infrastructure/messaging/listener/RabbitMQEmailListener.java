package com.donations.donations.infrastructure.messaging.listener;

import com.donations.donations.infrastructure.config.RabbitMQConfig;
import com.donations.donations.infrastructure.messaging.dto.EmailEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQEmailListener {

    private final JavaMailSender mailSender;

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void handleEmailEvent(EmailEvent event) {
        log.info("Received request to send email to {}", event.getToEmail());

        try {
            jakarta.mail.internet.MimeMessage message = mailSender.createMimeMessage();
            org.springframework.mail.javamail.MimeMessageHelper helper = new org.springframework.mail.javamail.MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(event.getToEmail());
            helper.setSubject(event.getSubject());
            helper.setText(event.getBody(), true); // true = isHtml
            
            mailSender.send(message);
            
            log.info("Email successfully sent to {}", event.getToEmail());
        } catch (Exception e) {
            log.error("Failed to send email to {}. Retrying... Reason: {}", event.getToEmail(), e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
