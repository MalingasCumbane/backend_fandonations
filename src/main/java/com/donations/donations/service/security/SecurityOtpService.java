package com.donations.donations.service.security;

import com.donations.donations.model.SecurityOtp;
import com.donations.donations.repository.SecurityOtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SecurityOtpService {

    private final SecurityOtpRepository repository;

    @Transactional
    public String generateAndSaveOtp(UUID userId, String purpose, String targetInfo) {
        String otp = String.format("%06d", new Random().nextInt(1000000));

        SecurityOtp entity = SecurityOtp.builder()
                .userId(userId)
                .purpose(purpose)
                .targetInfo(targetInfo)
                .otpCode(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .used(false)
                .build();

        repository.save(entity);
        return otp;
    }

    @Transactional
    public SecurityOtp verifyAndConsumeOtp(UUID userId, String purpose, String otp) {
        if (otp == null || otp.isBlank()) {
            throw new IllegalArgumentException("O código OTP é obrigatório.");
        }

        SecurityOtp entity = repository.findFirstByUserIdAndPurposeAndUsedFalseOrderByCreatedAtDesc(userId, purpose)
                .orElseThrow(() -> new IllegalArgumentException("Nenhum código de verificação pendente encontrado. Solicite um novo código."));

        if (entity.isUsed()) {
            throw new IllegalStateException("Este código de verificação já foi utilizado.");
        }

        if (entity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("O código de verificação expirou. Por favor solicite um novo.");
        }

        if (!entity.getOtpCode().equals(otp.trim())) {
            throw new IllegalArgumentException("Os dados introduzidos são incorrectos.");
        }

        entity.setUsed(true);
        return repository.save(entity);
    }
}
