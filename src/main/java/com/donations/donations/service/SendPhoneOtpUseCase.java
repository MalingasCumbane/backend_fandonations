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
    public void execute(UUID userId, String phoneNumber) {
        String otp = String.format("%06d", new Random().nextInt(1000000));
        
        PhoneVerification entity = PhoneVerification.builder()
                .userId(userId)
                .phoneNumber(phoneNumber)
                .otpCode(otp)
                .expiresAt(LocalDateTime.now().plusMinutes(10))
                .verified(false)
                .build();
                
        repository.save(entity);
        
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        emailRepository.sendPhoneOtpEmail(user.getEmail(), otp, phoneNumber);
    }
}
