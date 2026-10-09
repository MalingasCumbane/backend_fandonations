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
    public void execute(UUID userId, String phoneNumber, String otp) {
        PhoneVerification entity = repository.findFirstByUserIdAndPhoneNumberOrderByCreatedAtDesc(userId, phoneNumber)
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
        
        Creator creator = creatorRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Creator profile not found"));
                
        creator.setPhone(phoneNumber);
        creatorRepository.save(creator);
    }
}
