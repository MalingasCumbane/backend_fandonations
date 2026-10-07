package com.donations.donations.application.usecase;

import com.donations.donations.application.port.out.CreatorPort;
import com.donations.donations.domain.model.Creator;
import com.donations.donations.infrastructure.persistence.entity.PhoneVerificationJpaEntity;
import com.donations.donations.infrastructure.persistence.repository.PhoneVerificationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VerifyPhoneOtpUseCase {

    private final PhoneVerificationJpaRepository repository;
    private final CreatorPort creatorPort;

    @Transactional
    public void execute(UUID userId, String phoneNumber, String otp) {
        PhoneVerificationJpaEntity entity = repository.findFirstByUserIdAndPhoneNumberOrderByCreatedAtDesc(userId, phoneNumber)
                .orElseThrow(() -> new RuntimeException("OTP request not found"));
                
        if (entity.isVerified()) {
            throw new RuntimeException("Phone is already verified");
        }
        if (entity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP has expired");
        }
        if (!entity.getOtpCode().equals(otp)) {
            throw new RuntimeException("Invalid OTP");
        }
        
        entity.setVerified(true);
        repository.save(entity);
        
        Creator creator = creatorPort.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Creator profile not found"));
                
        creator.setPhone(phoneNumber);
        creatorPort.save(creator);
    }
}
