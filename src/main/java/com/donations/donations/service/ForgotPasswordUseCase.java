package com.donations.donations.service;

import com.donations.donations.dto.ForgotPasswordCommand;
import com.donations.donations.model.PasswordResetToken;
import com.donations.donations.model.User;
import com.donations.donations.repository.EmailRepository;
import com.donations.donations.repository.PasswordResetTokenRepository;
import com.donations.donations.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ForgotPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailRepository emailRepository;

    @Transactional
    public void execute(ForgotPasswordCommand command) {
        if (command == null || command.getEmail() == null || command.getEmail().isBlank()) {
            return;
        }

        String normalizedEmail = command.getEmail().trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByEmail(normalizedEmail);

        if (userOpt.isEmpty()) {
            log.info("Pedido de recuperação de senha para email não registado: {}", normalizedEmail);
            return;
        }

        User user = userOpt.get();

        try {
            passwordResetTokenRepository.deleteByUserId(user.getId());
        } catch (Exception e) {
            log.warn("Falha ao limpar tokens de recuperação antigos para utilizador {}: {}", user.getId(), e.getMessage());
        }

        String tokenStr = UUID.randomUUID().toString();
        PasswordResetToken token = PasswordResetToken.builder()
                .token(tokenStr)
                .userId(user.getId())
                .expiryDate(LocalDateTime.now().plusHours(1))
                .used(false)
                .build();

        passwordResetTokenRepository.save(token);
        emailRepository.sendPasswordResetEmail(user.getEmail(), tokenStr);
        log.info("Email de recuperação de senha enviado com sucesso para: {}", user.getEmail());
    }
}
