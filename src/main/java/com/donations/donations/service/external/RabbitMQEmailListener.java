package com.donations.donations.service.external;

import com.donations.donations.config.RabbitMQConfig;
import com.donations.donations.dto.EmailEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RabbitMQEmailListener {

    private final EmailDeliveryService emailDeliveryService;

    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void handleEmailEvent(EmailEvent event) {
        log.info("[RabbitMQ Listener] Mensagem de email recebida da fila para {}", event.getToEmail());
        emailDeliveryService.sendEmailSync(event);
    }
}
