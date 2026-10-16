package com.donations.donations.service;

import com.donations.donations.dto.ResetPasswordCommand;
import com.donations.donations.model.PasswordResetToken;
import com.donations.donations.model.User;
import com.donations.donations.repository.PasswordEncoderRepository;
import com.donations.donations.repository.PasswordResetTokenRepository;
import com.donations.donations.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResetPasswordUseCase {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoderRepository passwordEncoder;

    @Transactional
    public void execute(ResetPasswordCommand command) {
        if (command == null || command.getToken() == null || command.getToken().isBlank()) {
            throw new IllegalArgumentException("Token de recuperação não fornecido.");
        }

        if (command.getNewPassword() == null || command.getNewPassword().trim().length() < 6) {
            throw new IllegalArgumentException("A nova palavra-passe deve ter no mínimo 6 caracteres.");
        }

        PasswordResetToken token = passwordResetTokenRepository.findByToken(command.getToken().trim()).orElseThrow(() -> new IllegalArgumentException("Link de recuperação inválido ou inexistente."));

        if (token.isUsed()) {
            throw new IllegalStateException("Este link de recuperação já foi utilizado. Solicite um novo.");
        }

        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Este link de recuperação expirou. Por favor solicite um novo.");
        }

        User user = userRepository.findById(token.getUserId()).orElseThrow(() -> new IllegalStateException("Utilizador associado ao token não encontrado."));

        user.setPasswordHash(passwordEncoder.encode(command.getNewPassword().trim()));
        user.setActive(true);
        userRepository.save(user);

        token.setUsed(true);
        passwordResetTokenRepository.save(token);

        log.info("Senha redefinida com sucesso para o utilizador: {}", user.getEmail());
    }
}
