package com.donations.donations.service;

import com.donations.donations.model.PhoneVerification;
import com.donations.donations.repository.CreatorRepository;
import com.donations.donations.model.Creator;
//import com.donations.donations.model.PhoneVerification;
//import com.donations.donations.repository.PhoneVerificationRepository;
import com.donations.donations.repository.PhoneVerificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerifyPhoneOtpUseCase {

    private final PhoneVerificationRepository repository;
    private final CreatorRepository creatorRepository;

    @Transactional
    public void execute(UUID userId, String rawPhoneNumber, String otp) {
        if (rawPhoneNumber == null || rawPhoneNumber.isBlank()) {
            throw new IllegalArgumentException("O número de telefone é obrigatório.");
        }
        if (otp == null || otp.isBlank()) {
            throw new IllegalArgumentException("O código OTP é obrigatório.");
        }

        String digits = rawPhoneNumber.replaceAll("\\D", "").replaceFirst("^258", "");

        PhoneVerification entity = repository.findFirstByUserIdAndPhoneNumberOrderByCreatedAtDesc(userId, digits)
                .or(() -> repository.findFirstByUserIdAndPhoneNumberOrderByCreatedAtDesc(userId, rawPhoneNumber))
                .orElseThrow(() -> new IllegalArgumentException("Nenhum pedido de validação pendente encontrado para este número."));
                
        if (entity.isVerified()) {
            throw new IllegalStateException("Este número já se encontra verificado.");
        }
        if (entity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("O código de verificação expirou. Por favor solicite um novo.");
        }
        if (!entity.getOtpCode().equals(otp.trim())) {
            throw new IllegalArgumentException("Código de validação incorreto.");
        }
        
        entity.setVerified(true);
        repository.save(entity);
        
        Creator creator = creatorRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Perfil de criador não encontrado."));
                
        if (digits.startsWith("84") || digits.startsWith("85")) {
            creator.setMpesaPhone(digits);
        } else if (digits.startsWith("86") || digits.startsWith("87")) {
            creator.setEmolaPhone(digits);
        }

        // Se o phone principal estiver vazio, atribui este
        if (creator.getPhone() == null || creator.getPhone().isBlank()) {
            creator.setPhone(digits);
        }

        creatorRepository.save(creator);
    }

    @Transactional
    public void removePhone(UUID userId, String provider) {
        Creator creator = creatorRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalStateException("Perfil de criador não encontrado."));

        if ("MPESA".equalsIgnoreCase(provider)) {
            creator.setMpesaPhone(null);
            if (creator.getPhone() != null && (creator.getPhone().startsWith("84") || creator.getPhone().startsWith("85"))) {
                creator.setPhone(creator.getEmolaPhone());
            }
        } else if ("EMOLA".equalsIgnoreCase(provider)) {
            creator.setEmolaPhone(null);
            if (creator.getPhone() != null && (creator.getPhone().startsWith("86") || creator.getPhone().startsWith("87"))) {
                creator.setPhone(creator.getMpesaPhone());
            }
        } else {
            throw new IllegalArgumentException("Provedor inválido. Utilize MPESA ou EMOLA.");
        }

        creatorRepository.save(creator);
    }
}
