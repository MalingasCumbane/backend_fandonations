package com.donations.donations.application.usecase;

import com.donations.donations.application.port.out.EmailPort;
import com.donations.donations.domain.model.User;
import com.donations.donations.domain.repository.UserRepository;
import com.donations.donations.infrastructure.persistence.entity.PhoneVerificationJpaEntity;
import com.donations.donations.infrastructure.persistence.repository.PhoneVerificationJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SendPhoneOtpUseCase {

    private final PhoneVerificationJpaRepository repository;
    private final EmailPort emailPort;
    private final UserRepository userRepository;

    @Transactional
    public void execute(UUID userId, String phoneNumber) {
        String otp = String.format("%06d", new Random().nextInt(1000000));
        
        PhoneVerificationJpaEntity entity = PhoneVerificationJpaEntity.builder()
                .userId(userId)
                .phoneNumber(phoneNumber)
                .otpCode(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .verified(false)
                .build();
                
        repository.save(entity);
        
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        emailPort.sendPhoneOtpEmail(user.getEmail(), otp, phoneNumber);
    }
}
