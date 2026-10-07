package com.donations.donations.application.usecase;

import com.donations.donations.application.port.out.EmailPort;
import com.donations.donations.domain.model.User;
import com.donations.donations.domain.model.VerificationToken;
import com.donations.donations.domain.repository.UserRepository;
import com.donations.donations.domain.repository.VerificationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VerifyEmailUseCase {

    private final VerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailPort emailPort;

    @Transactional
    public void execute(String tokenStr) {
        VerificationToken token = tokenRepository.findByToken(tokenStr)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (token.isExpired()) {
            throw new RuntimeException("Token expired");
        }

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.activate();
        userRepository.save(user);
        
        tokenRepository.delete(token);

        emailPort.sendWelcomeEmail(user.getEmail());
    }
}
