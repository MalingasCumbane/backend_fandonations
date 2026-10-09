package com.donations.donations.service;

import com.donations.donations.repository.EmailRepository;
import com.donations.donations.repository.*;
import com.donations.donations.model.User;
import com.donations.donations.model.VerificationToken;
import com.donations.donations.repository.UserRepository;
import com.donations.donations.repository.*;
import com.donations.donations.repository.VerificationTokenRepository;
import com.donations.donations.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerifyEmailUseCase {

    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailRepository emailRepository;

    @Transactional
    public void execute(String tokenStr) {
        VerificationToken token = tokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (token.getExpiryDate().isBefore(java.time.LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setActive(true);
        userRepository.save(user);
        
        tokenRepository.delete(token);

        emailRepository.sendWelcomeEmail(user.getEmail());
    }
}
