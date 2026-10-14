package com.donations.donations.service;

import com.donations.donations.repository.EmailRepository;
import com.donations.donations.repository.*;
import com.donations.donations.model.User;
import com.donations.donations.repository.UserRepository;
import com.donations.donations.repository.*;
import com.donations.donations.model.PhoneVerification;
import com.donations.donations.repository.PhoneVerificationRepository;
import com.donations.donations.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SendPhoneOtpUseCase {

    private final PhoneVerificationRepository repository;
    private final EmailRepository emailRepository;
    private final UserRepository userRepository;

    @Transactional
    public void execute(UUID userId, String rawPhoneNumber, String requestedProvider) {
        if (rawPhoneNumber == null || rawPhoneNumber.isBlank()) {
            throw new IllegalArgumentException("O número de telefone é obrigatório.");
        }

        String digits = rawPhoneNumber.replaceAll("\\D", "").replaceFirst("^258", "");
        if (digits.length() != 9) {
            throw new IllegalArgumentException("O número de telefone deve ter 9 dígitos (ex: 84XXXXXXX ou 86XXXXXXX).");
        }

        String provider;
        if (digits.startsWith("84") || digits.startsWith("85")) {
            provider = "MPESA";
        } else if (digits.startsWith("86") || digits.startsWith("87")) {
            provider = "EMOLA";
        } else {
            throw new IllegalArgumentException("Prefixo inválido. Use 84 ou 85 para Vodacom M-Pesa, ou 86 ou 87 para Movitel e-Mola.");
        }

        if (requestedProvider != null && !requestedProvider.isBlank()) {
            String cleanReq = requestedProvider.trim().toUpperCase();
            if (!provider.equals(cleanReq)) {
                if ("MPESA".equals(cleanReq)) {
                    throw new IllegalArgumentException("O número " + digits + " não é Vodacom M-Pesa (deve começar por 84 ou 85).");
                } else if ("EMOLA".equals(cleanReq)) {
                    throw new IllegalArgumentException("O número " + digits + " não é Movitel e-Mola (deve começar por 86 ou 87).");
                }
            }
        }

        String otp = String.format("%06d", new Random().nextInt(1000000));
        
        PhoneVerification entity = PhoneVerification.builder()
                .userId(userId)
                .phoneNumber(digits)
                .provider(provider)
                .otpCode(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .verified(false)
                .build();
                
        repository.save(entity);
        
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("Utilizador não encontrado"));
        emailRepository.sendPhoneOtpEmail(user.getEmail(), otp, digits);
    }

    @Transactional
    public void execute(UUID userId, String phoneNumber) {
        execute(userId, phoneNumber, null);
    }
}
